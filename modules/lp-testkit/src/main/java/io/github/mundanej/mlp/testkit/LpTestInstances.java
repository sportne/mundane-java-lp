package io.github.mundanej.mlp.testkit;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.generators.CanonicalLpFixtures;
import io.github.mundanej.mlp.generators.ExpectedResultKind;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.ValidationEvidence;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.util.List;
import java.util.Optional;

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

    /**
     * Returns a Tier 1 fixture by canonical problem name.
     *
     * @param name canonical problem name
     */
    public static CanonicalLpFixture tierOneFixture(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return tierOneFixtures().stream()
                .filter(fixture -> name.equals(fixture.problem().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown Tier 1 fixture " + name));
    }

    /**
     * Returns expected validation evidence for a canonical fixture.
     *
     * @param fixture canonical fixture
     */
    public static ExpectedValidationResult expectedValidationResult(final CanonicalLpFixture fixture) {
        return new ExpectedValidationResult(
                Optional.of(statusFor(fixture.evidence().resultKind())),
                fixture.evidence().objectiveValue());
    }

    /**
     * Returns solver-like validation evidence from canonical fixture evidence.
     *
     * @param fixture canonical fixture
     */
    public static ValidationEvidence validationEvidence(final CanonicalLpFixture fixture) {
        return new ValidationEvidence(
                Optional.of(statusFor(fixture.evidence().resultKind())),
                fixture.evidence().objectiveValue(),
                fixture.evidence().primal());
    }

    private static ValidationStatus statusFor(final ExpectedResultKind resultKind) {
        return switch (resultKind) {
            case OPTIMAL -> ValidationStatus.OPTIMAL;
            case INFEASIBLE -> ValidationStatus.INFEASIBLE;
            case UNBOUNDED -> ValidationStatus.UNBOUNDED;
        };
    }
}
