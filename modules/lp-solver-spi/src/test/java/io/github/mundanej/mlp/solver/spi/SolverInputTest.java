package io.github.mundanej.mlp.solver.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
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
    void copiesExplicitNamesDefensively() {
        List<String> rows = List.of("row");
        List<String> columns = List.of("x");
        SolverInput input = new SolverInput(
                oneRowProblem(),
                oneEntryMatrix(),
                rows,
                columns,
                "OBJ");

        assertEquals(rows, input.rowNames());
        assertEquals(columns, input.columnNames());
        assertThrows(UnsupportedOperationException.class, () -> input.rowNames().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> input.columnNames().add("other"));
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
    void rejectsNullInputs() {
        assertThrows(NullPointerException.class, () -> SolverInput.withGeneratedNames(null, matrix()));
        assertThrows(NullPointerException.class, () -> new SolverInput(
                null,
                matrix(),
                List.of(),
                List.of("x"),
                "OBJ"));
        assertThrows(NullPointerException.class, () -> new SolverInput(
                problem(),
                null,
                List.of(),
                List.of("x"),
                "OBJ"));
        assertThrows(NullPointerException.class, () -> new SolverInput(
                problem(),
                matrix(),
                null,
                List.of("x"),
                "OBJ"));
        assertThrows(NullPointerException.class, () -> new SolverInput(
                problem(),
                matrix(),
                List.of(),
                null,
                "OBJ"));
        assertThrows(NullPointerException.class, () -> new SolverInput(
                problem(),
                matrix(),
                List.of(),
                List.of("x"),
                null));
    }

    @Test
    void rejectsBlankAndDuplicateNames() {
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                oneRowProblem(),
                oneEntryMatrix(),
                List.of(" "),
                List.of("x"),
                "OBJ"));
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                problem(),
                matrix(),
                List.of(),
                List.of(" "),
                "OBJ"));
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                problem(),
                matrix(),
                List.of(),
                List.of("x"),
                " "));
        assertThrows(IllegalArgumentException.class, () -> new SolverInput(
                twoRowProblem(),
                twoRowMatrix(),
                List.of("r", "r"),
                List.of("x"),
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

    @Test
    void exposesSolverIdAndWorkDirectory() {
        SolverId id = new SolverId("solver", "cli");
        SolverWorkDirectory workDirectory = new SolverWorkDirectory(java.nio.file.Path.of("work"));

        assertEquals("solver", id.name());
        assertEquals("cli", id.mode());
        assertEquals(java.nio.file.Path.of("work"), workDirectory.path());
        assertThrows(IllegalArgumentException.class, () -> new SolverId(null, "cli"));
        assertThrows(IllegalArgumentException.class, () -> new SolverId(" ", "cli"));
        assertThrows(IllegalArgumentException.class, () -> new SolverId("solver", null));
        assertThrows(IllegalArgumentException.class, () -> new SolverId("solver", " "));
        assertThrows(IllegalArgumentException.class, () -> new SolverWorkDirectory(null));
        assertSame(SolverStatus.OPTIMAL, SolverStatus.valueOf("OPTIMAL"));
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

    private static LpProblem oneRowProblem() {
        return new LpProblem(
                "one-row",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(new LpRowBounds(2.0d, 2.0d)),
                new LpProblemStats(1, 1, 1));
    }

    private static CsrMatrix oneEntryMatrix() {
        return new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 1});
    }

    private static LpProblem twoRowProblem() {
        return new LpProblem(
                "two-row",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(new LpRowBounds(1.0d, 1.0d), new LpRowBounds(2.0d, 2.0d)),
                new LpProblemStats(2, 1, 2));
    }

    private static CsrMatrix twoRowMatrix() {
        return new CsrMatrix(2, 1, new double[] {1.0d, 1.0d}, new int[] {0, 0}, new int[] {0, 1, 2});
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
