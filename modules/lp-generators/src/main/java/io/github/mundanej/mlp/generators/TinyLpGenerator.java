package io.github.mundanej.mlp.generators;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import java.util.List;

/** Tiny hand-checkable LP fixtures. */
public final class TinyLpGenerator {
    /** Returns min x with 0 <= x <= 1, optimum x = 0. */
    public LpProblem singleBoundedVariable() {
        return new LpProblem(
                "single-bounded-variable",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
    }
}
