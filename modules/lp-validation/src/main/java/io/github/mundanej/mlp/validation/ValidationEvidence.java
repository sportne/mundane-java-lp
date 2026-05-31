package io.github.mundanej.mlp.validation;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Solver evidence supplied to validation.
 *
 * @param status normalized solver status claim
 * @param objectiveValue reported objective value when available
 * @param primal primal vector in column order; empty means unavailable
 */
public record ValidationEvidence(
    Optional<ValidationStatus> status, OptionalDouble objectiveValue, double[] primal) {
  /**
   * Creates solver evidence.
   *
   * @param status normalized solver status claim
   * @param objectiveValue reported objective value when available
   * @param primal primal vector in column order; empty means unavailable
   */
  public ValidationEvidence {
    status = Objects.requireNonNull(status, "status");
    objectiveValue = Objects.requireNonNull(objectiveValue, "objectiveValue");
    primal = Objects.requireNonNull(primal, "primal").clone();
  }

  /**
   * Returns evidence with only a status claim.
   *
   * @param status normalized solver status claim
   */
  public static ValidationEvidence statusOnly(final ValidationStatus status) {
    return new ValidationEvidence(Optional.of(status), OptionalDouble.empty(), new double[0]);
  }

  /**
   * Returns optimal evidence with status, objective, and primal values.
   *
   * @param objectiveValue reported objective value
   * @param primal primal vector in column order
   */
  public static ValidationEvidence optimal(final double objectiveValue, final double... primal) {
    return new ValidationEvidence(
        Optional.of(ValidationStatus.OPTIMAL), OptionalDouble.of(objectiveValue), primal);
  }

  /** Returns true when primal evidence is available. */
  public boolean hasPrimal() {
    return primal.length > 0;
  }

  /** Returns a defensive copy of primal evidence. */
  @Override
  public double[] primal() {
    return primal.clone();
  }
}
