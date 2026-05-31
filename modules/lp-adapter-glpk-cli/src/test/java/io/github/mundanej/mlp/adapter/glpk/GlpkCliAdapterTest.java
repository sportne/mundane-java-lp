package io.github.mundanej.mlp.adapter.glpk;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

final class GlpkCliAdapterTest {
    @TempDir
    private Path tempDir;

    @Test
    void hasExpectedId() {
        assertEquals("glpk", new GlpkCliAdapter().id().name());
        assertEquals("cli", new GlpkCliAdapter().id().mode());
    }

    @Test
    void constructsCommandWithModelSolutionAndTimeout() {
        List<String> command = new GlpkCliAdapter("glpsol").command(
                tempDir.resolve("model.mps"),
                tempDir.resolve("solution.sol"),
                SolverOptions.defaults());

        assertEquals(List.of(
                "glpsol",
                "--freemps",
                tempDir.resolve("model.mps").toString(),
                "--tmlim",
                "60",
                "--write",
                tempDir.resolve("solution.sol").toString()), command);
    }

    @Test
    void normalizesStatusText() {
        GlpkCliAdapter adapter = new GlpkCliAdapter();

        assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("OPTIMAL LP SOLUTION FOUND", 0));
        assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("Status:     OPTIMAL", 0));
        assertEquals(SolverStatus.INFEASIBLE_OR_UNBOUNDED,
                adapter.parseStatus("infeasible or unbounded", 0));
        assertEquals(SolverStatus.INFEASIBLE,
                adapter.parseStatus("PROBLEM HAS NO FEASIBLE SOLUTION", 0));
        assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("PROBLEM HAS UNBOUNDED SOLUTION", 0));
        assertEquals(SolverStatus.TIME_LIMIT, adapter.parseStatus("TIME LIMIT EXCEEDED", 0));
        assertEquals(SolverStatus.ERROR, adapter.parseStatus("solver failed", 1));
    }

    @Test
    void parsesObjectiveValue() {
        GlpkCliAdapter adapter = new GlpkCliAdapter();

        assertEquals(12.5d, adapter.parseObjective("Objective:  OBJ = 12.5 (MINimum)").orElseThrow());
        assertEquals(2.0d, adapter.parseObjective("obj = 2.0").orElseThrow());
        assertTrue(adapter.parseObjective("Objective: OBJ = NaN").isEmpty());
        assertTrue(adapter.hasMalformedObjective("Objective: OBJ = NaN"));
        assertTrue(adapter.hasMalformedObjective("Objective: OBJ = 1e309"));
        assertTrue(adapter.hasMalformedObjective("Objective: OBJ = abc"));
        assertTrue(adapter.parseObjective("no objective").isEmpty());
        assertTrue(!adapter.hasMalformedObjective("no objective"));
    }

    @Test
    void reportsUnavailableBinary() {
        SolverRunResult result = new GlpkCliAdapter("missing-glpsol-binary-for-test").solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("unavailable"));
    }

    @Test
    void reportsUnsupportedMpsFeature() {
        LpProblem problem = new LpProblem(
                "max",
                new LpObjective(ObjectiveSense.MAXIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        SolverRunResult result = new GlpkCliAdapter("missing-glpsol-binary-for-test").solve(
                SolverInput.withGeneratedNames(
                        problem,
                        new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0})),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("Unsupported MPS feature"));
    }

    @Test
    void reportsMalformedObjectiveEvenForNonOptimalStatus() throws IOException {
        Path fakeBinary = tempDir.resolve("fake-glpsol.sh");
        Files.writeString(fakeBinary, """
                #!/usr/bin/env sh
                echo 'TIME LIMIT EXCEEDED'
                echo 'Objective:  OBJ = 1e309'
                exit 0
                """);
        assertTrue(fakeBinary.toFile().setExecutable(true));

        SolverRunResult result = new GlpkCliAdapter(fakeBinary.toString()).solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("work")));

        assertEquals(SolverStatus.ERROR, result.status());
        assertTrue(result.message().contains("malformed objective"));
    }

    @Test
    @EnabledIf("glpsolAvailable")
    void guardedIntegrationSmoke() {
        SolverRunResult result = new GlpkCliAdapter().solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertTrue(result.status() == SolverStatus.OPTIMAL
                || result.status() == SolverStatus.UNKNOWN);
    }

    private static boolean glpsolAvailable() {
        try {
            Process process = new ProcessBuilder("glpsol", "--version")
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private static SolverInput input() {
        LpProblem problem = new LpProblem(
                "tiny",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        return SolverInput.withGeneratedNames(
                problem,
                new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
    }
}
