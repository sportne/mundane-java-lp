package io.github.mundanej.mlp.model;

/**
 * Lower and upper bounds for a row activity.
 *
 * @param lower lower activity bound
 * @param upper upper activity bound
 */
public record LpRowBounds(double lower, double upper) {
  /** A free row. */
  public static final LpRowBounds FREE =
      new LpRowBounds(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

  /**
   * Creates row bounds.
   *
   * @param lower lower activity bound
   * @param upper upper activity bound
   * @throws IllegalArgumentException if lower is greater than upper
   */
  public LpRowBounds {
    if (lower > upper) {
      throw new IllegalArgumentException("row lower bound exceeds row upper bound");
    }
  }

  /** Returns true when the row is equality-constrained. */
  public boolean isEquality() {
    return Double.compare(lower, upper) == 0;
  }
}
