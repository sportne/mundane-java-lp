package io.github.mundanej.mlp.sparse;

import java.util.Objects;

/** Compressed sparse column matrix backed by copied primitive arrays. */
public final class CscMatrix {
  private final int rows;
  private final int columns;
  private final double[] values;
  private final int[] rowIndices;
  private final int[] columnPointers;

  /**
   * Creates a CSC matrix.
   *
   * @param rows row count
   * @param columns column count
   * @param values nonzero values in column-major CSC order; defensively copied
   * @param rowIndices row index for each nonzero; defensively copied
   * @param columnPointers column start offsets with length {@code columns + 1}; defensively copied
   */
  public CscMatrix(
      final int rows,
      final int columns,
      final double[] values,
      final int[] rowIndices,
      final int[] columnPointers) {
    if (rows < 0 || columns < 0) {
      throw new IllegalArgumentException("matrix dimensions must be non-negative");
    }
    this.rows = rows;
    this.columns = columns;
    this.values = Objects.requireNonNull(values, "values").clone();
    this.rowIndices = Objects.requireNonNull(rowIndices, "rowIndices").clone();
    this.columnPointers = Objects.requireNonNull(columnPointers, "columnPointers").clone();
    validate();
  }

  /** Returns row count. */
  public int rows() {
    return rows;
  }

  /** Returns column count. */
  public int columns() {
    return columns;
  }

  /** Returns nonzero count. */
  public int nonzeros() {
    return values.length;
  }

  /** Returns a defensive copy of nonzero values. */
  public double[] values() {
    return values.clone();
  }

  /** Returns a defensive copy of row indices. */
  public int[] rowIndices() {
    return rowIndices.clone();
  }

  /** Returns a defensive copy of column pointers. */
  public int[] columnPointers() {
    return columnPointers.clone();
  }

  /**
   * Computes y = A x.
   *
   * @param x dense input vector whose length equals the matrix column count
   * @return newly allocated dense row-activity vector
   */
  public double[] multiply(final double[] x) {
    Objects.requireNonNull(x, "x");
    if (x.length != columns) {
      throw new IllegalArgumentException("vector length must match matrix columns");
    }
    double[] y = new double[rows];
    for (int column = 0; column < columns; column++) {
      double xValue = x[column];
      int start = columnPointers[column];
      int end = columnPointers[column + 1];
      for (int offset = start; offset < end; offset++) {
        y[rowIndices[offset]] += values[offset] * xValue;
      }
    }
    return y;
  }

  private void validate() {
    if (values.length != rowIndices.length) {
      throw new IllegalArgumentException("values and row indices length mismatch");
    }
    if (columnPointers.length != columns + 1) {
      throw new IllegalArgumentException("column pointer length must equal columns + 1");
    }
    if (columnPointers[0] != 0) {
      throw new IllegalArgumentException("first column pointer must be zero");
    }
    int previous = 0;
    for (int pointer : columnPointers) {
      if (pointer < previous) {
        throw new IllegalArgumentException("column pointers must be monotonic");
      }
      previous = pointer;
    }
    if (columnPointers[columnPointers.length - 1] != values.length) {
      throw new IllegalArgumentException("last column pointer must equal values length");
    }
    for (int rowIndex : rowIndices) {
      if (rowIndex < 0 || rowIndex >= rows) {
        throw new IllegalArgumentException("row index out of range");
      }
    }
  }
}
