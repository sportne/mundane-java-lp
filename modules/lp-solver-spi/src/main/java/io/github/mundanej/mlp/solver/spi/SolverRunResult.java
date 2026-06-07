package io.github.mundanej.mlp.solver.spi;

import java.util.OptionalDouble;

/**
 * Normalized solver run result.
 *
 * @param solverId solver identifier
 * @param status normalized solver status
 * @param objectiveValue optional objective value
 * @param primalValues primal variable values in column order when available; defensively copied
 * @param elapsedSeconds reported solve time in seconds
 * @param message adapter diagnostic message; null is normalized to blank
 */
public record SolverRunResult(
    SolverId solverId,
    SolverStatus status,
    OptionalDouble objectiveValue,
    double[] primalValues,
    double elapsedSeconds,
    String message) {
  /**
   * Creates a run result.
   *
   * @param solverId solver identifier
   * @param status normalized solver status
   * @param objectiveValue optional objective value
   * @param primalValues primal variable values in column order when available
   * @param elapsedSeconds reported solve time in seconds
   * @param message adapter diagnostic message; null is normalized to blank
   */
  public SolverRunResult {
    if (solverId == null) {
      throw new IllegalArgumentException("solverId must not be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("status must not be null");
    }
    if (objectiveValue == null) {
      throw new IllegalArgumentException("objectiveValue must not be null");
    }
    if (primalValues == null) {
      throw new IllegalArgumentException("primalValues must not be null");
    }
    primalValues = primalValues.clone();
    for (double value : primalValues) {
      if (!Double.isFinite(value)) {
        throw new IllegalArgumentException("primalValues must be finite");
      }
    }
    if (elapsedSeconds < 0.0d) {
      throw new IllegalArgumentException("elapsedSeconds must be non-negative");
    }
    if (message == null) {
      message = "";
    }
  }

  /** Returns a defensive copy of primal evidence in column order. */
  @Override
  public double[] primalValues() {
    return primalValues.clone();
  }
}
