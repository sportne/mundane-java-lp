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

/** Deterministic small numerical stress fixtures for solver validation. */
public final class NumericalStressGenerator {
  /** Generator name recorded in generated instance metadata. */
  public static final String GENERATOR_NAME = "numerical-stress-v1";

  /** Returns deterministic scaling, degeneracy, tight-bound, and ill-conditioned cases. */
  public List<GeneratedLpInstance> suite() {
    return List.of(scaling(), degeneracy(), tightBounds(), illConditionedUnsupported());
  }

  /** Returns a scaled-coefficient case with a hand-checked optimum. */
  public GeneratedLpInstance scaling() {
    CanonicalLpFixture fixture =
        fixture(
            "stress-scaling",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0e-6d, 1.0d},
            List.of(
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY),
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(new LpRowBounds(1.0d, Double.POSITIVE_INFINITY)),
            new double[] {1.0e-6d, 1.0d},
            new int[] {0, 1},
            new int[] {0, 2},
            List.of("scaled-demand"),
            List.of("large", "small"),
            optimal(1.0d, 1.0e6d, 0.0d));
    return generated(
        fixture, 101L, Map.of("family", 1, "rows", 1, "columns", 2, "scaleExponent", -6));
  }

  /** Returns a degenerate optimum case with duplicate active constraints. */
  public GeneratedLpInstance degeneracy() {
    CanonicalLpFixture fixture =
        fixture(
            "stress-degeneracy",
            ObjectiveSense.MINIMIZE,
            new double[] {0.0d, 0.0d},
            List.of(
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY),
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(
                new LpRowBounds(1.0d, Double.POSITIVE_INFINITY),
                new LpRowBounds(1.0d, Double.POSITIVE_INFINITY),
                new LpRowBounds(Double.NEGATIVE_INFINITY, 1.0d)),
            new double[] {1.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d},
            new int[] {0, 1, 0, 1, 0, 1},
            new int[] {0, 2, 4, 6},
            List.of("lower-one", "duplicate-lower-one", "upper-one"),
            List.of("x", "y"),
            optimal(0.0d, 1.0d, 0.0d));
    return generated(
        fixture, 202L, Map.of("family", 2, "rows", 3, "columns", 2, "duplicateRows", 1));
  }

  /** Returns a tight-bound case that should remain feasible at standard tolerance. */
  public GeneratedLpInstance tightBounds() {
    CanonicalLpFixture fixture =
        fixture(
            "stress-tight-bounds",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0d},
            List.of(new LpVariableBounds(0.0d, 1.0e-9d)),
            List.of(new LpRowBounds(1.0e-9d, Double.POSITIVE_INFINITY)),
            new double[] {1.0d},
            new int[] {0},
            new int[] {0, 1},
            List.of("tight-lower"),
            List.of("x"),
            optimal(1.0e-9d, 1.0e-9d));
    return generated(
        fixture, 303L, Map.of("family", 3, "rows", 1, "columns", 1, "widthExponent", -9));
  }

  /** Returns an ill-conditioned ranged row that is intentionally unsupported for now. */
  public GeneratedLpInstance illConditionedUnsupported() {
    CanonicalLpFixture fixture =
        fixture(
            "stress-ill-conditioned-ranged",
            ObjectiveSense.MINIMIZE,
            new double[] {1.0d, 1.0d},
            List.of(
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY),
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(new LpRowBounds(1.0d, 1.0d + 1.0e-10d)),
            new double[] {1.0d, 1.0d + 1.0e-10d},
            new int[] {0, 1},
            new int[] {0, 2},
            List.of("nearly-parallel-range"),
            List.of("x", "y"),
            optimal(1.0d / (1.0d + 1.0e-10d), 0.0d, 1.0d / (1.0d + 1.0e-10d)));
    return generated(fixture, 404L, Map.of("family", 4, "rows", 1, "columns", 2, "ranged", 1));
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

  private static LpFixtureEvidence optimal(final double objectiveValue, final double... primal) {
    return new LpFixtureEvidence(
        ExpectedResultKind.OPTIMAL, OptionalDouble.of(objectiveValue), primal);
  }
}
