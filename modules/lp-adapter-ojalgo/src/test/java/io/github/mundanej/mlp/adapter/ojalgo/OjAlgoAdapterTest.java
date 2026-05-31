package io.github.mundanej.mlp.adapter.ojalgo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ojalgo.optimisation.Optimisation;

final class OjAlgoAdapterTest {
    @TempDir
    private Path tempDir;

    @Test
    void hasExpectedId() {
        assertEquals("ojalgo", new OjAlgoAdapter().id().name());
        assertEquals("java", new OjAlgoAdapter().id().mode());
    }

    @Test
    void normalizesResultStatuses() {
        assertEquals(SolverStatus.OPTIMAL, OjAlgoAdapter.normalize(Optimisation.State.OPTIMAL));
        assertEquals(SolverStatus.FEASIBLE, OjAlgoAdapter.normalize(Optimisation.State.FEASIBLE));
        assertEquals(SolverStatus.FEASIBLE, OjAlgoAdapter.normalize(Optimisation.State.APPROXIMATE));
        assertEquals(SolverStatus.INFEASIBLE, OjAlgoAdapter.normalize(Optimisation.State.INFEASIBLE));
        assertEquals(SolverStatus.UNBOUNDED, OjAlgoAdapter.normalize(Optimisation.State.UNBOUNDED));
        assertEquals(SolverStatus.NUMERICAL_FAILURE, OjAlgoAdapter.normalize(Optimisation.State.FAILED));
        assertEquals(SolverStatus.ERROR, OjAlgoAdapter.normalize(Optimisation.State.INVALID));
        assertEquals(SolverStatus.OPTIMAL, OjAlgoAdapter.normalize(Optimisation.State.DISTINCT));
        assertEquals(SolverStatus.UNKNOWN, OjAlgoAdapter.normalize(Optimisation.State.UNEXPLORED));
        assertEquals(SolverStatus.UNKNOWN, OjAlgoAdapter.normalize(Optimisation.State.VALID));
    }

    @Test
    void formatsRuntimeDiagnosticMessages() {
        assertEquals("IllegalStateException", OjAlgoAdapter.message(new IllegalStateException()));
        assertEquals("boom", OjAlgoAdapter.message(new IllegalStateException("boom")));
    }

    @Test
    void solvesTinyLp() {
        SolverRunResult result = new OjAlgoAdapter().solve(
                tinyInput(1.0d),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.OPTIMAL, result.status());
        assertEquals(0.0d, result.objectiveValue().orElseThrow(), 1.0e-7);
        assertArrayEquals(new double[] {0.0d}, result.primalValues(), 1.0e-7);
    }

    @Test
    void acceptsZeroVariableOptimalLp() {
        LpProblem problem = new LpProblem(
                "zero-variable",
                new LpObjective(ObjectiveSense.MINIMIZE, 2.0d, new double[0]),
                List.of(),
                List.of(),
                new LpProblemStats(0, 0, 0));
        SolverRunResult result = new OjAlgoAdapter().solve(
                SolverInput.withGeneratedNames(
                        problem,
                        new CsrMatrix(0, 0, new double[0], new int[0], new int[] {0})),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.OPTIMAL, result.status());
        assertEquals(2.0d, result.objectiveValue().orElseThrow(), 1.0e-7);
        assertArrayEquals(new double[0], result.primalValues());
    }

