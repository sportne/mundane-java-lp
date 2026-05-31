package io.github.mundanej.mlp.adapter.highs;

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

final class HighsCliAdapterTest {
    @TempDir
    private Path tempDir;

    @Test
    void hasExpectedId() {
        assertEquals("highs", new HighsCliAdapter().id().name());
        assertEquals("cli", new HighsCliAdapter().id().mode());
    }

    @Test
    void constructsCommandWithModelSolutionTimeoutAndThreads() {
        List<String> command = new HighsCliAdapter("highs").command(
                tempDir.resolve("model.mps"),
                tempDir.resolve("solution.sol"),
                SolverOptions.defaults());

        assertEquals("highs", command.get(0));
        assertTrue(command.contains("--model_file=" + tempDir.resolve("model.mps")));
        assertTrue(command.contains("--solution_file=" + tempDir.resolve("solution.sol")));
        assertTrue(command.contains("--time_limit=60"));
        assertTrue(command.contains("--threads=1"));
    }

    @Test
    void normalizesStatusText() {
        HighsCliAdapter adapter = new HighsCliAdapter();

        assertEquals(SolverStatus.OPTIMAL, adapter.parseStatus("Model status : Optimal", 0));
        assertEquals(SolverStatus.INFEASIBLE_OR_UNBOUNDED,
                adapter.parseStatus("Model status : Infeasible or unbounded", 0));
        assertEquals(SolverStatus.INFEASIBLE, adapter.parseStatus("Model status : Infeasible", 0));
        assertEquals(SolverStatus.UNBOUNDED, adapter.parseStatus("Model status : Unbounded", 0));
        assertEquals(SolverStatus.TIME_LIMIT, adapter.parseStatus("time limit reached", 0));
        assertEquals(SolverStatus.ERROR, adapter.parseStatus("solver failed", 1));
        assertEquals(SolverStatus.UNKNOWN, adapter.parseStatus("Model status : Not optimal", 0));
        assertEquals(SolverStatus.UNKNOWN, adapter.parseStatus("diagnostic references optimal basis", 0));
    }

    @Test
    void parsesObjectiveValue() {
        HighsCliAdapter adapter = new HighsCliAdapter();

        assertEquals(12.5d, adapter.parseObjective("Objective value : 12.5").orElseThrow());
        assertTrue(adapter.parseObjective("Objective value : NaN").isEmpty());
        assertTrue(adapter.hasMalformedObjective("Objective value : NaN"));
        assertTrue(adapter.hasMalformedObjective("Objective value : 1e309"));
        assertTrue(adapter.hasMalformedObjective("Objective value : abc"));
        assertTrue(adapter.parseObjective("no objective").isEmpty());
        assertTrue(!adapter.hasMalformedObjective("no objective"));
    }

    @Test
    void reportsUnavailableBinary() {
        SolverRunResult result = new HighsCliAdapter("missing-highs-binary-for-test").solve(
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
        SolverRunResult result = new HighsCliAdapter("missing-highs-binary-for-test").solve(
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
        Path fakeBinary = tempDir.resolve("fake-highs.sh");
        Files.writeString(fakeBinary, """
                #!/usr/bin/env sh
                echo 'Model status : Time limit'
                echo 'Objective value : 1e309'
                exit 0
                """);
        assertTrue(fakeBinary.toFile().setExecutable(true));

        SolverRunResult result = new HighsCliAdapter(fakeBinary.toString()).solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir.resolve("work")));

        assertEquals(SolverStatus.ERROR, result.status());
        assertTrue(result.message().contains("malformed objective"));
    }

    @Test
    @EnabledIf("highsAvailable")
    void guardedIntegrationSmoke() {
        SolverRunResult result = new HighsCliAdapter().solve(
                input(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertTrue(result.status() == SolverStatus.OPTIMAL
                || result.status() == SolverStatus.UNKNOWN
                || result.status() == SolverStatus.ERROR);
    }

    private static boolean highsAvailable() {
        try {
            Process process = new ProcessBuilder("highs", "--version")
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
