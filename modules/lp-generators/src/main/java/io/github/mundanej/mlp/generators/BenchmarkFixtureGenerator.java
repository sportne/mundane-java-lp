package io.github.mundanej.mlp.generators;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

/** Deterministic generated benchmark families for expanded benchmark evidence. */
public final class BenchmarkFixtureGenerator {
  /** Generator name recorded in generated instance metadata. */
  public static final String GENERATOR_NAME = "benchmark-fixtures-v1";

  /** Returns the deterministic expanded generated benchmark suite. */
  public List<GeneratedLpInstance> suite() {
    return List.of(
        smallDense(),
        sparse(),
        new NetworkFlowGenerator().threeNode(19L),
        equalityHeavy(),
        degenerate(),
        scaled());
  }

  /** Returns a small dense two-row fixture. */
  public GeneratedLpInstance smallDense() {
    CanonicalLpFixture fixture =
        fixture(
            "benchmark-small-dense",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0d, 2.0d},
            List.of(nonnegative(), nonnegative()),
            List.of(lower(4.0d), lower(5.0d)),
            new double[] {1.0d, 1.0d, 2.0d, 1.0d},
            new int[] {0, 1, 0, 1},
            new int[] {0, 2, 4},
            List.of("demand", "coverage"),
            List.of("x", "y"),
            optimal(4.0d, 4.0d, 0.0d));
    return generated(fixture, 1001L, Map.of("family", 1, "rows", 2, "columns", 2, "nonzeros", 4));
  }

  /** Returns a sparse rectangular fixture. */
  public GeneratedLpInstance sparse() {
    CanonicalLpFixture fixture =
        fixture(
            "benchmark-sparse",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0d, 2.0d, 4.0d, 3.0d},
            List.of(nonnegative(), nonnegative(), bounded(0.0d, 5.0d), nonnegative()),
            List.of(lower(2.0d), lower(1.0d)),
            new double[] {1.0d, 1.0d, 1.0d},
            new int[] {0, 3, 1},
            new int[] {0, 2, 3},
            List.of("sparse-demand", "single-column-demand"),
            List.of("x", "y", "z", "w"),
            optimal(4.0d, 2.0d, 1.0d, 0.0d, 0.0d));
    return generated(fixture, 1002L, Map.of("family", 2, "rows", 2, "columns", 4, "nonzeros", 3));
  }

  /** Returns an equality-heavy fixture with a hand-checked optimum. */
  public GeneratedLpInstance equalityHeavy() {
    CanonicalLpFixture fixture =
        fixture(
            "benchmark-equality-heavy",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0d, 1.0d, 1.0d},
            List.of(nonnegative(), nonnegative(), nonnegative()),
            List.of(equal(3.0d), equal(2.0d)),
            new double[] {1.0d, 1.0d, 1.0d, 1.0d},
            new int[] {0, 1, 1, 2},
            new int[] {0, 2, 4},
            List.of("left-balance", "right-balance"),
            List.of("x", "y", "z"),
            optimal(3.0d, 1.0d, 2.0d, 0.0d));
    return generated(fixture, 1003L, Map.of("family", 3, "rows", 2, "columns", 3, "equalities", 2));
  }

  /** Returns a degenerate fixture with duplicate active row activity. */
  public GeneratedLpInstance degenerate() {
    CanonicalLpFixture fixture =
        fixture(
            "benchmark-degenerate",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0d, 0.0d},
            List.of(nonnegative(), nonnegative()),
            List.of(lower(1.0d), lower(1.0d), upper(1.0d)),
            new double[] {1.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d},
            new int[] {0, 1, 0, 1, 0, 1},
            new int[] {0, 2, 4, 6},
            List.of("lower-one", "duplicate-lower-one", "upper-one"),
            List.of("x", "y"),
            optimal(0.0d, 0.0d, 1.0d));
    return generated(
        fixture, 1004L, Map.of("family", 4, "rows", 3, "columns", 2, "duplicateRows", 1));
  }

  /** Returns a scaled-coefficient fixture with deterministic evidence. */
  public GeneratedLpInstance scaled() {
    CanonicalLpFixture fixture =
        fixture(
            "benchmark-scaled",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0e-3d, 1.0d},
            List.of(nonnegative(), nonnegative()),
            List.of(lower(1.0d)),
            new double[] {1.0e-3d, 1.0d},
            new int[] {0, 1},
            new int[] {0, 2},
            List.of("scaled-demand"),
            List.of("large", "small"),
            optimal(1.0d, 1000.0d, 0.0d));
    return generated(
        fixture, 1005L, Map.of("family", 5, "rows", 1, "columns", 2, "scaleExponent", -3));
  }

  private static GeneratedLpInstance generated(
      final CanonicalLpFixture fixture,
      final long seed,
      final Map<String, Integer> sizeParameters) {
    return new GeneratedLpInstance(
        fixture.problem().name(), GENERATOR_NAME, seed, sizeParameters, fixture);
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
    return new CanonicalLpFixture(
        new LpProblem(
            name,
            new LpObjective(sense, 0.0d, objective),
            variableBounds,
            rowBounds,
            new LpProblemStats(rowBounds.size(), variableBounds.size(), values.length)),
        new CsrMatrix(rowBounds.size(), variableBounds.size(), values, columnIndices, rowPointers),
        rowNames,
        columnNames,
        evidence);
  }

  private static LpVariableBounds nonnegative() {
    return new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY);
  }

  private static LpVariableBounds bounded(final double lower, final double upper) {
    return new LpVariableBounds(lower, upper);
  }

  private static LpRowBounds lower(final double lower) {
    return new LpRowBounds(lower, Double.POSITIVE_INFINITY);
  }

  private static LpRowBounds upper(final double upper) {
    return new LpRowBounds(Double.NEGATIVE_INFINITY, upper);
  }

  private static LpRowBounds equal(final double value) {
    return new LpRowBounds(value, value);
  }

  private static LpFixtureEvidence optimal(final double objectiveValue, final double... primal) {
    return new LpFixtureEvidence(
        ExpectedResultKind.OPTIMAL, OptionalDouble.of(objectiveValue), primal);
  }
}