    @Test
    void solvesMatrixBackedLp() {
        SolverRunResult result = new OjAlgoAdapter().solve(
                matrixBackedInput(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.OPTIMAL, result.status());
        assertEquals(11.0d, result.objectiveValue().orElseThrow(), 1.0e-7);
        assertArrayEquals(new double[] {4.0d, 0.0d}, result.primalValues(), 1.0e-7);
    }

    @Test
    void rejectsNullSolveInputs() {
        OjAlgoAdapter adapter = new OjAlgoAdapter();

        assertThrows(NullPointerException.class,
                () -> adapter.solve(null, SolverOptions.defaults(), new SolverWorkDirectory(tempDir)));
        assertThrows(NullPointerException.class,
                () -> adapter.solve(tinyInput(1.0d), null, new SolverWorkDirectory(tempDir)));
        assertThrows(NullPointerException.class,
                () -> adapter.solve(tinyInput(1.0d), SolverOptions.defaults(), null));
    }

    @Test
    void reportsUnsupportedNonFiniteModelData() {
        SolverRunResult result = new OjAlgoAdapter().solve(
                tinyInput(Double.NaN),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("objective coefficient must be finite"));
    }

    @Test
    void reportsUnsupportedNonFiniteObjectiveConstant() {
        SolverRunResult result = new OjAlgoAdapter().solve(
                objectiveConstantInput(Double.NaN),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("objective constant must be finite"));
    }

    @Test
    void skipsFreeRowsWhenBuildingModel() {
        SolverRunResult result = new OjAlgoAdapter().solve(
                freeRowInput(),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.OPTIMAL, result.status());
    }

    @Test
    void rejectsUnsupportedInfiniteVariableBoundPolarity() {
        LpProblem problem = new LpProblem(
                "bad-variable-bound",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {0.0d}),
                List.of(new LpVariableBounds(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        SolverRunResult result = new OjAlgoAdapter().solve(
                SolverInput.withGeneratedNames(
                        problem,
                        new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0})),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("lower bound must not be positive infinity"));
    }

    @Test
    void rejectsUnsupportedNaNBounds() {
        LpProblem problem = new LpProblem(
                "nan-bound",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {0.0d}),
                List.of(new LpVariableBounds(Double.NaN, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        SolverRunResult result = new OjAlgoAdapter().solve(
                SolverInput.withGeneratedNames(
                        problem,
                        new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0})),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("bound must not be NaN"));
    }

    @Test
    void rejectsUnsupportedInfiniteRowBoundPolarity() {
        LpProblem problem = new LpProblem(
                "bad-row-bound",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {0.0d}),
                List.of(LpVariableBounds.FREE),
                List.of(new LpRowBounds(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)),
                new LpProblemStats(1, 1, 1));
        SolverRunResult result = new OjAlgoAdapter().solve(
                new SolverInput(
                        problem,
                        new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 1}),
                        List.of("bad-row"),
                        List.of("x"),
                        "OBJ"),
                SolverOptions.defaults(),
                new SolverWorkDirectory(tempDir));

        assertEquals(SolverStatus.UNSUPPORTED, result.status());
        assertTrue(result.message().contains("upper bound must not be negative infinity"));
    }

    @Nested
    final class TerminalStatusTests {
        @Test
        void detectsInfeasibleProblem() {
            SolverRunResult result = new OjAlgoAdapter().solve(
                    infeasibleInput(),
                    SolverOptions.defaults(),
                    new SolverWorkDirectory(tempDir));

            assertEquals(SolverStatus.INFEASIBLE, result.status());
        }
    }

    private static SolverInput tinyInput(final double objectiveCoefficient) {
        LpProblem problem = new LpProblem(
                "tiny",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {objectiveCoefficient}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        return SolverInput.withGeneratedNames(
                problem,
                new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
    }

    private static SolverInput matrixBackedInput() {
        LpProblem problem = new LpProblem(
                "matrix-backed",
                new LpObjective(ObjectiveSense.MAXIMIZE, 3.0d, new double[] {2.0d, 1.0d}),
                List.of(
                        new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY),
                        new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
                List.of(
                        new LpRowBounds(Double.NEGATIVE_INFINITY, 4.0d),
                        new LpRowBounds(Double.NEGATIVE_INFINITY, 2.0d)),
                new LpProblemStats(2, 2, 3));
        return new SolverInput(
                problem,
                new CsrMatrix(
                        2,
                        2,
                        new double[] {1.0d, 1.0d, 1.0d},
                        new int[] {0, 1, 1},
                        new int[] {0, 2, 3}),
                List.of("capacity", "y-limit"),
                List.of("x", "y"),
                "OBJ");
    }

    private static SolverInput objectiveConstantInput(final double objectiveConstant) {
        LpProblem problem = new LpProblem(
                "objective-constant",
                new LpObjective(ObjectiveSense.MINIMIZE, objectiveConstant, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
        return SolverInput.withGeneratedNames(
                problem,
                new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
    }

    private static SolverInput freeRowInput() {
        LpProblem problem = new LpProblem(
                "free-row",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(LpRowBounds.FREE),
                new LpProblemStats(1, 1, 0));
        return new SolverInput(
                problem,
                new CsrMatrix(1, 1, new double[0], new int[0], new int[] {0, 0}),
                List.of("free"),
                List.of("x"),
                "OBJ");
    }

    private static SolverInput infeasibleInput() {
        LpProblem problem = new LpProblem(
                "infeasible",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {0.0d}),
                List.of(LpVariableBounds.FREE),
                List.of(
                        new LpRowBounds(1.0d, Double.POSITIVE_INFINITY),
                        new LpRowBounds(Double.NEGATIVE_INFINITY, 0.0d)),
                new LpProblemStats(2, 1, 2));
        return new SolverInput(
                problem,
                new CsrMatrix(
                        2,
                        1,
                        new double[] {1.0d, 1.0d},
                        new int[] {0, 0},
                        new int[] {0, 1, 2}),
                List.of("lower", "upper"),
                List.of("x"),
                "OBJ");
    }
}
