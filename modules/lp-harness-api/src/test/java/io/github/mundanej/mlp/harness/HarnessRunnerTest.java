package io.github.mundanej.mlp.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class HarnessRunnerTest {
    @TempDir
    private Path tempDir;

    @Test
    void recordsSuccessfulRun() {
        List<RunRecord> records = run(new FixedAdapter(SolverStatus.OPTIMAL, 0.0d));

        assertEquals(1, records.size());
        assertEquals(RunOutcome.SUCCESS, records.get(0).outcome());
        assertTrue(records.get(0).validationReport().accepted());
        assertTrue(Files.isDirectory(tempDir.resolve("0000-suite-one-variable-test")));
    }

    @Test
    void recordsValidationFailure() {
        RunRecord record = run(new FixedAdapter(SolverStatus.OPTIMAL, 1.0d)).get(0);

        assertEquals(RunOutcome.VALIDATION_FAILED, record.outcome());
        assertFalse(record.validationReport().accepted());
    }

    @Test
    void recordsAdapterError() {
        RunRecord record = run(new ThrowingAdapter()).get(0);

        assertEquals(RunOutcome.ADAPTER_ERROR, record.outcome());
        assertEquals(SolverStatus.ERROR, record.solverResult().status());
        assertFalse(record.failureMessage().isBlank());
    }

    @Test
    void recordsAdapterIdError() {
        RunRecord record = run(new ThrowingIdAdapter()).get(0);

        assertEquals(RunOutcome.ADAPTER_ERROR, record.outcome());
        assertEquals("unknown", record.solverResult().solverId().name());
        assertFalse(record.failureMessage().isBlank());
    }

    @Test
    void recordsReturnedErrorDiagnostic() {
        RunRecord record = run(new ErrorResultAdapter()).get(0);

        assertEquals(RunOutcome.ADAPTER_ERROR, record.outcome());
        assertEquals("solver failed", record.failureMessage());
    }

    @Test
    void recordsUnavailableSolver() {
        RunRecord record = run(new FixedAdapter(SolverStatus.UNSUPPORTED, Double.NaN)).get(0);

        assertEquals(RunOutcome.SOLVER_UNAVAILABLE, record.outcome());
    }

    @Test
    void recordsMalformedSolverResultAsValidationFailure() {
        RunRecord record = run(new FixedAdapter(SolverStatus.OPTIMAL, Double.NaN)).get(0);

        assertEquals(RunOutcome.VALIDATION_FAILED, record.outcome());
        assertFalse(record.validationReport().accepted());
    }

    @Test
    void validatesPrimalEvidenceFromSolverResult() {
        RunRecord record = run(new PrimalAdapter(new double[] {2.0d})).get(0);

        assertEquals(RunOutcome.VALIDATION_FAILED, record.outcome());
        assertTrue(record.validationReport().findings().stream()
                .anyMatch(finding -> "VARIABLE_UPPER_BOUND".equals(finding.code())));
    }

    private List<RunRecord> run(final LpSolverAdapter adapter) {
        return new HarnessRunner().run(
                new BenchmarkSuite("suite", List.of(instance())),
                List.of(adapter),
                new HarnessRunConfig(tempDir, SolverOptions.defaults(), ToleranceProfile.STANDARD));
    }

    private static BenchmarkInstance instance() {
        LpProblem problem = new LpProblem(
                "one-variable",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        return new BenchmarkInstance(
                "one-variable",
                problem,
                new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}),
                ExpectedValidationResult.optimal(0.0d));
    }

    private record FixedAdapter(SolverStatus status, double objective) implements LpSolverAdapter {
        @Override
        public SolverId id() {
            return new SolverId("test", "fixed");
        }

        @Override
        public SolverRunResult solve(
                final SolverInput input,
                final SolverOptions options,
                final SolverWorkDirectory workDirectory) {
            OptionalDouble objectiveValue = Double.isNaN(objective)
                    ? OptionalDouble.of(Double.NaN)
                    : OptionalDouble.of(objective);
            return new SolverRunResult(id(), status, objectiveValue, new double[0], 0.0d, "");
        }
    }

    private record PrimalAdapter(double[] primal) implements LpSolverAdapter {
        @Override
        public SolverId id() {
            return new SolverId("test", "primal");
        }

        @Override
        public SolverRunResult solve(
                final SolverInput input,
                final SolverOptions options,
                final SolverWorkDirectory workDirectory) {
            return new SolverRunResult(
                    id(),
                    SolverStatus.OPTIMAL,
                    OptionalDouble.of(0.0d),
                    primal,
                    0.0d,
                    "");
        }
    }

    private static final class ThrowingAdapter implements LpSolverAdapter {
        @Override
        public SolverId id() {
            return new SolverId("test", "throwing");
        }

        @Override
        public SolverRunResult solve(
                final SolverInput input,
                final SolverOptions options,
                final SolverWorkDirectory workDirectory) {
            throw new IllegalStateException("adapter failed");
        }
    }

    private static final class ThrowingIdAdapter implements LpSolverAdapter {
        @Override
        public SolverId id() {
            throw new IllegalStateException("id failed");
        }

        @Override
        public SolverRunResult solve(
                final SolverInput input,
                final SolverOptions options,
                final SolverWorkDirectory workDirectory) {
            throw new AssertionError("solve should not run");
        }
    }

    private static final class ErrorResultAdapter implements LpSolverAdapter {
        @Override
        public SolverId id() {
            return new SolverId("test", "error-result");
        }

        @Override
        public SolverRunResult solve(
                final SolverInput input,
                final SolverOptions options,
                final SolverWorkDirectory workDirectory) {
            return new SolverRunResult(
                    id(),
                    SolverStatus.ERROR,
                    OptionalDouble.empty(),
                    new double[0],
                    0.0d,
                    "solver failed");
        }
    }
}
