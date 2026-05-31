package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A named LP benchmark instance.
 *
 * @param id instance identifier
 * @param problem LP problem
 * @param matrix row-by-column coefficient matrix
 * @param expectedResult expected validation evidence
 */
public record BenchmarkInstance(
    String id, LpProblem problem, CsrMatrix matrix, ExpectedValidationResult expectedResult) {
  /**
   * Creates an instance.
   *
   * @param id instance identifier
   * @param problem LP problem
   * @param matrix row-by-column coefficient matrix
   * @param expectedResult expected validation evidence
   */
  public BenchmarkInstance {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("id must not be blank");
    }
    if (problem == null) {
      throw new IllegalArgumentException("problem must not be null");
    }
    if (matrix == null) {
      throw new IllegalArgumentException("matrix must not be null");
    }
    if (expectedResult == null) {
      throw new IllegalArgumentException("expectedResult must not be null");
    }
    if (problem.stats().rows() != matrix.rows()) {
      throw new IllegalArgumentException("matrix rows must match problem rows");
    }
    if (problem.stats().columns() != matrix.columns()) {
      throw new IllegalArgumentException("matrix columns must match problem columns");
    }
  }

  /**
   * Creates an instance with no expected validation evidence.
   *
   * @param id instance identifier
   * @param problem LP problem
   */
  public BenchmarkInstance(final String id, final LpProblem problem) {
    this(
        id,
        problem,
        emptyMatrix(problem),
        new ExpectedValidationResult(Optional.empty(), OptionalDouble.empty()));
  }

  private static CsrMatrix emptyMatrix(final LpProblem problem) {
    if (problem == null) {
      throw new IllegalArgumentException("problem must not be null");
    }
    return new CsrMatrix(
        problem.stats().rows(),
        problem.stats().columns(),
        new double[0],
        new int[0],
        new int[problem.stats().rows() + 1]);
  }
}
