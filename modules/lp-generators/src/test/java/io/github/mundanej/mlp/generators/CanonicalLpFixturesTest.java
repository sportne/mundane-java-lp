package io.github.mundanej.mlp.generators;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

final class CanonicalLpFixturesTest {
  @Test
  void providesAllTierOneFixtures() {
    assertEquals(10, CanonicalLpFixtures.tierOne().size());
  }

  @Test
  void fixturesHaveConsistentShapesAndNames() {
    for (CanonicalLpFixture fixture : CanonicalLpFixtures.tierOne()) {
      assertEquals(
          fixture.problem().stats().rows(), fixture.matrix().rows(), fixture.problem().name());
      assertEquals(
          fixture.problem().stats().columns(),
          fixture.matrix().columns(),
          fixture.problem().name());
      assertEquals(
          fixture.problem().rowBounds().size(),
          fixture.rowNames().size(),
          fixture.problem().name());
      assertEquals(
          fixture.problem().variableBounds().size(),
          fixture.columnNames().size(),
          fixture.problem().name());
    }
  }

  @Test
  void optimalFixtureEvidenceMatchesObjectiveAndRows() {
    for (CanonicalLpFixture fixture : CanonicalLpFixtures.tierOne()) {
      if (fixture.evidence().resultKind() != ExpectedResultKind.OPTIMAL) {
        continue;
      }
      double[] primal = fixture.evidence().primal();
      assertEquals(
          fixture.evidence().objectiveValue().orElseThrow(),
          fixture.problem().objective().evaluate(primal),
          1.0e-12,
          fixture.problem().name());
      double[] activities = fixture.matrix().multiply(primal);
      List<LpRowBounds> rowBounds = fixture.problem().rowBounds();
      for (int index = 0; index < activities.length; index++) {
        assertTrue(
            activities[index] >= rowBounds.get(index).lower() - 1.0e-12, fixture.problem().name());
        assertTrue(
            activities[index] <= rowBounds.get(index).upper() + 1.0e-12, fixture.problem().name());
      }
    }
  }

  @Test
  void nonOptimalFixtureEvidenceDoesNotCarryPrimalOrObjective() {
    CanonicalLpFixture infeasible = CanonicalLpFixtures.infeasibleProblem();
    assertEquals(ExpectedResultKind.INFEASIBLE, infeasible.evidence().resultKind());
    assertFalse(infeasible.evidence().objectiveValue().isPresent());
    assertArrayEquals(new double[0], infeasible.evidence().primal());

    CanonicalLpFixture unbounded = CanonicalLpFixtures.unboundedProblem();
    assertEquals(ExpectedResultKind.UNBOUNDED, unbounded.evidence().resultKind());
    assertFalse(unbounded.evidence().objectiveValue().isPresent());
    assertArrayEquals(new double[0], unbounded.evidence().primal());
  }

  @Test
  void rejectsInvalidOptimalEvidence() {
    assertThrows(IllegalArgumentException.class, () -> fixtureWithEvidence(optimal(0.0d, -1.0d)));
    assertThrows(IllegalArgumentException.class, () -> fixtureWithEvidence(optimal(2.0d, 1.0d)));
    assertThrows(
        IllegalArgumentException.class, () -> fixtureWithEvidence(optimal(0.0d, 1.0d, 2.0d)));
  }

  @Test
  void rejectsInvalidDiagnosticNames() {
    assertThrows(
        IllegalArgumentException.class, () -> fixtureWithNames(List.of(" "), List.of("x")));
    assertThrows(
        IllegalArgumentException.class,
        () -> fixtureWithNames(List.of("limit"), List.of("x", "x")));
  }

  private static CanonicalLpFixture fixtureWithEvidence(final LpFixtureEvidence evidence) {
    return new CanonicalLpFixture(
        baseProblem(), baseMatrix(), List.of("limit"), List.of("x"), evidence);
  }

  private static CanonicalLpFixture fixtureWithNames(
      final List<String> rowNames, final List<String> columnNames) {
    return new CanonicalLpFixture(
        baseProblem(), baseMatrix(), rowNames, columnNames, optimal(1.0d, 1.0d));
  }

  private static LpProblem baseProblem() {
    return new LpProblem(
        "fixture-validation",
        new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
        List.of(new LpVariableBounds(0.0d, 1.0d)),
        List.of(new LpRowBounds(Double.NEGATIVE_INFINITY, 1.0d)),
        new LpProblemStats(1, 1, 1));
  }

  private static CsrMatrix baseMatrix() {
    return new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 1});
  }

  private static LpFixtureEvidence optimal(final double objectiveValue, final double... primal) {
    return new LpFixtureEvidence(
        ExpectedResultKind.OPTIMAL, OptionalDouble.of(objectiveValue), primal);
  }
}
