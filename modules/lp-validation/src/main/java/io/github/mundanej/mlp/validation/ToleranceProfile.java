package io.github.mundanej.mlp.validation;

/** Named numerical tolerance profile. */
public enum ToleranceProfile {
  /** Exploratory or first-order tolerance. */
  LOOSE(1.0e-4),
  /** Default regression tolerance. */
  STANDARD(1.0e-7),
  /** Small-instance strict tolerance. */
  STRICT(1.0e-9);

  private final double feasibilityTolerance;

  ToleranceProfile(final double feasibilityTolerance) {
    this.feasibilityTolerance = feasibilityTolerance;
  }

  /** Returns the feasibility tolerance. */
  public double feasibilityTolerance() {
    return feasibilityTolerance;
  }
}
