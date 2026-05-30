package io.github.mundanej.mlp.validation;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpVariableBounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Checks variable bound violations for a primal solution. */
public final class BoundViolationChecker {
    /**
     * Checks bounds.
     *
     * @param problem LP problem
     * @param primal primal vector
     * @param profile tolerance profile
     */
    public ValidationReport check(
            final LpProblem problem,
            final double[] primal,
            final ToleranceProfile profile) {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(primal, "primal");
        Objects.requireNonNull(profile, "profile");
        if (primal.length != problem.variableBounds().size()) {
            throw new IllegalArgumentException("primal length does not match variable count");
        }
        List<ValidationFinding> findings = new ArrayList<>();
        for (int index = 0; index < primal.length; index++) {
            LpVariableBounds bounds = problem.variableBounds().get(index);
            double lowerViolation = bounds.lower() - primal[index];
            if (lowerViolation > profile.feasibilityTolerance()) {
                findings.add(new ValidationFinding(
                        "VARIABLE_LOWER_BOUND",
                        "variable " + index + " violates lower bound",
                        lowerViolation));
            }
            double upperViolation = primal[index] - bounds.upper();
            if (upperViolation > profile.feasibilityTolerance()) {
                findings.add(new ValidationFinding(
                        "VARIABLE_UPPER_BOUND",
                        "variable " + index + " violates upper bound",
                        upperViolation));
            }
        }
        return new ValidationReport(profile, findings);
    }
}
