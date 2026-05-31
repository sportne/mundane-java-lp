package io.github.mundanej.mlp.solver.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import org.junit.jupiter.api.Test;

final class SolverInputTest {
    @Test
    void createsInputWithGeneratedNames() {
        SolverInput input = SolverInput.withGeneratedNames(problem(), matrix());

        assertEquals(List.of(), input.rowNames());
        assertEquals(List.of("X0"), input.columnNames());
        assertEquals("OBJ", input.objectiveRowName());
    }

    @Test
    void rejectsShapeAndNameCount() {
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                problem(),
                new CsrMatrix(1, 1, new double[0], new int[0], new int[] {0, 0}),
                List.of("r"),
                List.of("x"),
                "OBJ"));
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                problem(),
                matrix(),
                List.of(),
                List.of("x", "x"),
                "OBJ"));
    }

    @Test
    void rejectsNonzeroMismatchAndDuplicateNames() {
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                new LpProblem(
                        "tiny",
                        new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                        List.of(new LpVariableBounds(0.0d, 1.0d)),
                        List.of(),
                        new LpProblemStats(0, 1, 1)),
                matrix(),
                List.of(),
                List.of("x"),
                "OBJ"));
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                twoColumnProblem(),
                new CsrMatrix(0, 2, new double[0], new int[0], new int[] {0}),
                List.of(),
                List.of("x", "x"),
                "OBJ"));
    }

    private static LpProblem problem() {
        return new LpProblem(
                "tiny",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
    }

    private static CsrMatrix matrix() {
        return new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0});
    }

    private static LpProblem twoColumnProblem() {
        return new LpProblem(
                "tiny-two",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d, 2.0d}),
                List.of(
                        new LpVariableBounds(0.0d, 1.0d),
                        new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 2, 0));
    }
}
