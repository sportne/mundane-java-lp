package io.github.mundanej.mlp.solver.performance;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import java.util.OptionalDouble;

final class RevisedSimplexCore {
  private static final double EPSILON = 1.0e-9d;

  SolveResult solve(final SolverInput input) {
    LpProblem problem = input.problem();
    int columns = problem.stats().columns();
    String unsupported = validateFiniteProblemData(problem);
    if (unsupported != null) {
      return SolveResult.statusOnly(SolverStatus.UNSUPPORTED, unsupported);
    }
    ConstraintCounts counts = countConstraints(input);
    if (counts.unsupported() != null) {
      return SolveResult.statusOnly(SolverStatus.UNSUPPORTED, counts.unsupported());
    }
    if (columns == 0) {
      return solveZeroDimensional(problem);
    }
    if (counts.constraintCount() == 0) {
      return unconstrainedNonnegative(problem);
    }
    Tableau tableau = buildTableau(input, counts);
    if (tableau.hasArtificialVariables()) {
      SimplexStatus phaseOne = tableau.optimize(tableau.phaseOneObjective());
      if (phaseOne == SimplexStatus.UNBOUNDED || tableau.objectiveValue() < -EPSILON) {
        return SolveResult.statusOnly(SolverStatus.INFEASIBLE, "phase I infeasible");
      }
    }
    double[] objective = normalizedObjective(problem);
    SimplexStatus status = tableau.optimize(objective);
    if (status == SimplexStatus.UNBOUNDED) {
      return SolveResult.statusOnly(SolverStatus.UNBOUNDED, "simplex detected improving ray");
    }
    double[] primal = tableau.originalSolution(columns);
    String violation = originalFeasibilityViolation(input, primal);
    if (violation != null) {
      return SolveResult.statusOnly(SolverStatus.INFEASIBLE, violation);
    }
    return SolveResult.optimal(problem.objective().evaluate(primal), primal);
  }

  private static String validateFiniteProblemData(final LpProblem problem) {
    if (!Double.isFinite(problem.objective().constant())) {
      return "performance solver requires finite objective constants";
    }
    for (double coefficient : problem.objective().coefficients()) {
      if (!Double.isFinite(coefficient)) {
        return "performance solver requires finite objective coefficients";
      }
    }
    for (LpVariableBounds bounds : problem.variableBounds()) {
      if (Double.isNaN(bounds.lower()) || Double.isNaN(bounds.upper())) {
        return "performance solver requires non-NaN variable bounds";
      }
    }
    for (LpRowBounds bounds : problem.rowBounds()) {
      if (Double.isNaN(bounds.lower()) || Double.isNaN(bounds.upper())) {
        return "performance solver requires non-NaN row bounds";
      }
    }
    return null;
  }

  private static SolveResult solveZeroDimensional(final LpProblem problem) {
    for (LpRowBounds bounds : problem.rowBounds()) {
      if (!satisfies(0.0d, bounds)) {
        return SolveResult.statusOnly(SolverStatus.INFEASIBLE, "empty LP row bounds infeasible");
      }
    }
    return SolveResult.optimal(problem.objective().constant(), new double[0]);
  }

  private static ConstraintCounts countConstraints(final SolverInput input) {
    LpProblem problem = input.problem();
    int columns = problem.stats().columns();
    int constraintCount = 0;
    int extraColumns = 0;
    for (int column = 0; column < columns; column++) {
      LpVariableBounds bounds = problem.variableBounds().get(column);
      if (Math.abs(bounds.lower()) > EPSILON) {
        return ConstraintCounts.unsupported("performance solver supports only zero lower bounds");
      }
      if (Double.isFinite(bounds.upper())) {
        constraintCount++;
        extraColumns += extraColumnsFor(Relation.LE, bounds.upper());
      }
    }
    for (int row = 0; row < problem.rowBounds().size(); row++) {
      LpRowBounds bounds = problem.rowBounds().get(row);
      if (!Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
        continue;
      }
      if (!bounds.isEquality()
          && Double.isFinite(bounds.lower())
          && Double.isFinite(bounds.upper())) {
        return ConstraintCounts.unsupported("performance solver does not support ranged rows");
      }
      if (bounds.isEquality()) {
        constraintCount++;
        extraColumns += extraColumnsFor(Relation.EQ, bounds.upper());
      } else if (!Double.isFinite(bounds.lower()) && Double.isFinite(bounds.upper())) {
        constraintCount++;
        extraColumns += extraColumnsFor(Relation.LE, bounds.upper());
      } else if (Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
        constraintCount++;
        extraColumns += extraColumnsFor(Relation.GE, bounds.lower());
      }
    }
    return new ConstraintCounts(constraintCount, extraColumns, null);
  }

