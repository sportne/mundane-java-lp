package io.github.mundanej.mlp.validation;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Validates solver evidence against model and fixture evidence. */
public final class LpSolutionValidator {
    /**
     * Validates solver evidence.
     *
     * @param problem canonical LP problem metadata
     * @param matrix row-by-column coefficient matrix
     * @param expected expected fixture evidence
     * @param evidence solver evidence
     * @param profile tolerance profile
     */
    public ValidationReport validate(
            final LpProblem problem,
            final CsrMatrix matrix,
            final ExpectedValidationResult expected,
            final ValidationEvidence evidence,
            final ToleranceProfile profile) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(matrix, "matrix");
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(profile, "profile");
        requireMatchingShape(problem, matrix);

        List<ValidationFinding> findings = new ArrayList<>();
        checkFiniteObjective(evidence, findings);
        checkStatus(expected, evidence, findings);
        if (evidence.hasPrimal()) {
            double[] primal = evidence.primal();
            if (primal.length != problem.variableBounds().size()) {
                throw new IllegalArgumentException("primal length does not match variable count");
            }
            checkFinitePrimal(primal, findings);
            checkVariableBounds(problem, primal, profile, findings);
            checkRowActivity(problem, matrix.multiply(primal), profile, findings);
            checkPrimalObjective(problem, expected, evidence, primal, profile, findings);
        }
        checkReportedObjective(expected, evidence, profile, findings);
        return new ValidationReport(profile, findings);
    }

    private static void requireMatchingShape(final LpProblem problem, final CsrMatrix matrix) {
        if (problem.stats().rows() != matrix.rows()) {
            throw new IllegalArgumentException("matrix row count must match problem row count");
        }
        if (problem.stats().columns() != matrix.columns()) {
            throw new IllegalArgumentException("matrix column count must match problem column count");
        }
    }

    private static void checkStatus(
            final ExpectedValidationResult expected,
            final ValidationEvidence evidence,
            final List<ValidationFinding> findings) {
        if (expected.status().isEmpty()) {
            return;
        }
        if (evidence.status().isEmpty()) {
            findings.add(new ValidationFinding(
                    "STATUS_MISSING",
                    "normalized status is missing",
                    1.0d));
            return;
        }
        if (expected.status().get() != evidence.status().get()) {
            findings.add(new ValidationFinding(
                    "STATUS_MISMATCH",
                    "normalized status does not match expected status",
                    1.0d));
        }
    }

    private static void checkVariableBounds(
            final LpProblem problem,
            final double[] primal,
            final ToleranceProfile profile,
            final List<ValidationFinding> findings) {
        for (int index = 0; index < primal.length; index++) {
            LpVariableBounds bounds = problem.variableBounds().get(index);
            addIfViolation(findings, "VARIABLE_LOWER_BOUND",
                    "variable " + index + " violates lower bound",
                    bounds.lower() - primal[index], profile);
            addIfViolation(findings, "VARIABLE_UPPER_BOUND",
                    "variable " + index + " violates upper bound",
                    primal[index] - bounds.upper(), profile);
        }
    }

    private static void checkRowActivity(
            final LpProblem problem,
            final double[] activities,
            final ToleranceProfile profile,
            final List<ValidationFinding> findings) {
        for (int index = 0; index < activities.length; index++) {
            LpRowBounds bounds = problem.rowBounds().get(index);
            addIfViolation(findings, "ROW_LOWER_BOUND",
                    "row " + index + " violates lower bound",
                    bounds.lower() - activities[index], profile);
            addIfViolation(findings, "ROW_UPPER_BOUND",
                    "row " + index + " violates upper bound",
                    activities[index] - bounds.upper(), profile);
        }
    }

    private static void checkFinitePrimal(
            final double[] primal,
            final List<ValidationFinding> findings) {
        for (int index = 0; index < primal.length; index++) {
            if (!Double.isFinite(primal[index])) {
                findings.add(new ValidationFinding(
                        "NON_FINITE_PRIMAL",
                        "primal value " + index + " is not finite",
                        Double.POSITIVE_INFINITY));
            }
        }
    }

    private static void checkFiniteObjective(
            final ValidationEvidence evidence,
            final List<ValidationFinding> findings) {
        if (evidence.objectiveValue().isPresent()
                && !Double.isFinite(evidence.objectiveValue().getAsDouble())) {
            findings.add(new ValidationFinding(
                    "NON_FINITE_OBJECTIVE",
                    "reported objective is not finite",
                    Double.POSITIVE_INFINITY));
        }
    }

    private static void checkPrimalObjective(
            final LpProblem problem,
            final ExpectedValidationResult expected,
            final ValidationEvidence evidence,
            final double[] primal,
            final ToleranceProfile profile,
            final List<ValidationFinding> findings) {
        double evaluated = problem.objective().evaluate(primal);
        if (!Double.isFinite(evaluated)) {
            findings.add(new ValidationFinding(
                    "NON_FINITE_OBJECTIVE",
                    "objective evaluated from primal is not finite",
                    Double.POSITIVE_INFINITY));
            return;
        }
        if (expected.objectiveValue().isPresent()) {
            addIfViolation(findings, "OBJECTIVE_PRIMAL_MISMATCH",
                    "objective evaluated from primal does not match expected objective",
                    Math.abs(evaluated - expected.objectiveValue().getAsDouble()), profile);
        }
        if (evidence.objectiveValue().isPresent()) {
            addIfViolation(findings, "OBJECTIVE_REPORTED_MISMATCH",
                    "reported objective does not match objective evaluated from primal",
                    Math.abs(evaluated - evidence.objectiveValue().getAsDouble()), profile);
        }
    }

    private static void checkReportedObjective(
            final ExpectedValidationResult expected,
            final ValidationEvidence evidence,
            final ToleranceProfile profile,
            final List<ValidationFinding> findings) {
        if (expected.objectiveValue().isEmpty() || evidence.objectiveValue().isEmpty()) {
            return;
        }
        double expectedValue = expected.objectiveValue().getAsDouble();
        double actualValue = evidence.objectiveValue().getAsDouble();
        double absoluteGap = Math.abs(actualValue - expectedValue);
        addIfViolation(findings, "OBJECTIVE_ABSOLUTE_GAP",
                "reported objective absolute gap exceeds tolerance",
                absoluteGap, profile);
        double relativeGap = absoluteGap / Math.max(1.0d, Math.abs(expectedValue));
        addIfViolation(findings, "OBJECTIVE_RELATIVE_GAP",
                "reported objective relative gap exceeds tolerance",
                relativeGap, profile);
    }

    private static void addIfViolation(
            final List<ValidationFinding> findings,
            final String code,
            final String message,
            final double magnitude,
            final ToleranceProfile profile) {
        if (magnitude > profile.feasibilityTolerance()) {
            findings.add(new ValidationFinding(code, message, magnitude));
        }
    }
}
