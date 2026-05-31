package io.github.mundanej.mlp.generators;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import java.util.OptionalDouble;

/** Tier 1 hand-checkable canonical LP fixtures. */
public final class CanonicalLpFixtures {
  private CanonicalLpFixtures() {}

  /** Returns all Tier 1 fixtures in stable suite order. */
  public static List<CanonicalLpFixture> tierOne() {
    return List.of(
        singleBoundedVariable(),
        twoVariableFeasibleOptimum(),
        infeasibleProblem(),
        unboundedProblem(),
        redundantRow(),
        fixedVariable(),
        freeVariable(),
        equalityRow(),
        rangedRow(),
        degenerateOptimum());
  }

  /** Returns min x with {@code 0 <= x <= 1}, optimum x = 0. */
  public static CanonicalLpFixture singleBoundedVariable() {
    return fixture(
        "single-bounded-variable",
        ObjectiveSense.MINIMIZE,
        new double[] {1.0d},
        List.of(new LpVariableBounds(0.0d, 1.0d)),
        List.of(),
        new double[0],
        new int[0],
        new int[] {0},
        List.of(),
        List.of("x"),
        optimal(0.0d, 0.0d));
  }

  /** Returns a two-variable bounded optimum, max {@code 3x + 2y = 10}. */
  public static CanonicalLpFixture twoVariableFeasibleOptimum() {
    return fixture(
        "two-variable-feasible-optimum",
        ObjectiveSense.MAXIMIZE,
        new double[] {3.0d, 2.0d},
        nonNegativeVariables(2),
        List.of(
            new LpRowBounds(Double.NEGATIVE_INFINITY, 4.0d),
            new LpRowBounds(Double.NEGATIVE_INFINITY, 2.0d),
            new LpRowBounds(Double.NEGATIVE_INFINITY, 3.0d)),
        new double[] {1.0d, 1.0d, 1.0d, 1.0d},
        new int[] {0, 1, 0, 1},
        new int[] {0, 2, 3, 4},
        List.of("capacity", "x-limit", "y-limit"),
        List.of("x", "y"),
        optimal(10.0d, 2.0d, 2.0d));
  }

  /** Returns a valid LP with contradictory rows. */
  public static CanonicalLpFixture infeasibleProblem() {
    return fixture(
        "infeasible-rows",
        ObjectiveSense.MINIMIZE,
        new double[] {0.0d},
        List.of(LpVariableBounds.FREE),
        List.of(
            new LpRowBounds(1.0d, Double.POSITIVE_INFINITY),
            new LpRowBounds(Double.NEGATIVE_INFINITY, 0.0d)),
        new double[] {1.0d, 1.0d},
        new int[] {0, 0},
        new int[] {0, 1, 2},
        List.of("x-at-least-one", "x-at-most-zero"),
        List.of("x"),
        nonOptimal(ExpectedResultKind.INFEASIBLE));
  }

  /** Returns max x with {@code x >= 0}, which is unbounded. */
  public static CanonicalLpFixture unboundedProblem() {
    return fixture(
        "unbounded-nonnegative-ray",
        ObjectiveSense.MAXIMIZE,
        new double[] {1.0d},
        List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
        List.of(),
        new double[0],
        new int[0],
        new int[] {0},
        List.of(),
        List.of("x"),
        nonOptimal(ExpectedResultKind.UNBOUNDED));
  }

  /** Returns min x with one active and one redundant lower row. */
  public static CanonicalLpFixture redundantRow() {
    return fixture(
        "redundant-row",
        ObjectiveSense.MINIMIZE,
        new double[] {1.0d},
        List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
        List.of(
            new LpRowBounds(1.0d, Double.POSITIVE_INFINITY),
            new LpRowBounds(0.0d, Double.POSITIVE_INFINITY)),
        new double[] {1.0d, 1.0d},
        new int[] {0, 0},
        new int[] {0, 1, 2},
        List.of("active-lower", "redundant-lower"),
        List.of("x"),
        optimal(1.0d, 1.0d));
  }

