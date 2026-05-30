package io.github.mundanej.mlp.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

final class LpSolutionValidatorTest {
    private final LpSolutionValidator validator = new LpSolutionValidator();

    @Test
    void rejectsVariableBoundViolation() {
        ValidationReport report = validateOneVariable(
                new double[] {1.1d},
                OptionalDouble.of(1.1d),
                ExpectedValidationResult.optimal(0.0d));

        assertFinding(report, "VARIABLE_UPPER_BOUND");
    }

    @Test
    void rejectsRowActivityViolation() {
        ValidationReport report = validateOneRow(
                new double[] {0.5d},
                OptionalDouble.of(0.5d),
                ExpectedValidationResult.optimal(0.5d));

        assertFinding(report, "ROW_LOWER_BOUND");
    }

    @Test
    void rejectsObjectiveMismatchesAndGaps() {
        ValidationReport report = validateOneVariable(
                new double[] {0.0d},
                OptionalDouble.of(0.5d),
                ExpectedValidationResult.optimal(0.0d));

        assertFinding(report, "OBJECTIVE_REPORTED_MISMATCH");
        assertFinding(report, "OBJECTIVE_ABSOLUTE_GAP");
        assertFinding(report, "OBJECTIVE_RELATIVE_GAP");
    }

    @Test
    void rejectsPrimalObjectiveMismatch() {
        ValidationReport report = validateOneVariable(
                new double[] {0.5d},
                OptionalDouble.empty(),
                ExpectedValidationResult.optimal(0.0d));

        assertFinding(report, "OBJECTIVE_PRIMAL_MISMATCH");
    }

    @Test
    void rejectsStatusMismatch() {
        ValidationReport report = validator.validate(
                oneVariableProblem(),
                emptyMatrix(0, 1),
                ExpectedValidationResult.statusOnly(ValidationStatus.INFEASIBLE),
                ValidationEvidence.statusOnly(ValidationStatus.OPTIMAL),
                ToleranceProfile.STANDARD);

        assertFinding(report, "STATUS_MISMATCH");
    }

    @Test
    void rejectsMissingStatusWhenStatusExpected() {
        ValidationReport report = validator.validate(
                oneVariableProblem(),
                emptyMatrix(0, 1),
                ExpectedValidationResult.statusOnly(ValidationStatus.INFEASIBLE),
                new ValidationEvidence(Optional.empty(), OptionalDouble.empty(), new double[0]),
                ToleranceProfile.STANDARD);

        assertFinding(report, "STATUS_MISSING");
    }

    @Test
    void rejectsNonFinitePrimalAndObjectiveEvidence() {
        ValidationReport report = validateOneVariable(
                new double[] {Double.NaN},
                OptionalDouble.of(Double.NaN),
                ExpectedValidationResult.optimal(0.0d));

        assertFinding(report, "NON_FINITE_PRIMAL");
        assertFinding(report, "NON_FINITE_OBJECTIVE");
    }

    @Test
    void acceptsWithinToleranceAndRejectsBeyondTolerance() {
        ValidationReport boundary = validateOneVariable(
                new double[] {1.0d + (ToleranceProfile.STANDARD.feasibilityTolerance() / 2.0d)},
                OptionalDouble.empty(),
                ExpectedValidationResult.statusOnly(ValidationStatus.OPTIMAL));
        ValidationReport beyond = validateOneVariable(
                new double[] {1.0d + (ToleranceProfile.STANDARD.feasibilityTolerance() * 2.0d)},
                OptionalDouble.empty(),
                ExpectedValidationResult.statusOnly(ValidationStatus.OPTIMAL));

        assertTrue(boundary.accepted());
        assertFalse(beyond.accepted());
    }

    @Test
    void rejectsShapeMismatch() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(
                oneVariableProblem(),
                emptyMatrix(1, 1),
                ExpectedValidationResult.optimal(0.0d),
                ValidationEvidence.optimal(0.0d, 0.0d),
                ToleranceProfile.STANDARD));
    }

    @Test
    void protectsEvidencePrimalDefensively() {
        double[] primal = {0.0d};
        ValidationEvidence evidence = ValidationEvidence.optimal(0.0d, primal);
        primal[0] = 1.0d;
        double[] copy = evidence.primal();
        copy[0] = 2.0d;

        assertEquals(0.0d, evidence.primal()[0]);
    }

    private ValidationReport validateOneVariable(
            final double[] primal,
            final OptionalDouble reportedObjective,
            final ExpectedValidationResult expected) {
        return validator.validate(
                oneVariableProblem(),
                emptyMatrix(0, 1),
                expected,
                new ValidationEvidence(Optional.of(ValidationStatus.OPTIMAL), reportedObjective, primal),
                ToleranceProfile.STANDARD);
    }

    private ValidationReport validateOneRow(
            final double[] primal,
            final OptionalDouble reportedObjective,
            final ExpectedValidationResult expected) {
        return validator.validate(
                oneRowProblem(),
                new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 1}),
                expected,
                new ValidationEvidence(Optional.of(ValidationStatus.OPTIMAL), reportedObjective, primal),
                ToleranceProfile.STANDARD);
    }

    private static LpProblem oneVariableProblem() {
        return new LpProblem(
                "one-variable",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 1, 0));
    }

    private static LpProblem oneRowProblem() {
        return new LpProblem(
                "one-row",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
                List.of(new LpVariableBounds(0.0d, 1.0d)),
                List.of(new LpRowBounds(1.0d, Double.POSITIVE_INFINITY)),
                new LpProblemStats(1, 1, 1));
    }

    private static CsrMatrix emptyMatrix(final int rows, final int columns) {
        return new CsrMatrix(rows, columns, new double[0], new int[0], new int[rows + 1]);
    }

    private static void assertFinding(final ValidationReport report, final String code) {
        assertTrue(report.findings().stream().anyMatch(finding -> code.equals(finding.code())),
                "expected finding " + code + " in " + report.findings());
    }
}
