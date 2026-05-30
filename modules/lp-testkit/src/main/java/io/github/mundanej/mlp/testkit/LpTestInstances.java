package io.github.mundanej.mlp.testkit;

import io.github.mundanej.mlp.generators.TinyLpGenerator;
import io.github.mundanej.mlp.model.LpProblem;

/** Shared LP test instances. */
public final class LpTestInstances {
    private LpTestInstances() {
    }

    /** Returns a single-variable bounded LP. */
    public static LpProblem singleBoundedVariable() {
        return new TinyLpGenerator().singleBoundedVariable();
    }
}
