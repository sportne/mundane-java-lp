package io.github.mundanej.mlp.validation;

import java.util.List;

/** Validation result for a solver output. */
public record ValidationReport(ToleranceProfile toleranceProfile, List<ValidationFinding> findings) {
    /** Creates a report. */
    public ValidationReport {
        if (toleranceProfile == null) {
            throw new IllegalArgumentException("toleranceProfile must not be null");
        }
        findings = List.copyOf(findings);
    }

    /** Returns true when no findings were recorded. */
    public boolean accepted() {
        return findings.isEmpty();
    }
}
