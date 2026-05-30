package io.github.mundanej.mlp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

final class LpProblemTest {
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
}
