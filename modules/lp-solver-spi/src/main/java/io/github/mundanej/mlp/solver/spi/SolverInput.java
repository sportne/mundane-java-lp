package io.github.mundanej.mlp.solver.spi;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Solver-facing LP input envelope.
 *
 * @param problem canonical LP problem metadata
 * @param matrix row-by-column coefficient matrix matching the problem statistics
 * @param rowNames distinct nonblank row names in matrix row order; copied into an immutable list
 * @param columnNames distinct nonblank column names in variable order; copied into an immutable
 *     list
 * @param objectiveRowName nonblank objective row name for file formats
 */
public record SolverInput(
    LpProblem problem,
    CsrMatrix matrix,
    List<String> rowNames,
    List<String> columnNames,
    String objectiveRowName) {
  /**
   * Creates a solver input envelope.
   *
   * @param problem canonical LP problem metadata
   * @param matrix row-by-column coefficient matrix matching the problem statistics
   * @param rowNames distinct nonblank row names in matrix row order
   * @param columnNames distinct nonblank column names in variable order
   * @param objectiveRowName nonblank objective row name for file formats
   */
  public SolverInput {
    Objects.requireNonNull(problem, "problem");
    Objects.requireNonNull(matrix, "matrix");
    rowNames = List.copyOf(Objects.requireNonNull(rowNames, "rowNames"));
    columnNames = List.copyOf(Objects.requireNonNull(columnNames, "columnNames"));
    objectiveRowName = requireName(objectiveRowName, "objectiveRowName");
    if (problem.stats().rows() != matrix.rows()) {
      throw new IllegalArgumentException("matrix rows must match problem rows");
    }
    if (problem.stats().columns() != matrix.columns()) {
      throw new IllegalArgumentException("matrix columns must match problem columns");
    }
    if (problem.stats().nonzeros() != matrix.nonzeros()) {
      throw new IllegalArgumentException("matrix nonzeros must match problem nonzeros");
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

  /**
   * Creates a solver input with generated row and column names.
   *
   * @param problem canonical LP problem metadata
   * @param matrix row-by-column coefficient matrix matching the problem statistics
   * @return solver input whose row names are {@code R0..Rn}, column names are {@code X0..Xn}, and
   *     objective row name is {@code OBJ}
   */
  public static SolverInput withGeneratedNames(final LpProblem problem, final CsrMatrix matrix) {
    Objects.requireNonNull(problem, "problem");
    return new SolverInput(
        problem,
        matrix,
        generated("R", problem.stats().rows()),
        generated("X", problem.stats().columns()),
        "OBJ");
  }

  private static List<String> generated(final String prefix, final int count) {
    return IntStream.range(0, count).mapToObj(index -> prefix + index).toList();
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

  private static String requireName(final String value, final String label) {
    Objects.requireNonNull(value, label);
    if (value.isBlank()) {
      throw new IllegalArgumentException(label + " must not be blank");
    }
    return value;
  }
}
