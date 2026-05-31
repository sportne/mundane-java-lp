package io.github.mundanej.mlp.adapter.clp;

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

final class ClpCliAdapterTest {
  @TempDir private Path tempDir;

  @Test
  void hasExpectedId() {
    assertEquals("clp", new ClpCliAdapter().id().name());
    assertEquals("cli", new ClpCliAdapter().id().mode());
    assertThrows(IllegalArgumentException.class, () -> new ClpCliAdapter(null));
    assertThrows(IllegalArgumentException.class, () -> new ClpCliAdapter(" "));
  }

  @Test
  void constructsCommandWithModelSolutionTimeoutAndThreads() {
    List<String> command =
        new ClpCliAdapter("clp")
            .command(
                tempDir.resolve("model.mps"),
                tempDir.resolve("solution.sol"),
                SolverOptions.defaults());

    assertEquals(
        List.of(
            "clp",
            tempDir.resolve("model.mps").toString(),
            "-seconds",
            "60",
            "-threads",
            "1",
            "-solve",
            "-solution",
            tempDir.resolve("solution.sol").toString()),
        command);
  }

  @Test
  void normalizesStatusText() {
    ClpCliAdapter adapter = new ClpCliAdapter();

    assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("Optimal objective 12.5", 0));
    assertEquals(
        SolverStatus.INFEASIBLE_OR_UNBOUNDED, adapter.parseStatus("infeasible or unbounded", 0));
    assertEquals(SolverStatus.INFEASIBLE, adapter.parseStatus("Primal infeasible", 0));
    assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("Dual infeasible", 0));
    assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("Dual unbounded", 0));
    assertEquals(SolverStatus.INFEASIBLE, adapter.parseStatus("problem is infeasible", 0));
    assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("problem is unbounded", 0));
    assertEquals(SolverStatus.TIME_LIMIT, adapter.parseStatus("Stopped on time", 0));
    assertEquals(SolverStatus.TIME_LIMIT, adapter.parseStatus("seconds limit reached", 0));
    assertEquals(SolverStatus.NUMERICAL_FAILURE, adapter.parseStatus("numerical issue", 0));
    assertEquals(SolverStatus.ERROR, adapter.parseStatus("solver failed", 1));
    assertEquals(SolverStatus.UNKNOWN, adapter.parseStatus("not optimal", 0));
  }

  @Test
  void parsesObjectiveValue() {
    ClpCliAdapter adapter = new ClpCliAdapter();

    assertEquals(12.5d, adapter.parseObjective("Optimal objective 12.5").orElseThrow());
    assertEquals(2.0d, adapter.parseObjective("Objective value : 2.0").orElseThrow());
    assertTrue(adapter.parseObjective("Objective value : NaN").isEmpty());
    assertTrue(adapter.hasMalformedObjective("Objective value : NaN"));
    assertTrue(adapter.hasMalformedObjective("Optimal objective 1e309"));
    assertTrue(adapter.hasMalformedObjective("Objective value : abc"));
    assertTrue(adapter.parseObjective("no objective").isEmpty());
    assertTrue(!adapter.hasMalformedObjective("no objective"));
  }

  @Test
  void reportsUnavailableBinary() {
    SolverRunResult result =
        new ClpCliAdapter("missing-clp-binary-for-test")
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
        new ClpCliAdapter("missing-clp-binary-for-test")
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
    Path fakeBinary = tempDir.resolve("fake-clp.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                echo 'Stopped on time'
                echo 'Objective value : 1e309'
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new ClpCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("work")));

    assertEquals(SolverStatus.ERROR, result.status());
    assertTrue(result.message().contains("malformed objective"));
  }

  @Test
  void reportsOptimalProcessOutput() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-clp-optimal.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                echo 'Optimal objective 7.0'
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new ClpCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("optimal-work")));

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(7.0d, result.objectiveValue().orElseThrow());
  }

  @Test
  void reportsOptimalWithoutObjectiveAsError() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-clp-no-objective.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                echo 'Optimal solution found'
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new ClpCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("missing-objective-work")));

    assertEquals(SolverStatus.ERROR, result.status());
    assertTrue(result.message().contains("without parseable objective"));
  }

  @Test
  void reportsTimedOutProcess() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-clp-timeout.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                sleep 2
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new ClpCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                new SolverOptions(Duration.ofMillis(25), 1),
                new SolverWorkDirectory(tempDir.resolve("timeout-work")));

    assertEquals(SolverStatus.TIME_LIMIT, result.status());
  }

  @Test
  void boundsCapturedProcessOutput() throws IOException {
    Path fakeBinary = tempDir.resolve("fake-clp-large-output.sh");
    Files.writeString(
        fakeBinary,
        """
                #!/usr/bin/env sh
                head -c 20000 /dev/zero | tr '\\0' x
                exit 0
                """);
    assertTrue(fakeBinary.toFile().setExecutable(true));

    SolverRunResult result =
        new ClpCliAdapter(fakeBinary.toString())
            .solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("large-output-work")));

    assertTrue(result.message().contains("[truncated]"));
  }

  @Test
  @EnabledIf("clpAvailable")
  void guardedIntegrationSmoke() {
    SolverRunResult result =
        new ClpCliAdapter()
            .solve(input(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertTrue(
        result.status() == SolverStatus.OPTIMAL
            || result.status() == SolverStatus.UNKNOWN
            || result.status() == SolverStatus.ERROR);
  }

  private static boolean clpAvailable() {
    try {
      Process process = new ProcessBuilder("clp", "-stop").redirectErrorStream(true).start();
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
