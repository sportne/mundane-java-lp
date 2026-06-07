package io.github.mundanej.mlp.solver.simple;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/** Correctness-first in-project solver for tiny zero-, one-, and two-variable LPs. */
public final class SimpleLpSolverAdapter implements LpSolverAdapter {
  private static final SolverId ID = new SolverId("simple", "java");
  private static final double EPSILON = 1.0e-9d;

  /** {@inheritDoc} */
  @Override
  public SolverId id() {
    return ID;
  }

  /**
   * Solves supported zero-, one-, and two-variable LP shapes.
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
    int columns = problem.stats().columns();
    if (columns > 2) {
      return result(
          SolverStatus.UNSUPPORTED,
          OptionalDouble.empty(),
          new double[0],
          startNanos,
          "simple solver supports at most two variables");
    }
    SolveOutcome outcome =
        switch (columns) {
          case 0 -> solveZeroDimensional(problem);
          case 1 -> solveOneDimensional(problem, denseRows(input));
          case 2 -> solveTwoDimensional(problem, denseRows(input));
          default -> throw new IllegalStateException("unsupported column count " + columns);
        };
    return result(
        outcome.status(),
        outcome.objectiveValue(),
        outcome.primalValues(),
        startNanos,
        outcome.message());
  }

  private static SolveOutcome solveZeroDimensional(final LpProblem problem) {
    for (LpRowBounds bounds : problem.rowBounds()) {
      if (!satisfiesBounds(0.0d, bounds.lower(), bounds.upper())) {
        return SolveOutcome.statusOnly(SolverStatus.INFEASIBLE, "empty LP row bounds infeasible");
      }
    }
    return SolveOutcome.optimal(
        OptionalDouble.of(problem.objective().constant()), new double[0], "empty LP optimal");
  }

  private static SolveOutcome solveOneDimensional(final LpProblem problem, final double[][] rows) {
    Interval interval = variableInterval(problem.variableBounds().get(0));
    for (int row = 0; row < problem.rowBounds().size(); row++) {
      interval = interval.intersect(rowInterval(rows[row][0], problem.rowBounds().get(row)));
      if (interval.isEmpty()) {
        return SolveOutcome.statusOnly(SolverStatus.INFEASIBLE, "one-dimensional LP infeasible");
      }
    }
    double coefficient = problem.objective().coefficients()[0];
    boolean minimize = problem.objective().sense() == ObjectiveSense.MINIMIZE;
    double primal = chooseIntervalOptimum(interval, coefficient, minimize);
    if (!Double.isFinite(primal)) {
      return SolveOutcome.statusOnly(SolverStatus.UNBOUNDED, "one-dimensional LP unbounded");
    }
    return optimalAt(problem, new double[] {primal}, "one-dimensional LP optimal");
  }

  private static SolveOutcome solveTwoDimensional(final LpProblem problem, final double[][] rows) {
    List<Halfspace> halfspaces = new ArrayList<>();
    FeasibilityStatus status = addProblemHalfspaces(problem, rows, halfspaces);
    if (status == FeasibilityStatus.INFEASIBLE) {
      return SolveOutcome.statusOnly(SolverStatus.INFEASIBLE, "two-dimensional LP infeasible");
    }
    List<double[]> candidates = candidatePoints(halfspaces);
    List<double[]> feasible =
        candidates.stream().filter(candidate -> feasible(candidate, halfspaces)).toList();
    if (feasible.isEmpty()) {
      return SolveOutcome.statusOnly(SolverStatus.INFEASIBLE, "two-dimensional LP infeasible");
    }
    if (hasImprovingRecessionDirection(problem, halfspaces)) {
      return SolveOutcome.statusOnly(SolverStatus.UNBOUNDED, "two-dimensional LP unbounded");
    }
    double[] best = feasible.get(0);
    for (double[] candidate : feasible.subList(1, feasible.size())) {
      if (better(problem, candidate, best)) {
        best = candidate;
      }
    }
    return optimalAt(problem, best, "two-dimensional LP optimal");
  }

  private static double[][] denseRows(final SolverInput input) {
    int rows = input.matrix().rows();
    int columns = input.matrix().columns();
    double[][] dense = new double[rows][columns];
    double[] values = input.matrix().values();
    int[] columnIndices = input.matrix().columnIndices();
    int[] rowPointers = input.matrix().rowPointers();
    for (int row = 0; row < rows; row++) {
      for (int offset = rowPointers[row]; offset < rowPointers[row + 1]; offset++) {
        dense[row][columnIndices[offset]] += values[offset];
      }
    }
    return dense;
  }

  private static Interval variableInterval(final LpVariableBounds bounds) {
    return new Interval(bounds.lower(), bounds.upper());
  }

  private static Interval rowInterval(final double coefficient, final LpRowBounds bounds) {
    if (nearZero(coefficient)) {
      return satisfiesBounds(0.0d, bounds.lower(), bounds.upper())
          ? Interval.all()
          : Interval.empty();
    }
    double lower = bounds.lower() / coefficient;
    double upper = bounds.upper() / coefficient;
    return coefficient > 0.0d ? new Interval(lower, upper) : new Interval(upper, lower);
  }

  private static double chooseIntervalOptimum(
      final Interval interval, final double coefficient, final boolean minimize) {
    if (nearZero(coefficient)) {
      if (satisfiesBounds(0.0d, interval.lower(), interval.upper())) {
        return 0.0d;
      }
      return Double.isFinite(interval.lower()) ? interval.lower() : interval.upper();
    }
    boolean chooseLower = minimize == coefficient > 0.0d;
    return chooseLower ? interval.lower() : interval.upper();
  }

  private static FeasibilityStatus addProblemHalfspaces(
      final LpProblem problem, final double[][] rows, final List<Halfspace> halfspaces) {
    for (int column = 0; column < problem.variableBounds().size(); column++) {
      LpVariableBounds bounds = problem.variableBounds().get(column);
      if (addBound(halfspaces, unit(column, -1.0d), -bounds.lower()) == FeasibilityStatus.INFEASIBLE
          || addBound(halfspaces, unit(column, 1.0d), bounds.upper())
              == FeasibilityStatus.INFEASIBLE) {
        return FeasibilityStatus.INFEASIBLE;
      }
    }
    for (int row = 0; row < problem.rowBounds().size(); row++) {
      LpRowBounds bounds = problem.rowBounds().get(row);
      if (addBound(halfspaces, negate(rows[row]), -bounds.lower()) == FeasibilityStatus.INFEASIBLE
          || addBound(halfspaces, rows[row], bounds.upper()) == FeasibilityStatus.INFEASIBLE) {
        return FeasibilityStatus.INFEASIBLE;
      }
    }
    return FeasibilityStatus.FEASIBLE;
  }

  private static FeasibilityStatus addBound(
      final List<Halfspace> halfspaces, final double[] coefficients, final double bound) {
    if (!Double.isFinite(bound)) {
      return FeasibilityStatus.FEASIBLE;
    }
    if (nearZero(coefficients[0]) && nearZero(coefficients[1])) {
      return 0.0d <= bound + EPSILON ? FeasibilityStatus.FEASIBLE : FeasibilityStatus.INFEASIBLE;
    }
    halfspaces.add(new Halfspace(coefficients[0], coefficients[1], bound));
    return FeasibilityStatus.FEASIBLE;
  }

  private static double[] unit(final int column, final double value) {
    return column == 0 ? new double[] {value, 0.0d} : new double[] {0.0d, value};
  }

  private static double[] negate(final double[] values) {
    return new double[] {-values[0], -values[1]};
  }

  private static List<double[]> candidatePoints(final List<Halfspace> halfspaces) {
    List<double[]> candidates = new ArrayList<>();
    candidates.add(new double[] {0.0d, 0.0d});
    for (Halfspace halfspace : halfspaces) {
      double normSquared = halfspace.a() * halfspace.a() + halfspace.b() * halfspace.b();
      candidates.add(
          new double[] {
            halfspace.limit() * halfspace.a() / normSquared,
            halfspace.limit() * halfspace.b() / normSquared
          });
    }
    for (int left = 0; left < halfspaces.size(); left++) {
      for (int right = left + 1; right < halfspaces.size(); right++) {
        double[] intersection = intersection(halfspaces.get(left), halfspaces.get(right));
        if (intersection != null) {
          candidates.add(intersection);
        }
      }
    }
    return candidates;
  }

  private static double[] intersection(final Halfspace left, final Halfspace right) {
    double determinant = left.a() * right.b() - right.a() * left.b();
    if (nearZero(determinant)) {
      return null;
    }
    return new double[] {
      (left.limit() * right.b() - right.limit() * left.b()) / determinant,
      (left.a() * right.limit() - right.a() * left.limit()) / determinant
    };
  }

  private static boolean feasible(final double[] point, final List<Halfspace> halfspaces) {
    for (Halfspace halfspace : halfspaces) {
      if (halfspace.a() * point[0] + halfspace.b() * point[1] > halfspace.limit() + EPSILON) {
        return false;
      }
    }
    return true;
  }

  private static boolean hasImprovingRecessionDirection(
      final LpProblem problem, final List<Halfspace> halfspaces) {
    double[] objective = problem.objective().coefficients();
    if (nearZero(objective[0]) && nearZero(objective[1])) {
      return false;
    }
    List<double[]> directions = new ArrayList<>();
    double sign = problem.objective().sense() == ObjectiveSense.MAXIMIZE ? 1.0d : -1.0d;
    directions.add(new double[] {sign * objective[0], sign * objective[1]});
    directions.add(new double[] {1.0d, 0.0d});
    directions.add(new double[] {-1.0d, 0.0d});
    directions.add(new double[] {0.0d, 1.0d});
    directions.add(new double[] {0.0d, -1.0d});
    for (Halfspace halfspace : halfspaces) {
      directions.add(new double[] {halfspace.b(), -halfspace.a()});
      directions.add(new double[] {-halfspace.b(), halfspace.a()});
    }
    return directions.stream()
        .filter(direction -> !nearZero(direction[0]) || !nearZero(direction[1]))
        .anyMatch(
            direction -> feasibleDirection(direction, halfspaces) && improves(problem, direction));
  }

  private static boolean feasibleDirection(
      final double[] direction, final List<Halfspace> halfspaces) {
    for (Halfspace halfspace : halfspaces) {
      if (halfspace.a() * direction[0] + halfspace.b() * direction[1] > EPSILON) {
        return false;
      }
    }
    return true;
  }

  private static boolean improves(final LpProblem problem, final double[] direction) {
    double[] objective = problem.objective().coefficients();
    double delta = objective[0] * direction[0] + objective[1] * direction[1];
    return problem.objective().sense() == ObjectiveSense.MAXIMIZE
        ? delta > EPSILON
        : delta < -EPSILON;
  }

  private static boolean better(
      final LpProblem problem, final double[] candidate, final double[] incumbent) {
    double candidateValue = problem.objective().evaluate(candidate);
    double incumbentValue = problem.objective().evaluate(incumbent);
    if (Math.abs(candidateValue - incumbentValue) > EPSILON) {
      return problem.objective().sense() == ObjectiveSense.MAXIMIZE
          ? candidateValue > incumbentValue
          : candidateValue < incumbentValue;
    }
    return candidate[0] < incumbent[0] - EPSILON
        || (Math.abs(candidate[0] - incumbent[0]) <= EPSILON
            && candidate[1] < incumbent[1] - EPSILON);
  }

  private static SolveOutcome optimalAt(
      final LpProblem problem, final double[] primal, final String message) {
    return SolveOutcome.optimal(
        OptionalDouble.of(problem.objective().evaluate(primal)), primal, message);
  }

  private static boolean satisfiesBounds(
      final double value, final double lower, final double upper) {
    return value >= lower - EPSILON && value <= upper + EPSILON;
  }

  private static boolean nearZero(final double value) {
    return Math.abs(value) <= EPSILON;
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

  private enum FeasibilityStatus {
    FEASIBLE,
    INFEASIBLE
  }

  private record Interval(double lower, double upper) {
    static Interval all() {
      return new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    static Interval empty() {
      return new Interval(1.0d, 0.0d);
    }

    Interval intersect(final Interval other) {
      return new Interval(Math.max(lower, other.lower()), Math.min(upper, other.upper()));
    }

    boolean isEmpty() {
      return lower > upper + EPSILON;
    }
  }

  private record Halfspace(double a, double b, double limit) {}

  private record SolveOutcome(
      SolverStatus status, OptionalDouble objectiveValue, double[] primalValues, String message) {
    static SolveOutcome optimal(
        final OptionalDouble objectiveValue, final double[] primalValues, final String message) {
      return new SolveOutcome(SolverStatus.OPTIMAL, objectiveValue, primalValues, message);
    }

    static SolveOutcome statusOnly(final SolverStatus status, final String message) {
      return new SolveOutcome(status, OptionalDouble.empty(), new double[0], message);
    }
  }
}
