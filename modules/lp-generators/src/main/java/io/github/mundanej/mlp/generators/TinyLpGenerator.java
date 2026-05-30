package io.github.mundanej.mlp.generators;

import io.github.mundanej.mlp.model.LpProblem;

/** Tiny hand-checkable LP fixtures. */
public final class TinyLpGenerator {
    /** Returns min x with {@code 0 <= x <= 1}, optimum x = 0. */
    public LpProblem singleBoundedVariable() {
        return CanonicalLpFixtures.singleBoundedVariable().problem();
    }
}
