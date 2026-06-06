package io.github.mundanej.mlp.adapter.glpk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

final class GlpkCliAdapterTest {
  @TempDir private Path tempDir;

  @Test
  void hasExpectedId() {
    assertEquals("glpk", new GlpkCliAdapter().id().name());
    assertEquals("cli", new GlpkCliAdapter().id().mode());
    assertThrows(IllegalArgumentException.class, () -> new GlpkCliAdapter(null));
    assertThrows(IllegalArgumentException.class, () -> new GlpkCliAdapter(" "));
  }

  @Test
  void constructsCommandWithModelSolutionAndTimeout() {
    List<String> command =
        new GlpkCliAdapter("glpsol")
            .command(
                tempDir.resolve("model.mps"),
                tempDir.resolve("solution.sol"),
                SolverOptions.defaults());

    assertEquals(
        List.of(
            "glpsol",
            "--freemps",
            tempDir.resolve("model.mps").toString(),
            "--tmlim",
            "60",
            "--write",
            tempDir.resolve("solution.sol").toString()),
        command);
  }

  @Test
  void normalizesStatusText() {
    GlpkCliAdapter adapter = new GlpkCliAdapter();

    assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("OPTIMAL LP SOLUTION FOUND", 0));
    assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("OPTIMAL SOLUTION FOUND", 0));
    assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("Status:     OPTIMAL", 0));
    assertEquals(
        SolverStatus.INFEASIBLE_OR_UNBOUNDED, adapter.parseStatus("infeasible or unbounded", 0));
    assertEquals(
        SolverStatus.INFEASIBLE, adapter.parseStatus("PROBLEM HAS NO FEASIBLE SOLUTION", 0));
    assertEquals(SolverStatus.INFEASIBLE, adapter.parseStatus("Status:     INFEASIBLE", 0));
    assertEquals(SolverStatus.INFEASIBLE, adapter.parseStatus("problem is infeasible", 0));
    assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("PROBLEM HAS UNBOUNDED SOLUTION", 0));
    assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("No dual feasible solution", 0));
    assertEquals(SolverStatus.UNSUPPORTED, adapter.parseStatus("Integer optimization begins", 0));
    assertEquals(SolverStatus.TIME_LIMIT, adapter.parseStatus("TIME LIMIT EXCEEDED", 0));
    assertEquals(SolverStatus.NUMERICAL_FAILURE, adapter.parseStatus("numerical issue", 0));
    assertEquals(SolverStatus.ERROR, adapter.parseStatus("solver failed", 1));
    assertEquals(SolverStatus.UNKNOWN, adapter.parseStatus("ordinary diagnostic", 0));
  }

  @Test
  void parsesObjectiveValue() {
    GlpkCliAdapter adapter = new GlpkCliAdapter();

    assertEquals(12.5d, adapter.parseObjective("Objective:  OBJ = 12.5 (MINimum)").orElseThrow());
    assertEquals(2.0d, adapter.parseObjective("obj = 2.0").orElseThrow());
    assertEquals(3.0d, adapter.parseObjective("~     0: obj = 3.0 infeas = 0").orElseThrow());
    assertTrue(adapter.parseObjective("Objective: OBJ = NaN").isEmpty());
    assertTrue(adapter.hasMalformedObjective("Objective: OBJ = NaN"));
    assertTrue(adapter.hasMalformedObjective("Objective: OBJ = 1e309"));
    assertTrue(adapter.hasMalformedObjective("Objective: OBJ = abc"));
    assertTrue(adapter.parseObjective("no objective").isEmpty());
    assertTrue(!adapter.hasMalformedObjective("no objective"));
  }

  @Test
  void reportsUnavailableBinary() {
    SolverRunResult result =
        new GlpkCliAdapter("missing-glpsol-binary-for-test")
            .solve(input(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("unavailable"));
  }

  @Test
  void reportsUnsupportedMpsFeature() {
    LpProblem problem =
        new LpProblem(
            "max",
            new LpObjective(ObjectiveSense.MAXIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    SolverRunResult result =
        new GlpkCliAdapter("missing-glpsol-binary-for-test")
            .solve(
                SolverInput.withGeneratedNames(
                    problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0})),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("Unsupported MPS feature"));
  }

  @Test
  void reportsMalformedObjectiveEvenForNonOptimalStatus() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-glpsol.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                echo 'TIME LIMIT EXCEEDED'
                echo 'Objective:  OBJ = 1e309'
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new GlpkCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("work")));

    assertEquals(SolverStatus.ERROR, result.status());
    assertTrue(result.message().contains("malformed objective"));
  }

  @Test
  void reportsOptimalProcessOutput() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-glpsol-optimal.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                echo 'OPTIMAL LP SOLUTION FOUND'
                echo 'Objective:  OBJ = 7.0'
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new GlpkCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("optimal-work")));

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(7.0d, result.objectiveValue().orElseThrow());
  }

  @Test
  void reportsOptimalWithoutObjectiveAsError() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-glpsol-no-objective.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                echo 'OPTIMAL LP SOLUTION FOUND'
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new GlpkCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("missing-objective-work")));

    assertEquals(SolverStatus.ERROR, result.status());
    assertTrue(result.message().contains("without parseable objective"));
  }

  @Test
  void reportsTimedOutProcess() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-glpsol-timeout.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                sleep 2
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new GlpkCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                new SolverOptions(Duration.ofMillis(25), 1),
                new SolverWorkDirectory(tempDir.resolve("timeout-work")));

    assertEquals(SolverStatus.TIME_LIMIT, result.status());
  }

  @Test
  void boundsCapturedProcessOutput() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-glpsol-large-output.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                head -c 20000 /dev/zero | tr '\\0' x
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new GlpkCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("large-output-work")));

    assertTrue(result.message().contains("[truncated]"));
  }

  @Test
  @EnabledIf("glpsolAvailable")
  void guardedIntegrationSmoke() {
    SolverRunResult result =
        new GlpkCliAdapter()
            .solve(input(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertTrue(result.status() == SolverStatus.OPTIMAL || result.status() == SolverStatus.UNKNOWN);
  }

  private static boolean glpsolAvailable() {
    try {
      Process process = new ProcessBuilder("glpsol", "--version").redirectErrorStream(true).start();
      return process.waitFor() == 0;
    } catch (IOException | InterruptedException exception) {
      if (exception instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }

  private static SolverInput input() {
    LpProblem problem =
        new LpProblem(
            "tiny",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, 1.0d)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }
}
