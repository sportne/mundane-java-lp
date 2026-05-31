package io.github.mundanej.mlp.sparse;

/**
 * Shape and nonzero summary for a sparse matrix.
 *
 * @param rows row count
 * @param columns column count
 * @param nonzeros nonzero count
 */
public record SparseMatrixStats(int rows, int columns, int nonzeros) {
  /**
   * Creates sparse matrix stats.
   *
   * @param rows row count
   * @param columns column count
   * @param nonzeros nonzero count
   */
  public SparseMatrixStats {
    if (rows < 0 || columns < 0 || nonzeros < 0) {
      throw new IllegalArgumentException("shape and nonzeros must be non-negative");
    }
  }
}
