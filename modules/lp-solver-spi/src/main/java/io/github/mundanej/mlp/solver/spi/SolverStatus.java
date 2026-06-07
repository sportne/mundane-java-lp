package io.github.mundanej.mlp.solver.spi;

/** Normalized solver status. */
public enum SolverStatus {
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
  /** Unsupported problem shape, file-format feature, or adapter capability. */
  UNSUPPORTED,
  /** Unknown status. */
  UNKNOWN
}
