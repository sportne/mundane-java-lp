package io.github.mundanej.mlp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void evaluatesObjective() {
        LpObjective objective = new LpObjective(ObjectiveSense.MINIMIZE, 2.0d, new double[] {3.0d, 4.0d});
        assertEquals(17.0d, objective.evaluate(new double[] {1.0d, 3.0d}));
    }
}
