package io.github.mundanej.mlp.validation;

/** Solver status names normalized before validation. */
public enum ValidationStatus {
  /** Solver claims an optimal solution. */
  OPTIMAL,
  /** Solver returned a feasible non-optimal or incumbent solution. */
  FEASIBLE,
  /** Problem is infeasible. */
  INFEASIBLE,
  /** Problem is unbounded. */
  UNBOUNDED,
  /** Solver could not distinguish infeasible and unbounded. */
  INFEASIBLE_OR_UNBOUNDED,
  /** Time limit reached. */
  TIME_LIMIT,
  /** Memory limit reached. */
  MEMORY_LIMIT,
  /** Numerical issue. */
  NUMERICAL_FAILURE,
  /** Adapter or solver error. */
  ERROR,
  /** Unsupported problem or feature. */
  UNSUPPORTED,
  /** Unknown status. */
  UNKNOWN
}