  private static int extraColumnsFor(final Relation relation, final double rhs) {
    Relation normalizedRelation = normalizedRelation(relation, rhs);
    return switch (normalizedRelation) {
      case LE, EQ -> 1;
      case GE -> 2;
    };
  }

  private static Tableau buildTableau(final SolverInput input, final ConstraintCounts counts) {
    LpProblem problem = input.problem();
    int columns = problem.stats().columns();
    TableauBuilder builder =
        new TableauBuilder(columns, counts.constraintCount(), counts.extraColumns());
    for (int column = 0; column < columns; column++) {
      LpVariableBounds bounds = problem.variableBounds().get(column);
      if (Double.isFinite(bounds.upper())) {
        builder.addUnitLe(column, bounds.upper());
      }
    }
    CsrMatrix matrix = input.matrix();
    double[] rowCoefficients = new double[matrix.columns()];
    for (int row = 0; row < problem.rowBounds().size(); row++) {
      LpRowBounds bounds = problem.rowBounds().get(row);
      if (!Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
        continue;
      }
      matrix.copyRowInto(row, rowCoefficients);
      if (bounds.isEquality()) {
        builder.add(Relation.EQ, rowCoefficients, bounds.upper());
      } else if (!Double.isFinite(bounds.lower()) && Double.isFinite(bounds.upper())) {
        builder.add(Relation.LE, rowCoefficients, bounds.upper());
      } else if (Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
        builder.add(Relation.GE, rowCoefficients, bounds.lower());
      }
    }
    return builder.build();
  }

  private static SolveResult unconstrainedNonnegative(final LpProblem problem) {
    double[] objective = normalizedObjective(problem);
    for (double coefficient : objective) {
      if (coefficient > EPSILON) {
        return SolveResult.statusOnly(SolverStatus.UNBOUNDED, "unconstrained objective ray");
      }
    }
    double[] primal = new double[objective.length];
    return SolveResult.optimal(problem.objective().evaluate(primal), primal);
  }

  private static double[] normalizedObjective(final LpProblem problem) {
    double[] coefficients = problem.objective().coefficients();
    if (problem.objective().sense() == ObjectiveSense.MINIMIZE) {
      for (int index = 0; index < coefficients.length; index++) {
        coefficients[index] = -coefficients[index];
      }
    }
    return coefficients;
  }

  private static boolean satisfies(final double value, final LpRowBounds bounds) {
    return value >= bounds.lower() - EPSILON && value <= bounds.upper() + EPSILON;
  }

  private static String originalFeasibilityViolation(
      final SolverInput input, final double[] primal) {
    LpProblem problem = input.problem();
    for (int column = 0; column < primal.length; column++) {
      LpVariableBounds bounds = problem.variableBounds().get(column);
      if (violatesLower(primal[column], bounds.lower())
          || violatesUpper(primal[column], bounds.upper())) {
        return "simplex primal violates original variable bounds";
      }
    }
    double[] activities = input.matrix().multiply(primal);
    for (int row = 0; row < activities.length; row++) {
      LpRowBounds bounds = problem.rowBounds().get(row);
      if (violatesLower(activities[row], bounds.lower())
          || violatesUpper(activities[row], bounds.upper())) {
        return "simplex primal violates original row bounds";
      }
    }
    return null;
  }

  private static boolean violatesLower(final double value, final double lower) {
    return Double.isFinite(lower) && lower - value > feasibilityTolerance(value, lower);
  }

  private static boolean violatesUpper(final double value, final double upper) {
    return Double.isFinite(upper) && value - upper > feasibilityTolerance(value, upper);
  }

  private static double feasibilityTolerance(final double value, final double bound) {
    return EPSILON * Math.max(1.0d, Math.max(Math.abs(value), Math.abs(bound)));
  }

  record SolveResult(
      SolverStatus status, OptionalDouble objectiveValue, double[] primalValues, String message) {
    static SolveResult optimal(final double objectiveValue, final double[] primalValues) {
      return new SolveResult(
          SolverStatus.OPTIMAL,
          OptionalDouble.of(objectiveValue),
          primalValues.clone(),
          "simplex core optimal");
    }

    static SolveResult statusOnly(final SolverStatus status, final String message) {
      return new SolveResult(status, OptionalDouble.empty(), new double[0], message);
    }
  }

