package io.github.mundanej.mlp.solver.spi;

/** Adapter contract for LP solvers. */
public interface LpSolverAdapter {
  /** Returns solver identifier. */
  SolverId id();

  /**
   * Solves the supplied problem or returns an unsupported/error result.
   *
   * @param input solver input envelope
   * @param options solver options
   * @param workDirectory solver work directory
   */
  SolverRunResult solve(
      SolverInput input, SolverOptions options, SolverWorkDirectory workDirectory);
}
