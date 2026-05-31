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
  private final RevisedSimplexCore core = new RevisedSimplexCore();

  /** {@inheritDoc} */
  @Override
  public SolverId id() {
    return ID;
  }

  /** {@inheritDoc} */
  @Override
  public SolverRunResult solve(
      final SolverInput input,
      final SolverOptions options,
      final SolverWorkDirectory workDirectory) {
    Objects.requireNonNull(input, "input");
    Objects.requireNonNull(options, "options");
    Objects.requireNonNull(workDirectory, "workDirectory");
    long startNanos = System.nanoTime();
    RevisedSimplexCore.SolveResult outcome = core.solve(input);
    return result(
        outcome.status(),
        outcome.objectiveValue(),
        outcome.primalValues(),
        startNanos,
        outcome.message());
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