  private enum Relation {
    LE,
    GE,
    EQ
  }

  private record ConstraintCounts(int constraintCount, int extraColumns, String unsupported) {
    static ConstraintCounts unsupported(final String message) {
      return new ConstraintCounts(0, 0, message);
    }
  }

  record LinearConstraint(double[] coefficients, Relation relation, double rhs) {
    static LinearConstraint unitLe(final int columns, final int column, final double rhs) {
      double[] coefficients = new double[columns];
      coefficients[column] = 1.0d;
      return normalizeOwned(coefficients, Relation.LE, rhs);
    }

    static LinearConstraint le(final double[] coefficients, final double rhs) {
      return normalize(coefficients, Relation.LE, rhs);
    }

    static LinearConstraint ge(final double[] coefficients, final double rhs) {
      return normalize(coefficients, Relation.GE, rhs);
    }

    static LinearConstraint eq(final double[] coefficients, final double rhs) {
      return normalize(coefficients, Relation.EQ, rhs);
    }

    private static LinearConstraint normalize(
        final double[] coefficients, final Relation relation, final double rhs) {
      double[] copy = coefficients.clone();
      return normalizeOwned(copy, relation, rhs);
    }

    private static LinearConstraint normalizeOwned(
        final double[] coefficients, final Relation relation, final double rhs) {
      Relation normalizedRelation = relation;
      double normalizedRhs = rhs;
      if (normalizedRhs < 0.0d) {
        for (int index = 0; index < coefficients.length; index++) {
          coefficients[index] = -coefficients[index];
        }
        normalizedRhs = -normalizedRhs;
        normalizedRelation = flipInequality(normalizedRelation);
      }
      return new LinearConstraint(coefficients, normalizedRelation, normalizedRhs);
    }
  }

  enum SimplexStatus {
    OPTIMAL,
    UNBOUNDED
  }

  static final class Tableau {
    private final double[][] rows;
    private final int[] basis;
    private final boolean[] artificial;
    private final int variableCount;

    private Tableau(
        final double[][] rows,
        final int[] basis,
        final boolean[] artificial,
        final int variableCount) {
      this.rows = rows;
      this.basis = basis;
      this.artificial = artificial;
      this.variableCount = variableCount;
    }

    static Tableau from(final int originalColumns, final List<LinearConstraint> constraints) {
      int extraColumns = 0;
      for (LinearConstraint constraint : constraints) {
        extraColumns +=
            switch (constraint.relation()) {
              case LE -> 1;
              case GE -> 2;
              case EQ -> 1;
            };
      }
      TableauBuilder builder =
          new TableauBuilder(originalColumns, constraints.size(), extraColumns);
      for (LinearConstraint constraint : constraints) {
        builder.add(constraint.relation(), constraint.coefficients(), constraint.rhs());
      }
      return builder.build();
    }

    boolean hasArtificialVariables() {
      for (boolean value : artificial) {
        if (value) {
          return true;
        }
      }
      return false;
    }

    double[] phaseOneObjective() {
      double[] objective = new double[variableCount];
      for (int column = 0; column < variableCount; column++) {
        if (artificial[column]) {
          objective[column] = -1.0d;
        }
      }
      return objective;
    }

    SimplexStatus optimize(final double[] objective) {
      installObjective(objective);
      while (true) {
        int entering = enteringColumn();
        if (entering < 0) {
          return SimplexStatus.OPTIMAL;
        }
        int leaving = leavingRow(entering);
        if (leaving < 0) {
          return SimplexStatus.UNBOUNDED;
        }
        pivot(leaving, entering);
      }
    }

    double objectiveValue() {
      return rows[rows.length - 1][variableCount];
    }

    double[] originalSolution(final int originalColumns) {
      double[] solution = new double[originalColumns];
      for (int row = 0; row < basis.length; row++) {
        if (basis[row] < originalColumns) {
          solution[basis[row]] = rows[row][variableCount];
        }
      }
      return solution;
    }

    int basisColumn(final int row) {
      return basis[row];
    }

    int enteringColumn() {
      double[] objectiveRow = rows[rows.length - 1];
      for (int column = 0; column < variableCount; column++) {
        if (!artificial[column] && objectiveRow[column] < -EPSILON) {
          return column;
        }
      }
      return -1;
    }

    int leavingRow(final int enteringColumn) {
      int leaving = -1;
      double bestRatio = Double.POSITIVE_INFINITY;
      for (int row = 0; row < basis.length; row++) {
        double coefficient = rows[row][enteringColumn];
        if (coefficient > EPSILON) {
          double ratio = rows[row][variableCount] / coefficient;
          if (ratio < bestRatio - EPSILON) {
            bestRatio = ratio;
            leaving = row;
          }
        }
      }
      return leaving;
    }

