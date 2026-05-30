package io.github.mundanej.mlp.testkit;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.generators.CanonicalLpFixtures;
import io.github.mundanej.mlp.model.LpProblem;
import java.util.List;

/** Shared LP test instances. */
public final class LpTestInstances {
    private LpTestInstances() {
    }

    /** Returns a single-variable bounded LP. */
    public static LpProblem singleBoundedVariable() {
        return CanonicalLpFixtures.singleBoundedVariable().problem();
    }

    /** Returns all Tier 1 canonical LP fixtures. */
    public static List<CanonicalLpFixture> tierOneFixtures() {
        return CanonicalLpFixtures.tierOne();
    }
}
