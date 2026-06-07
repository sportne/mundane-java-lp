package io.github.mundanej.mlp.model;

import java.util.List;
import java.util.Objects;

/** Canonical LP problem metadata: objective, bounds, and matrix shape. */
public final class LpProblem {
  private final String name;
  private final LpObjective objective;
  private final List<LpVariableBounds> variableBounds;
  private final List<LpRowBounds> rowBounds;
  private final LpProblemStats stats;

  /**
   * Creates a problem descriptor.
   *
   * @param name nonblank problem name used in diagnostics and file formats
   * @param objective objective function whose coefficient count equals the variable count
   * @param variableBounds variable bounds in column order; copied into an immutable list
   * @param rowBounds row activity bounds in row order; copied into an immutable list
   * @param stats problem shape statistics matching the bounds and coefficient matrix
   */
  public LpProblem(
      final String name,
      final LpObjective objective,
      final List<LpVariableBounds> variableBounds,
      final List<LpRowBounds> rowBounds,
      final LpProblemStats stats) {
    this.name = requireNonBlank(name, "name");
    this.objective = Objects.requireNonNull(objective, "objective");
    this.variableBounds = List.copyOf(Objects.requireNonNull(variableBounds, "variableBounds"));
    this.rowBounds = List.copyOf(Objects.requireNonNull(rowBounds, "rowBounds"));
    this.stats = Objects.requireNonNull(stats, "stats");
    if (objective.size() != variableBounds.size()) {
      throw new IllegalArgumentException("objective size must equal variable count");
    }
    if (stats.columns() != variableBounds.size()) {
      throw new IllegalArgumentException("stats columns must equal variable count");
    }
    if (stats.rows() != rowBounds.size()) {
      throw new IllegalArgumentException("stats rows must equal row count");
    }
  }

  /** Returns the problem name. */
  public String name() {
    return name;
  }

  /** Returns the objective. */
  public LpObjective objective() {
    return objective;
  }

  /** Returns immutable variable bounds in column order. */
  public List<LpVariableBounds> variableBounds() {
    return variableBounds;
  }

  /** Returns immutable row activity bounds in row order. */
  public List<LpRowBounds> rowBounds() {
    return rowBounds;
  }

  /** Returns the declared row, column, and nonzero counts. */
  public LpProblemStats stats() {
    return stats;
  }

  private static String requireNonBlank(final String value, final String name) {
    Objects.requireNonNull(value, name);
    if (value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value;
  }
}
