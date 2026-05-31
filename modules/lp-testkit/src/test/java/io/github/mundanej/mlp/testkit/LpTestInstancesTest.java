package io.github.mundanej.mlp.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.validation.LpSolutionValidator;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationReport;
import io.github.mundanej.mlp.validation.ValidationStatus;
import org.junit.jupiter.api.Test;

final class LpTestInstancesTest {
    @Test
    void exposesTierOneFixtures() {
        assertEquals(10, LpTestInstances.tierOneFixtures().size());
    }

    @Test
    void exposesTierOneFixtureByName() {
        assertEquals("free-variable-row-bounded",
                LpTestInstances.tierOneFixture("free-variable-row-bounded").problem().name());
        assertThrows(IllegalArgumentException.class, () -> LpTestInstances.tierOneFixture("missing"));
    }

    @Test
    void exposesValidationEvidenceForTierOneFixtures() {
        CanonicalLpFixture fixture = LpTestInstances.tierOneFixtures().get(0);

        assertEquals(ValidationStatus.OPTIMAL,
                LpTestInstances.expectedValidationResult(fixture).status().orElseThrow());
        assertTrue(LpTestInstances.validationEvidence(fixture).hasPrimal());
    }

    @Test
    void validatesTierOneFixturesThroughTestkitEvidence() {
        LpSolutionValidator validator = new LpSolutionValidator();

        for (CanonicalLpFixture fixture : LpTestInstances.tierOneFixtures()) {
            ValidationReport report = validator.validate(
                    fixture.problem(),
                    fixture.matrix(),
                    LpTestInstances.expectedValidationResult(fixture),
                    LpTestInstances.validationEvidence(fixture),
                    ToleranceProfile.STRICT);
            assertTrue(report.accepted(), fixture.problem().name());
        }
    }
}
