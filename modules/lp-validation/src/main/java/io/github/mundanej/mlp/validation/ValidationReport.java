package io.github.mundanej.mlp.validation;

import java.util.List;

/**
 * Validation result for a solver output.
 *
 * @param toleranceProfile tolerance profile used for validation
 * @param findings validation findings; copied into an immutable list
 */
public record ValidationReport(
    ToleranceProfile toleranceProfile, List<ValidationFinding> findings) {
  /**
   * Creates a report.
   *
   * @param toleranceProfile tolerance profile used for validation
   * @param findings validation findings
   */
  public ValidationReport {
    if (toleranceProfile == null) {
      throw new IllegalArgumentException("toleranceProfile must not be null");
    }
    findings = List.copyOf(findings);
  }

  /** Returns true when no validation findings were recorded. */
  public boolean accepted() {
    return findings.isEmpty();
  }
}
