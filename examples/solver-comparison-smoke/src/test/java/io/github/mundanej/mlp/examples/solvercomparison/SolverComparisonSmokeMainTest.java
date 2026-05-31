package io.github.mundanej.mlp.examples.solvercomparison;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SolverComparisonSmokeMainTest {
    @TempDir
    private Path tempDir;

    @Test
    void allUnavailableSolversStillProduceReports() throws IOException {
        SolverComparisonSmokeMain.SmokeResult result = SolverComparisonSmokeMain.run(
                tempDir,
                List.of(
                        unavailable("highs"),
                        unavailable("clp"),
                        unavailable("glpk")));

        assertEquals(3, result.records().size());
        assertEquals(0, result.successfulSolvers());
        assertEquals(0, result.failedSolvers());
        assertEquals(3, result.unavailableSolvers());
        assertTrue(Files.readString(result.markdownPath()).contains("SOLVER_UNAVAILABLE"));
        assertTrue(Files.readString(result.jsonPath()).contains("\"outcome\":\"SOLVER_UNAVAILABLE\""));
        assertTrue(Files.readString(result.csvPath()).contains("SOLVER_UNAVAILABLE"));
    }

    @Test
    void atLeastOneAvailableSolverIsReported() throws IOException {
        SolverComparisonSmokeMain.SmokeResult result = SolverComparisonSmokeMain.run(
                tempDir,
                List.of(
                        optimal("highs"),
                        unavailable("clp"),
                        unavailable("glpk")));

        assertEquals(3, result.records().size());
        assertEquals(1, result.successfulSolvers());
        assertEquals(0, result.failedSolvers());
        assertEquals(2, result.unavailableSolvers());
        assertTrue(Files.readString(result.markdownPath()).contains("SUCCESS"));
        assertTrue(Files.readString(result.jsonPath()).contains("\"solver\":\"highs\""));
        assertTrue(Files.readString(result.csvPath()).contains("highs"));
    }

    @Test
    void adapterErrorsAreReportedAsFailures() throws IOException {
        SolverComparisonSmokeMain.SmokeResult result = SolverComparisonSmokeMain.run(
                tempDir,
                List.of(
                        error("highs"),
                        unavailable("clp"),
                        unavailable("glpk")));

        assertEquals(0, result.successfulSolvers());
        assertEquals(1, result.failedSolvers());
        assertEquals(2, result.unavailableSolvers());
        assertThrows(IllegalStateException.class, result::throwIfFailed);
    }

    @Test
    void validationFailuresAreReportedAsFailures() throws IOException {
        SolverComparisonSmokeMain.SmokeResult result = SolverComparisonSmokeMain.run(
                tempDir,
                List.of(
                        wrongObjective("highs"),
                        unavailable("clp"),
                        unavailable("glpk")));

        assertEquals(0, result.successfulSolvers());
        assertEquals(1, result.failedSolvers());
        assertEquals(2, result.unavailableSolvers());
        assertThrows(IllegalStateException.class, result::throwIfFailed);
    }

    private static Supplier<LpSolverAdapter> unavailable(final String name) {
        return () -> new StubAdapter(name, SolverStatus.UNSUPPORTED, OptionalDouble.empty(), "unavailable");
    }

    private static Supplier<LpSolverAdapter> optimal(final String name) {
        return () -> new StubAdapter(name, SolverStatus.OPTIMAL, OptionalDouble.of(0.0d), "");
    }

    private static Supplier<LpSolverAdapter> wrongObjective(final String name) {
        return () -> new StubAdapter(name, SolverStatus.OPTIMAL, OptionalDouble.of(1.0d), "");
    }

    private static Supplier<LpSolverAdapter> error(final String name) {
        return () -> new StubAdapter(name, SolverStatus.ERROR, OptionalDouble.empty(), "failed");
    }

    private record StubAdapter(
            String name,
            SolverStatus status,
            OptionalDouble objective,
            String message) implements LpSolverAdapter {
        @Override
        public SolverId id() {
            return new SolverId(name, "cli");
        }

        @Override
        public SolverRunResult solve(
                final SolverInput input,
                final SolverOptions options,
                final SolverWorkDirectory workDirectory) {
            return new SolverRunResult(id(), status, objective, new double[0], 0.0d, message);
        }
    }
}
