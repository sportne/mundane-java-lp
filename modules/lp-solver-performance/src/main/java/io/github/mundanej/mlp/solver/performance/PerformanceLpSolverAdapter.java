package io.github.mundanej.mlp.solver.performance;

import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.Objects;
import java.util.OptionalDouble;

/** Minimal in-project performance solver adapter. */
public final class PerformanceLpSolverAdapter implements LpSolverAdapter {
  private static final SolverId ID = new SolverId("performance", "java");

  /** {@inheritDoc} */
  @Override
  public SolverId id() {
    return ID;
  }

  /**
   * Solves only the initial empty-LP smoke shape until the core lands.
   *
   * @param input solver input envelope
   * @param options solver options
   * @param workDirectory solver work directory
   */
  @Override
  public SolverRunResult solve(
      final SolverInput input,
      final SolverOptions options,
      final SolverWorkDirectory workDirectory) {
    Objects.requireNonNull(input, "input");
    Objects.requireNonNull(options, "options");
    Objects.requireNonNull(workDirectory, "workDirectory");
    long startNanos = System.nanoTime();
    if (input.problem().stats().rows() == 0 && input.problem().stats().columns() == 0) {
      return result(
          SolverStatus.OPTIMAL,
          OptionalDouble.of(input.problem().objective().constant()),
          new double[0],
          startNanos,
          "empty LP performance smoke optimal");
    }
    return result(
        SolverStatus.UNSUPPORTED,
        OptionalDouble.empty(),
        new double[0],
        startNanos,
        "performance solver core is not implemented for this shape");
  }

  private static SolverRunResult result(
      final SolverStatus status,
      final OptionalDouble objective,
      final double[] primal,
      final long startNanos,
      final String message) {
    double elapsedSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0d;
    return new SolverRunResult(ID, status, objective, primal, elapsedSeconds, message);
  }
}
