package io.github.mundanej.mlp.model;

/**
 * Immutable shape and density summary for an LP instance.
 *
 * @param rows row count
 * @param columns column count
 * @param nonzeros nonzero count
 */
public record LpProblemStats(int rows, int columns, long nonzeros) {
  /**
   * Creates a stats record.
   *
   * @param rows row count
   * @param columns column count
   * @param nonzeros nonzero count
   */
  public LpProblemStats {
    if (rows < 0) {
      throw new IllegalArgumentException("rows must be non-negative");
    }
    if (columns < 0) {
      throw new IllegalArgumentException("columns must be non-negative");
    }
    if (nonzeros < 0) {
      throw new IllegalArgumentException("nonzeros must be non-negative");
    }
  }

  /** Returns matrix density, or zero for an empty shape. */
  public double density() {
    if (rows == 0 || columns == 0) {
      return 0.0d;
    }
    return (double) nonzeros / ((double) rows * (double) columns);
  }
}
