package io.github.mundanej.mlp.validation;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Independent expected evidence for result validation.
 *
 * @param status expected terminal status when known
 * @param objectiveValue expected objective value when known; compared with validation tolerances
 */
public record ExpectedValidationResult(
    Optional<ValidationStatus> status, OptionalDouble objectiveValue) {
  /**
   * Creates expected validation evidence.
   *
   * @param status expected terminal status when known
   * @param objectiveValue expected objective value when known
   */
  public ExpectedValidationResult {
    status = Objects.requireNonNull(status, "status");
    objectiveValue = Objects.requireNonNull(objectiveValue, "objectiveValue");
  }

  /**
   * Returns expected evidence for an optimal result.
   *
   * @param objectiveValue expected optimal objective value
   * @return expected evidence requiring an optimal status and matching objective value
   */
  public static ExpectedValidationResult optimal(final double objectiveValue) {
    return new ExpectedValidationResult(
        Optional.of(ValidationStatus.OPTIMAL), OptionalDouble.of(objectiveValue));
  }

  /**
   * Returns expected evidence for a terminal status without objective evidence.
   *
   * @param status expected terminal status
   * @return expected evidence that validates status only
   */
  public static ExpectedValidationResult statusOnly(final ValidationStatus status) {
    return new ExpectedValidationResult(Optional.of(status), OptionalDouble.empty());
  }
}
