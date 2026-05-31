package io.github.mundanej.mlp.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class LpProblemTest {
    @Test
    void exposesProblemFieldsAndImmutableBounds() {
        LpObjective objective = new LpObjective(ObjectiveSense.MAXIMIZE, 1.0d, new double[] {2.0d});
        LpVariableBounds variableBounds = new LpVariableBounds(0.0d, 3.0d);
        LpRowBounds rowBounds = new LpRowBounds(4.0d, 4.0d);
        LpProblemStats stats = new LpProblemStats(1, 1, 1);

        LpProblem problem = new LpProblem(
                "valid",
                objective,
                List.of(variableBounds),
                List.of(rowBounds),
                stats);

        assertEquals("valid", problem.name());
        assertSame(objective, problem.objective());
        assertEquals(List.of(variableBounds), problem.variableBounds());
        assertEquals(List.of(rowBounds), problem.rowBounds());
        assertSame(stats, problem.stats());
        assertThrows(UnsupportedOperationException.class,
                () -> problem.variableBounds().add(LpVariableBounds.FREE));
        assertThrows(UnsupportedOperationException.class,
                () -> problem.rowBounds().add(LpRowBounds.FREE));
    }

    @Test
    void rejectsObjectiveSizeMismatch() {
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d});
        assertThrows(IllegalArgumentException.class, () -> new LpProblem(
                "bad",
                objective,
                List.of(new LpVariableBounds(0.0d, 1.0d), new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 2, 0)));
    }

    @Test
    void rejectsBlankName() {
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[0]);
        assertThrows(IllegalArgumentException.class, () -> new LpProblem(
                " ",
                objective,
                List.of(),
                List.of(),
                new LpProblemStats(0, 0, 0)));
    }

    @Test
    void rejectsNullProblemFields() {
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[0]);
        assertThrows(NullPointerException.class, () -> new LpProblem(
                null,
                objective,
                List.of(),
                List.of(),
                new LpProblemStats(0, 0, 0)));
        assertThrows(NullPointerException.class, () -> new LpProblem(
                "null-objective",
                null,
                List.of(),
                List.of(),
                new LpProblemStats(0, 0, 0)));
        assertThrows(NullPointerException.class, () -> new LpProblem(
                "null-variable-bounds",
                objective,
                null,
                List.of(),
                new LpProblemStats(0, 0, 0)));
        assertThrows(NullPointerException.class, () -> new LpProblem(
                "null-row-bounds",
                objective,
                List.of(),
                null,
                new LpProblemStats(0, 0, 0)));
        assertThrows(NullPointerException.class, () -> new LpProblem(
                "null-stats",
                objective,
                List.of(),
                List.of(),
                null));
    }

    @Test
    void rejectsStatsShapeMismatch() {
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d});
        assertThrows(IllegalArgumentException.class, () -> new LpProblem(
                "bad-columns",
                objective,
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 2, 0)));
        assertThrows(IllegalArgumentException.class, () -> new LpProblem(
                "bad-rows",
                objective,
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(1, 1, 0)));
    }

    @Test
    void rejectsInvalidBounds() {
        assertThrows(IllegalArgumentException.class, () -> new LpVariableBounds(2.0d, 1.0d));
        assertThrows(IllegalArgumentException.class, () -> new LpRowBounds(2.0d, 1.0d));
    }

    @Test
    void rejectsInvalidStats() {
        assertThrows(IllegalArgumentException.class, () -> new LpProblemStats(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new LpProblemStats(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new LpProblemStats(0, 0, -1));
    }

    @Test
    void returnsDefensiveObjectiveCoefficientCopies() {
        double[] coefficients = {1.0d};
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, coefficients);
        coefficients[0] = 2.0d;
        double[] copy = objective.coefficients();
        copy[0] = 3.0d;
        assertEquals(1.0d, objective.coefficients()[0]);
        assertNotSame(copy, objective.coefficients());
    }

    @Test
    void evaluatesObjective() {
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 2.0d, new double[] {3.0d, 4.0d});
        assertEquals(17.0d, objective.evaluate(new double[] {1.0d, 3.0d}));
    }

    @Test
    void rejectsInvalidObjectiveInputs() {
        assertThrows(NullPointerException.class, () -> new LpObjective(null, 0.0d, new double[0]));
        assertThrows(NullPointerException.class, () -> new LpObjective(
                ObjectiveSense.MINIMIZE,
                0.0d,
                null));
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d});
        assertThrows(NullPointerException.class, () -> objective.evaluate(null));
        assertThrows(IllegalArgumentException.class, () -> objective.evaluate(new double[0]));
    }

    @Test
    void exposesObjectiveProperties() {
        LpObjective objective = new LpObjective(ObjectiveSense.MAXIMIZE, 5.0d, new double[] {1.0d, 2.0d});

        assertEquals(ObjectiveSense.MAXIMIZE, objective.sense());
        assertEquals(5.0d, objective.constant());
        assertEquals(2, objective.size());
        assertArrayEquals(new double[] {1.0d, 2.0d}, objective.coefficients());
        assertTrue(objective.toString().contains("MAXIMIZE"));
    }

    @Test
    void exposesBoundsAndStatsHelpers() {
        assertSame(LpVariableBounds.FREE, LpVariableBounds.FREE);
        assertSame(LpRowBounds.FREE, LpRowBounds.FREE);
        assertTrue(new LpRowBounds(2.0d, 2.0d).isEquality());
        assertFalse(new LpRowBounds(1.0d, 2.0d).isEquality());
        assertEquals(0.0d, new LpProblemStats(0, 10, 5).density());
        assertEquals(0.0d, new LpProblemStats(10, 0, 5).density());
        assertEquals(0.25d, new LpProblemStats(2, 2, 1).density());
    }
}
