package io.github.mundanej.mlp.model;

/**
 * Lower and upper bounds for a single LP variable.
 *
 * @param lower lower variable bound
 * @param upper upper variable bound
 */
public record LpVariableBounds(double lower, double upper) {
  /** A free variable bound. */
  public static final LpVariableBounds FREE =
      new LpVariableBounds(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

  /**
   * Creates bounds.
   *
   * @param lower lower variable bound
   * @param upper upper variable bound
   * @throws IllegalArgumentException if lower is greater than upper
   */
  public LpVariableBounds {
    if (lower > upper) {
      throw new IllegalArgumentException("lower bound exceeds upper bound");
    }
  }
}
