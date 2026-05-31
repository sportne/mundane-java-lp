package io.github.mundanej.mlp.io.mps;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * MPS-facing LP envelope with coefficients and diagnostic row/column names.
 *
 * @param problem canonical LP problem metadata
 * @param matrix row-by-column coefficient matrix
 * @param rowNames diagnostic row names
 * @param columnNames diagnostic column names
 * @param objectiveRowName objective row name
 */
public record MpsLp(
    LpProblem problem,
    CsrMatrix matrix,
    List<String> rowNames,
    List<String> columnNames,
    String objectiveRowName) {
  /**
   * Creates an MPS LP envelope.
   *
   * @param problem canonical LP problem metadata
   * @param matrix row-by-column coefficient matrix
   * @param rowNames diagnostic row names
   * @param columnNames diagnostic column names
   * @param objectiveRowName objective row name
   */
  public MpsLp {
    Objects.requireNonNull(problem, "problem");
    Objects.requireNonNull(matrix, "matrix");
    rowNames = List.copyOf(Objects.requireNonNull(rowNames, "rowNames"));
    columnNames = List.copyOf(Objects.requireNonNull(columnNames, "columnNames"));
    objectiveRowName = requireName(objectiveRowName, "objectiveRowName");
    if (problem.stats().rows() != matrix.rows()) {
      throw new IllegalArgumentException("problem row count must match matrix rows");
    }
    if (problem.stats().columns() != matrix.columns()) {
      throw new IllegalArgumentException("problem column count must match matrix columns");
    }
    if (problem.stats().nonzeros() != matrix.nonzeros()) {
      throw new IllegalArgumentException("problem nonzero count must match matrix nonzeros");
    }
    if (problem.rowBounds().size() != rowNames.size()) {
      throw new IllegalArgumentException("row names must match row count");
    }
    if (problem.variableBounds().size() != columnNames.size()) {
      throw new IllegalArgumentException("column names must match column count");
    }
    requireDistinct(rowNames, "row");
    requireDistinct(columnNames, "column");
  }

  private static void requireDistinct(final List<String> names, final String kind) {
    Set<String> seen = new HashSet<>();
    for (String name : names) {
      String checked = requireName(name, kind + " name");
      if (!seen.add(checked)) {
        throw new IllegalArgumentException(kind + " names must be unique");
      }
    }
  }

  private static String requireName(final String name, final String label) {
    Objects.requireNonNull(name, label);
    if (name.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return name;
  }
}