  /** Returns a fixed-variable fixture, min x with {@code x = 2}. */
  public static CanonicalLpFixture fixedVariable() {
    return fixture(
        "fixed-variable",
        ObjectiveSense.MINIMIZE,
        new double[] {1.0d},
        List.of(new LpVariableBounds(2.0d, 2.0d)),
        List.of(),
        new double[0],
        new int[0],
        new int[] {0},
        List.of(),
        List.of("x"),
        optimal(2.0d, 2.0d));
  }

  /** Returns a free-variable fixture bounded only by rows. */
  public static CanonicalLpFixture freeVariable() {
    return fixture(
        "free-variable-row-bounded",
        ObjectiveSense.MINIMIZE,
        new double[] {1.0d},
        List.of(LpVariableBounds.FREE),
        List.of(new LpRowBounds(-1.0d, 3.0d)),
        new double[] {1.0d},
        new int[] {0},
        new int[] {0, 1},
        List.of("range"),
        List.of("x"),
        optimal(-1.0d, -1.0d));
  }

  /** Returns an equality-row fixture with optimum x = 0, y = 5. */
  public static CanonicalLpFixture equalityRow() {
    return fixture(
        "equality-row",
        ObjectiveSense.MINIMIZE,
        new double[] {1.0d, 0.0d},
        nonNegativeVariables(2),
        List.of(new LpRowBounds(5.0d, 5.0d)),
        new double[] {1.0d, 1.0d},
        new int[] {0, 1},
        new int[] {0, 2},
        List.of("balance"),
        List.of("x", "y"),
        optimal(0.0d, 0.0d, 5.0d));
  }

  /** Returns a ranged-row fixture with optimum x = 3. */
  public static CanonicalLpFixture rangedRow() {
    return fixture(
        "ranged-row",
        ObjectiveSense.MAXIMIZE,
        new double[] {1.0d},
        List.of(LpVariableBounds.FREE),
        List.of(new LpRowBounds(1.0d, 3.0d)),
        new double[] {1.0d},
        new int[] {0},
        new int[] {0, 1},
        List.of("range"),
        List.of("x"),
        optimal(3.0d, 3.0d));
  }

  /** Returns a fixture with multiple optimal primal vectors. */
  public static CanonicalLpFixture degenerateOptimum() {
    return fixture(
        "degenerate-optimum",
        ObjectiveSense.MINIMIZE,
        new double[] {0.0d, 0.0d},
        nonNegativeVariables(2),
        List.of(new LpRowBounds(1.0d, 1.0d)),
        new double[] {1.0d, 1.0d},
        new int[] {0, 1},
        new int[] {0, 2},
        List.of("balance"),
        List.of("x", "y"),
        optimal(0.0d, 0.0d, 1.0d));
  }

  private static CanonicalLpFixture fixture(
      final String name,
      final ObjectiveSense sense,
      final double[] objective,
      final List<LpVariableBounds> variableBounds,
      final List<LpRowBounds> rowBounds,
      final double[] values,
      final int[] columnIndices,
      final int[] rowPointers,
      final List<String> rowNames,
      final List<String> columnNames,
      final LpFixtureEvidence evidence) {
    CsrMatrix matrix =
        matrix(rowBounds.size(), variableBounds.size(), values, columnIndices, rowPointers);
    return new CanonicalLpFixture(
        new LpProblem(
            name,
            new LpObjective(sense, 0.0d, objective),
            variableBounds,
            rowBounds,
            new LpProblemStats(rowBounds.size(), variableBounds.size(), values.length)),
        matrix,
        rowNames,
        columnNames,
        evidence);
  }

  private static CsrMatrix matrix(
      final int rows,
      final int columns,
      final double[] values,
      final int[] columnIndices,
      final int[] rowPointers) {
    return new CsrMatrix(rows, columns, values, columnIndices, rowPointers);
  }

  private static List<LpVariableBounds> nonNegativeVariables(final int count) {
    return java.util.stream.IntStream.range(0, count)
        .mapToObj(index -> new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY))
        .toList();
  }

  private static LpFixtureEvidence optimal(final double objectiveValue, final double... primal) {
    return new LpFixtureEvidence(
        ExpectedResultKind.OPTIMAL, OptionalDouble.of(objectiveValue), primal);
  }

  private static LpFixtureEvidence nonOptimal(final ExpectedResultKind resultKind) {
    return new LpFixtureEvidence(resultKind, OptionalDouble.empty(), new double[0]);
  }
}
