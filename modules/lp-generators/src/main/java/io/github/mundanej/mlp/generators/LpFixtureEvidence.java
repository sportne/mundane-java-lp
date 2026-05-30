package io.github.mundanej.mlp.generators;

import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Hand-checked expected evidence for a canonical LP fixture.
 *
 * @param resultKind expected result kind
 * @param objectiveValue expected objective value when known
 * @param primal expected primal vector when known
 */
public record LpFixtureEvidence(
        ExpectedResultKind resultKind,
        OptionalDouble objectiveValue,
        double[] primal) {
    /**
     * Creates expected fixture evidence.
     *
     * @param resultKind expected result kind
     * @param objectiveValue expected objective value when known
     * @param primal expected primal vector when known
     */
    public LpFixtureEvidence {
        Objects.requireNonNull(resultKind, "resultKind");
        Objects.requireNonNull(objectiveValue, "objectiveValue");
        primal = Objects.requireNonNull(primal, "primal").clone();
        if (resultKind == ExpectedResultKind.OPTIMAL && objectiveValue.isEmpty()) {
            throw new IllegalArgumentException("optimal fixtures require an objective value");
        }
        if (resultKind != ExpectedResultKind.OPTIMAL && (!objectiveValue.isEmpty() || primal.length > 0)) {
            throw new IllegalArgumentException("non-optimal fixtures must not carry primal evidence");
        }
    }

    /** Returns true when primal evidence is available. */
    public boolean hasPrimal() {
        return primal.length > 0;
    }

    /** Returns a defensive copy of the expected primal vector. */
    @Override
    public double[] primal() {
        return primal.clone();
    }
}
