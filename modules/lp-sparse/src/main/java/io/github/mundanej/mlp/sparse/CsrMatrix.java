package io.github.mundanej.mlp.sparse;

import java.util.Objects;

/** Compressed sparse row matrix backed by primitive arrays. */
public final class CsrMatrix {
    private final int rows;
    private final int columns;
    private final double[] values;
    private final int[] columnIndices;
    private final int[] rowPointers;

    /**
     * Creates a CSR matrix.
     *
     * @param rows row count
     * @param columns column count
     * @param values nonzero values
     * @param columnIndices column index for each nonzero
     * @param rowPointers start offset for each row
     */
    public CsrMatrix(
            final int rows,
            final int columns,
            final double[] values,
            final int[] columnIndices,
            final int[] rowPointers) {
        if (rows < 0 || columns < 0) {
            throw new IllegalArgumentException("matrix dimensions must be non-negative");
        }
        this.rows = rows;
        this.columns = columns;
        this.values = Objects.requireNonNull(values, "values").clone();
        this.columnIndices = Objects.requireNonNull(columnIndices, "columnIndices").clone();
        this.rowPointers = Objects.requireNonNull(rowPointers, "rowPointers").clone();
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

    /** Returns a defensive copy of column indices. */
    public int[] columnIndices() {
        return columnIndices.clone();
    }

    /** Returns a defensive copy of row pointers. */
    public int[] rowPointers() {
        return rowPointers.clone();
    }

    /**
     * Computes y = A x.
     *
     * @param x dense input vector
     */
    public double[] multiply(final double[] x) {
        Objects.requireNonNull(x, "x");
        if (x.length != columns) {
            throw new IllegalArgumentException("vector length must match matrix columns");
        }
        double[] y = new double[rows];
        for (int row = 0; row < rows; row++) {
            int start = rowPointers[row];
            int end = rowPointers[row + 1];
            double sum = 0.0d;
            for (int offset = start; offset < end; offset++) {
                sum += values[offset] * x[columnIndices[offset]];
            }
            y[row] = sum;
        }
        return y;
    }

    private void validate() {
        if (values.length != columnIndices.length) {
            throw new IllegalArgumentException("values and column indices length mismatch");
        }
        if (rowPointers.length != rows + 1) {
            throw new IllegalArgumentException("row pointer length must equal rows + 1");
        }
        if (rowPointers[0] != 0) {
            throw new IllegalArgumentException("first row pointer must be zero");
        }
        int previous = 0;
        for (int pointer : rowPointers) {
            if (pointer < previous) {
                throw new IllegalArgumentException("row pointers must be monotonic");
            }
            previous = pointer;
        }
        if (rowPointers[rowPointers.length - 1] != values.length) {
            throw new IllegalArgumentException("last row pointer must equal values length");
        }
        for (int columnIndex : columnIndices) {
            if (columnIndex < 0 || columnIndex >= columns) {
                throw new IllegalArgumentException("column index out of range");
            }
        }
    }
}
