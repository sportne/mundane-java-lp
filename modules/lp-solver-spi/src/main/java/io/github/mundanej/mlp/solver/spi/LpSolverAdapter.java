package io.github.mundanej.mlp.solver.spi;

/** Adapter contract for LP solvers used by comparison and benchmark harnesses. */
public interface LpSolverAdapter {
  /** Returns the stable solver identifier used in reports. */
  SolverId id();

  /**
   * Solves the supplied problem or returns a normalized unsupported, unavailable, or error result.
   *
   * @param input solver input envelope
   * @param options solver options
   * @param workDirectory solver-owned work directory for temporary files
   * @return normalized solver evidence and diagnostic message
   */
  SolverRunResult solve(
      SolverInput input, SolverOptions options, SolverWorkDirectory workDirectory);
}
