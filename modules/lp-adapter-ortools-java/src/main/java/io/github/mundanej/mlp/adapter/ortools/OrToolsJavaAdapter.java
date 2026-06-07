package io.github.mundanej.mlp.adapter.ortools;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.Objects;
import java.util.OptionalDouble;

/** Java library adapter for OR-Tools GLOP continuous linear optimization. */
public final class OrToolsJavaAdapter implements LpSolverAdapter {
  private static final SolverId ID = new SolverId("ortools", "java");
  private final NativeRuntimeLoader nativeRuntimeLoader;
  private final SolverFactory solverFactory;

  /** Creates an adapter that loads the default OR-Tools native runtime. */
  public OrToolsJavaAdapter() {
    this(Loader::loadNativeLibraries, () -> MPSolver.createSolver("GLOP"));
  }

  OrToolsJavaAdapter(final NativeRuntimeLoader nativeRuntimeLoader) {
    this(nativeRuntimeLoader, () -> MPSolver.createSolver("GLOP"));
  }

  OrToolsJavaAdapter(
      final NativeRuntimeLoader nativeRuntimeLoader, final SolverFactory solverFactory) {
    this.nativeRuntimeLoader = Objects.requireNonNull(nativeRuntimeLoader, "nativeRuntimeLoader");
    this.solverFactory = Objects.requireNonNull(solverFactory, "solverFactory");
  }

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
    long startNanos = System.nanoTime();
    try {
      nativeRuntimeLoader.load();
    } catch (LinkageError | RuntimeException exception) {
      return result(
          SolverStatus.UNSUPPORTED,
          OptionalDouble.empty(),
          new double[0],
          startNanos,
          "OR-Tools native runtime unavailable: " + message(exception));
    }
    MPSolver solver = null;
    try {
      solver = solverFactory.create();
      if (solver == null) {
        return result(
            SolverStatus.UNSUPPORTED,
            OptionalDouble.empty(),
            new double[0],
            startNanos,
            "OR-Tools GLOP solver is unavailable.");
      }
      solver.setTimeLimit(options.timeLimit().toMillis());
      solver.setNumThreads(options.threads());
      solveInto(input, solver);
      MPSolver.ResultStatus status = solver.solve();
      SolverStatus normalized = normalize(status);
      OptionalDouble objective = objectiveValue(normalized, solver.objective());
      double[] primal = primalValues(normalized, solver.variables());
      return result(normalized, objective, primal, startNanos, "OR-Tools GLOP status: " + status);
    } catch (IllegalArgumentException exception) {
      return result(
          SolverStatus.UNSUPPORTED,
          OptionalDouble.empty(),
          new double[0],
          startNanos,
          exception.getMessage());
    } catch (LinkageError exception) {
      return result(
          SolverStatus.UNSUPPORTED,
          OptionalDouble.empty(),
          new double[0],
          startNanos,
          "OR-Tools native runtime unavailable: " + message(exception));
    } catch (RuntimeException exception) {
      return result(
          SolverStatus.ERROR,
          OptionalDouble.empty(),
          new double[0],
          startNanos,
          message(exception));
    } finally {
      if (solver != null) {
        solver.delete();
      }
    }
  }

  private static void solveInto(final SolverInput input, final MPSolver solver) {
    LpProblem problem = input.problem();
    MPVariable[] variables = variables(problem, input.columnNames().toArray(String[]::new), solver);
    constraints(problem, input, variables, solver);
    objective(problem, variables, solver.objective());
  }

  private static MPVariable[] variables(
      final LpProblem problem, final String[] columnNames, final MPSolver solver) {
    MPVariable[] variables = new MPVariable[columnNames.length];
    for (int index = 0; index < variables.length; index++) {
      LpVariableBounds bounds = problem.variableBounds().get(index);
      variables[index] =
          solver.makeNumVar(bound(bounds.lower()), bound(bounds.upper()), columnNames[index]);
    }
    return variables;
  }

  private static void constraints(
      final LpProblem problem,
      final SolverInput input,
      final MPVariable[] variables,
      final MPSolver solver) {
    double[] values = input.matrix().values();
    int[] columnIndices = input.matrix().columnIndices();
    int[] rowPointers = input.matrix().rowPointers();
    for (int row = 0; row < input.matrix().rows(); row++) {
      LpRowBounds bounds = problem.rowBounds().get(row);
      MPConstraint constraint =
          solver.makeConstraint(
              bound(bounds.lower()), bound(bounds.upper()), input.rowNames().get(row));
      for (int offset = rowPointers[row]; offset < rowPointers[row + 1]; offset++) {
        constraint.setCoefficient(
            variables[columnIndices[offset]], finite(values[offset], "matrix value"));
      }
    }
  }

  private static void objective(
      final LpProblem problem, final MPVariable[] variables, final MPObjective objective) {
    double[] coefficients = problem.objective().coefficients();
    objective.setOffset(finite(problem.objective().constant(), "objective constant"));
    for (int index = 0; index < coefficients.length; index++) {
      objective.setCoefficient(
          variables[index], finite(coefficients[index], "objective coefficient"));
    }
    if (problem.objective().sense() == ObjectiveSense.MAXIMIZE) {
      objective.setMaximization();
    } else {
      objective.setMinimization();
    }
  }

  static SolverStatus normalize(final MPSolver.ResultStatus status) {
    return switch (status) {
      case OPTIMAL -> SolverStatus.OPTIMAL;
      case FEASIBLE -> SolverStatus.FEASIBLE;
      case INFEASIBLE -> SolverStatus.INFEASIBLE;
      case UNBOUNDED -> SolverStatus.UNBOUNDED;
      case ABNORMAL -> SolverStatus.NUMERICAL_FAILURE;
      case MODEL_INVALID -> SolverStatus.ERROR;
      case NOT_SOLVED -> SolverStatus.UNKNOWN;
    };
  }

  private static OptionalDouble objectiveValue(
      final SolverStatus status, final MPObjective objective) {
    if (status != SolverStatus.OPTIMAL && status != SolverStatus.FEASIBLE) {
      return OptionalDouble.empty();
    }
    double value = objective.value();
    if (!Double.isFinite(value)) {
      return OptionalDouble.empty();
    }
    return OptionalDouble.of(value);
  }

  private static double[] primalValues(final SolverStatus status, final MPVariable[] variables) {
    if (status != SolverStatus.OPTIMAL && status != SolverStatus.FEASIBLE) {
      return new double[0];
    }
    double[] primal = new double[variables.length];
    for (int index = 0; index < variables.length; index++) {
      double value = variables[index].solutionValue();
      if (!Double.isFinite(value)) {
        return new double[0];
      }
      primal[index] = value;
    }
    return primal;
  }

  private static double bound(final double value) {
    if (value == Double.POSITIVE_INFINITY) {
      return MPSolver.infinity();
    }
    if (value == Double.NEGATIVE_INFINITY) {
      return -MPSolver.infinity();
    }
    return finite(value, "bound");
  }

  private static double finite(final double value, final String label) {
    if (!Double.isFinite(value)) {
      throw new IllegalArgumentException(label + " must be finite");
    }
    return value;
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

  private static String message(final Throwable throwable) {
    String message = throwable.getMessage();
    if (message == null || message.isBlank()) {
      return throwable.getClass().getSimpleName();
    }
    return message;
  }

  @FunctionalInterface
  interface NativeRuntimeLoader {
    void load();
  }

  @FunctionalInterface
  interface SolverFactory {
    MPSolver create();
  }
}