    void pivot(final int pivotRow, final int pivotColumn) {
      double divisor = rows[pivotRow][pivotColumn];
      for (int column = 0; column <= variableCount; column++) {
        rows[pivotRow][column] /= divisor;
      }
      for (int row = 0; row < rows.length; row++) {
        if (row != pivotRow) {
          double factor = rows[row][pivotColumn];
          if (Math.abs(factor) > EPSILON) {
            for (int column = 0; column <= variableCount; column++) {
              rows[row][column] -= factor * rows[pivotRow][column];
            }
          }
        }
      }
      basis[pivotRow] = pivotColumn;
    }

    private void installObjective(final double[] objective) {
      double[] objectiveRow = rows[rows.length - 1];
      for (int column = 0; column <= variableCount; column++) {
        objectiveRow[column] = 0.0d;
      }
      for (int column = 0; column < objective.length; column++) {
        objectiveRow[column] = -objective[column];
      }
      for (int row = 0; row < basis.length; row++) {
        int basic = basis[row];
        if (basic < objective.length) {
          double coefficient = objective[basic];
          if (Math.abs(coefficient) > EPSILON) {
            for (int column = 0; column <= variableCount; column++) {
              objectiveRow[column] += coefficient * rows[row][column];
            }
          }
        }
      }
    }
  }

  private static final class TableauBuilder {
    private final int originalColumns;
    private final int variableCount;
    private final double[][] rows;
    private final int[] basis;
    private final boolean[] artificial;
    private int nextColumn;
    private int nextRow;

    private TableauBuilder(
        final int originalColumns, final int constraintCount, final int extraColumns) {
      this.originalColumns = originalColumns;
      this.variableCount = originalColumns + extraColumns;
      this.rows = new double[constraintCount + 1][variableCount + 1];
      this.basis = new int[constraintCount];
      this.artificial = new boolean[variableCount];
      this.nextColumn = originalColumns;
    }

    private void addUnitLe(final int column, final double rhs) {
      rows[nextRow][column] = 1.0d;
      rows[nextRow][variableCount] = rhs;
      addSlackBasis();
      nextRow++;
    }

    private void add(final Relation relation, final double[] coefficients, final double rhs) {
      Relation normalizedRelation = relation;
      double normalizedRhs = rhs;
      double sign = 1.0d;
      if (normalizedRhs < 0.0d) {
        normalizedRhs = -normalizedRhs;
        sign = -1.0d;
        normalizedRelation = flipInequality(normalizedRelation);
      }
      for (int column = 0; column < originalColumns; column++) {
        rows[nextRow][column] = sign * coefficients[column];
      }
      rows[nextRow][variableCount] = normalizedRhs;
      scaleOriginalColumns();
      if (normalizedRelation == Relation.LE) {
        addSlackBasis();
      } else if (normalizedRelation == Relation.GE) {
        rows[nextRow][nextColumn] = -1.0d;
        nextColumn++;
        addArtificialBasis();
      } else {
        addArtificialBasis();
      }
      nextRow++;
    }

    private void scaleOriginalColumns() {
      double scale = 0.0d;
      for (int column = 0; column < originalColumns; column++) {
        scale = Math.max(scale, Math.abs(rows[nextRow][column]));
      }
      if (scale <= 1.0d || !Double.isFinite(scale)) {
        return;
      }
      for (int column = 0; column < originalColumns; column++) {
        rows[nextRow][column] /= scale;
      }
      rows[nextRow][variableCount] /= scale;
    }

    private void addSlackBasis() {
      rows[nextRow][nextColumn] = 1.0d;
      basis[nextRow] = nextColumn;
      nextColumn++;
    }

    private void addArtificialBasis() {
      rows[nextRow][nextColumn] = 1.0d;
      artificial[nextColumn] = true;
      basis[nextRow] = nextColumn;
      nextColumn++;
    }

    private Tableau build() {
      return new Tableau(rows, basis, artificial, variableCount);
    }
  }

  private static Relation normalizedRelation(final Relation relation, final double rhs) {
    return rhs < 0.0d ? flipInequality(relation) : relation;
  }

  private static Relation flipInequality(final Relation relation) {
    if (relation == Relation.LE) {
      return Relation.GE;
    }
    if (relation == Relation.GE) {
      return Relation.LE;
    }
    return relation;
  }
}
