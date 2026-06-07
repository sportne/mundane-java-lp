package io.github.mundanej.mlp.generators;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Canonical hand-checkable LP fixture with matrix coefficients and evidence.
 *
 * @param problem LP problem metadata
 * @param matrix row-by-column coefficient matrix
 * @param rowNames distinct nonblank row names in matrix row order; copied into an immutable list
 * @param columnNames distinct nonblank column names in variable order; copied into an immutable
 *     list
 * @param evidence hand-checked expected evidence
 */
public record CanonicalLpFixture(
    LpProblem problem,
    CsrMatrix matrix,
    List<String> rowNames,
    List<String> columnNames,
    LpFixtureEvidence evidence) {
  private static final double EVIDENCE_TOLERANCE = 1.0e-12;

  /**
   * Creates a fixture.
   *
   * @param problem LP problem metadata
   * @param matrix row-by-column coefficient matrix
   * @param rowNames distinct nonblank row names in matrix row order
   * @param columnNames distinct nonblank column names in variable order
   * @param evidence hand-checked expected evidence
   */
  public CanonicalLpFixture {
    Objects.requireNonNull(problem, "problem");
    Objects.requireNonNull(matrix, "matrix");
    rowNames = List.copyOf(Objects.requireNonNull(rowNames, "rowNames"));
    columnNames = List.copyOf(Objects.requireNonNull(columnNames, "columnNames"));
    Objects.requireNonNull(evidence, "evidence");
    if (problem.stats().rows() != matrix.rows()) {
      throw new IllegalArgumentException("problem row count must match matrix rows");
    }
    if (problem.stats().columns() != matrix.columns()) {
      throw new IllegalArgumentException("problem column count must match matrix columns");
    }
    if (problem.rowBounds().size() != rowNames.size()) {
      throw new IllegalArgumentException("row names must match row count");
    }
    if (problem.variableBounds().size() != columnNames.size()) {
      throw new IllegalArgumentException("column names must match column count");
    }
    requireDistinctNames(rowNames, "row");
    requireDistinctNames(columnNames, "column");
    validateEvidence(problem, matrix, evidence);
  }

  private static void requireDistinctNames(final List<String> names, final String kind) {
    Set<String> seen = new HashSet<>();
    for (String name : names) {
      if (name == null || name.isBlank()) {
        throw new IllegalArgumentException(kind + " names must be nonblank");
      }
      if (!seen.add(name)) {
        throw new IllegalArgumentException(kind + " names must be unique");
      }
    }
  }

  private static void validateEvidence(
      final LpProblem problem, final CsrMatrix matrix, final LpFixtureEvidence evidence) {
    if (evidence.resultKind() != ExpectedResultKind.OPTIMAL) {
      return;
    }
    double[] primal = evidence.primal();
    if (primal.length != problem.variableBounds().size()) {
      throw new IllegalArgumentException("optimal primal length must match variable count");
    }
    validateVariableBounds(problem, primal);
    validateRowBounds(problem, matrix.multiply(primal));
    double actualObjective = problem.objective().evaluate(primal);
    double expectedObjective = evidence.objectiveValue().orElseThrow();
    if (Math.abs(actualObjective - expectedObjective) > EVIDENCE_TOLERANCE) {
      throw new IllegalArgumentException("optimal objective evidence does not match primal");
    }
  }

  private static void validateVariableBounds(final LpProblem problem, final double[] primal) {
    for (int index = 0; index < primal.length; index++) {
      LpVariableBounds bounds = problem.variableBounds().get(index);
      if (primal[index] < bounds.lower() - EVIDENCE_TOLERANCE
          || primal[index] > bounds.upper() + EVIDENCE_TOLERANCE) {
        throw new IllegalArgumentException("optimal primal violates variable bounds");
      }
    }
  }

  private static void validateRowBounds(final LpProblem problem, final double[] activities) {
    for (int index = 0; index < activities.length; index++) {
      LpRowBounds bounds = problem.rowBounds().get(index);
      if (activities[index] < bounds.lower() - EVIDENCE_TOLERANCE
          || activities[index] > bounds.upper() + EVIDENCE_TOLERANCE) {
        throw new IllegalArgumentException("optimal primal violates row bounds");
      }
    }
  }
}
