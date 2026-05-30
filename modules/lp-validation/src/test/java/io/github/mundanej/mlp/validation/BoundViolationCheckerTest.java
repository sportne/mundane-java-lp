package io.github.mundanej.mlp.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import java.util.List;
import org.junit.jupiter.api.Test;

final class BoundViolationCheckerTest {
    @Test
    void acceptsInBoundsVector() {
        LpProblem problem = problem();
        ValidationReport report = new BoundViolationChecker().check(
                problem,
                new double[] {0.5d},
                ToleranceProfile.STANDARD);
        assertTrue(report.accepted());
    }

    @Test
    void rejectsOutOfBoundsVector() {
        LpProblem problem = problem();
        ValidationReport report = new BoundViolationChecker().check(
                problem,
                new double[] {2.0d},
                ToleranceProfile.STANDARD);
        assertFalse(report.accepted());
    }

    private static LpProblem problem() {
        return new LpProblem(
                "bounds",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
    }
}
