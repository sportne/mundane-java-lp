package io.github.mundanej.mlp.solver.simple;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.Objects;
import java.util.OptionalDouble;

/** Minimal in-project simple solver adapter. */
public final class SimpleLpSolverAdapter implements LpSolverAdapter {
  private static final SolverId ID = new SolverId("simple", "java");

  /** {@inheritDoc} */
  @Override
  public SolverId id() {
    return ID;
  }

  /**
   * Solves a trivial empty LP smoke shape or returns unsupported.
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
    LpProblem problem = input.problem();
    if (problem.stats().rows() == 0 && problem.stats().columns() == 0) {
      return result(
          SolverStatus.OPTIMAL,
          OptionalDouble.of(problem.objective().constant()),
          new double[0],
          startNanos,
          "simple solver trivial empty LP smoke path");
    }
    return result(
        SolverStatus.UNSUPPORTED,
        OptionalDouble.empty(),
        new double[0],
        startNanos,
        "simple solver supports only the G9-002 empty LP smoke path");
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
