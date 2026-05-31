package io.github.mundanej.mlp.solver.performance;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.ArrayList;
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
    List<LinearConstraint> constraints = new ArrayList<>();
    unsupported = addVariableConstraints(problem, constraints);
    if (unsupported != null) {
      return SolveResult.statusOnly(SolverStatus.UNSUPPORTED, unsupported);
    }
    unsupported = addRowConstraints(input, constraints);
    if (unsupported != null) {
      return SolveResult.statusOnly(SolverStatus.UNSUPPORTED, unsupported);
    }
    if (columns == 0) {
      return solveZeroDimensional(problem);
    }
    if (constraints.isEmpty()) {
      return unconstrainedNonnegative(problem);
    }
    Tableau tableau = Tableau.from(columns, constraints);
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

  private static String addVariableConstraints(
      final LpProblem problem, final List<LinearConstraint> constraints) {
    int columns = problem.stats().columns();
    for (int column = 0; column < columns; column++) {
      LpVariableBounds bounds = problem.variableBounds().get(column);
      if (Math.abs(bounds.lower()) > EPSILON) {
        return "performance solver supports only zero lower bounds";
      }
      if (Double.isFinite(bounds.upper())) {
        double[] coefficients = new double[columns];
        coefficients[column] = 1.0d;
        constraints.add(LinearConstraint.le(coefficients, bounds.upper()));
      }
    }
    return null;
  }

  private static String addRowConstraints(
      final SolverInput input, final List<LinearConstraint> constraints) {
    LpProblem problem = input.problem();
    double[][] rows = denseRows(input.matrix());
    for (int row = 0; row < problem.rowBounds().size(); row++) {
      LpRowBounds bounds = problem.rowBounds().get(row);
      if (bounds.isEquality()) {
        constraints.add(LinearConstraint.eq(rows[row], bounds.upper()));
      } else if (!Double.isFinite(bounds.lower()) && Double.isFinite(bounds.upper())) {
        constraints.add(LinearConstraint.le(rows[row], bounds.upper()));
      } else if (Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
        constraints.add(LinearConstraint.ge(rows[row], bounds.lower()));
      } else if (!Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
        continue;
      } else {
        return "performance solver does not support ranged rows";
      }
    }
    return null;
  }

  private static double[][] denseRows(final CsrMatrix matrix) {
    double[][] rows = new double[matrix.rows()][matrix.columns()];
    double[] values = matrix.values();
    int[] columnIndices = matrix.columnIndices();
    int[] rowPointers = matrix.rowPointers();
    for (int row = 0; row < matrix.rows(); row++) {
      for (int offset = rowPointers[row]; offset < rowPointers[row + 1]; offset++) {
        rows[row][columnIndices[offset]] += values[offset];
      }
    }
    return rows;
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

  record LinearConstraint(double[] coefficients, Relation relation, double rhs) {
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
      Relation normalizedRelation = relation;
      double normalizedRhs = rhs;
      if (normalizedRhs < 0.0d) {
        for (int index = 0; index < copy.length; index++) {
          copy[index] = -copy[index];
        }
        normalizedRhs = -normalizedRhs;
        if (normalizedRelation == Relation.LE) {
          normalizedRelation = Relation.GE;
        } else if (normalizedRelation == Relation.GE) {
          normalizedRelation = Relation.LE;
        }
      }
      return new LinearConstraint(copy, normalizedRelation, normalizedRhs);
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
      int variableCount = originalColumns + extraColumns;
      double[][] rows = new double[constraints.size() + 1][variableCount + 1];
      int[] basis = new int[constraints.size()];
      boolean[] artificial = new boolean[variableCount];
      int nextColumn = originalColumns;
      for (int row = 0; row < constraints.size(); row++) {
        LinearConstraint constraint = constraints.get(row);
        System.arraycopy(constraint.coefficients(), 0, rows[row], 0, originalColumns);
        rows[row][variableCount] = constraint.rhs();
        if (constraint.relation() == Relation.LE) {
          rows[row][nextColumn] = 1.0d;
          basis[row] = nextColumn;
          nextColumn++;
        } else if (constraint.relation() == Relation.GE) {
          rows[row][nextColumn] = -1.0d;
          nextColumn++;
          rows[row][nextColumn] = 1.0d;
          artificial[nextColumn] = true;
          basis[row] = nextColumn;
          nextColumn++;
        } else {
          rows[row][nextColumn] = 1.0d;
          artificial[nextColumn] = true;
          basis[row] = nextColumn;
          nextColumn++;
        }
      }
      return new Tableau(rows, basis, artificial, variableCount);
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
}
