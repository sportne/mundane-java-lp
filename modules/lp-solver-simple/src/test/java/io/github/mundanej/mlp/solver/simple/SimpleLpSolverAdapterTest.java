package io.github.mundanej.mlp.solver.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import io.github.mundanej.mlp.testkit.LpTestInstances;
import io.github.mundanej.mlp.validation.LpSolutionValidator;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationEvidence;
import io.github.mundanej.mlp.validation.ValidationReport;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SimpleLpSolverAdapterTest {
  @TempDir private Path tempDir;

  @Test
  void exposesStableSolverId() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    assertEquals("simple", adapter.id().name());
    assertEquals("java", adapter.id().mode());
  }

  @Test
  void validatesAllTierOneFixtures() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();
    LpSolutionValidator validator = new LpSolutionValidator();

    for (CanonicalLpFixture fixture : LpTestInstances.tierOneFixtures()) {
      SolverRunResult result = adapter.solve(input(fixture), SolverOptions.defaults(), work());

      assertEquals(expectedStatus(fixture), result.status(), fixture.problem().name());
      ValidationReport report =
          validator.validate(
              fixture.problem(),
              fixture.matrix(),
              LpTestInstances.expectedValidationResult(fixture),
              validationEvidence(result),
              ToleranceProfile.STANDARD);
      assertTrue(report.accepted(), () -> fixture.problem().name() + " " + report.findings());
    }
  }

  @Test
  void solvesEmptyLpSmokePath() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result = adapter.solve(emptyInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(7.0d, result.objectiveValue().orElseThrow());
    assertEquals(0, result.primalValues().length);
    assertTrue(result.message().contains("empty LP"));
  }

  @Test
  void rejectsInfeasibleEmptyLpRows() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(infeasibleEmptyInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.INFEASIBLE, result.status());
    assertTrue(result.objectiveValue().isEmpty());
  }

  @Test
  void rejectsInfeasibleZeroCoefficientOneDimensionalRow() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(
            infeasibleZeroCoefficientOneVariableInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.INFEASIBLE, result.status());
  }

  @Test
  void choosesFiniteEndpointForConstantOneDimensionalObjective() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(constantObjectiveOneVariableInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(2.0d, result.primalValues()[0]);
    assertEquals(5.0d, result.objectiveValue().orElseThrow());
  }

  @Test
  void sumsDuplicateSparseEntries() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(duplicateEntryOneVariableInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(2.0d, result.primalValues()[0]);
  }

  @Test
  void rejectsInfeasibleZeroCoefficientTwoDimensionalRow() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(
            infeasibleZeroCoefficientTwoVariableInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.INFEASIBLE, result.status());
  }

  @Test
  void reportsUnboundedTwoDimensionalRay() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(unboundedTwoVariableInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.UNBOUNDED, result.status());
    assertTrue(result.objectiveValue().isEmpty());
    assertEquals(0, result.primalValues().length);
  }

  @Test
  void reportsUnboundedTwoDimensionalMinimizationRay() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(unboundedTwoVariableMinimizationInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.UNBOUNDED, result.status());
    assertTrue(result.objectiveValue().isEmpty());
  }

  @Test
  void reportsUnsupportedForLargerShapes() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result = adapter.solve(threeVariableInput(), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.objectiveValue().isEmpty());
    assertEquals(0, result.primalValues().length);
    assertTrue(result.message().contains("at most two variables"));
  }

  @Test
  void rejectsNullInputs() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();
    SolverInput input = emptyInput();

    assertThrows(
        NullPointerException.class, () -> adapter.solve(null, SolverOptions.defaults(), work()));
    assertThrows(NullPointerException.class, () -> adapter.solve(input, null, work()));
    assertThrows(
        NullPointerException.class, () -> adapter.solve(input, SolverOptions.defaults(), null));
  }

  private static SolverInput emptyInput() {
    LpProblem problem =
        new LpProblem(
            "empty",
            new LpObjective(ObjectiveSense.MINIMIZE, 7.0d, new double[0]),
            List.of(),
            List.of(),
            new LpProblemStats(0, 0, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 0, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput infeasibleEmptyInput() {
    LpProblem problem =
        new LpProblem(
            "empty-infeasible-row",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[0]),
            List.of(),
            List.of(new LpRowBounds(1.0d, Double.POSITIVE_INFINITY)),
            new LpProblemStats(1, 0, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(1, 0, new double[0], new int[0], new int[] {0, 0}));
  }

  private static SolverInput infeasibleZeroCoefficientOneVariableInput() {
    LpProblem problem =
        new LpProblem(
            "one-variable-zero-row-infeasible",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(LpVariableBounds.FREE),
            List.of(new LpRowBounds(1.0d, Double.POSITIVE_INFINITY)),
            new LpProblemStats(1, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(1, 1, new double[0], new int[0], new int[] {0, 0}));
  }

  private static SolverInput constantObjectiveOneVariableInput() {
    LpProblem problem =
        new LpProblem(
            "one-variable-constant-objective",
            new LpObjective(ObjectiveSense.MINIMIZE, 5.0d, new double[] {0.0d}),
            List.of(new LpVariableBounds(2.0d, 3.0d)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput duplicateEntryOneVariableInput() {
    LpProblem problem =
        new LpProblem(
            "one-variable-duplicate-entry",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(new LpRowBounds(4.0d, Double.POSITIVE_INFINITY)),
            new LpProblemStats(1, 1, 2));
    return SolverInput.withGeneratedNames(
        problem,
        new CsrMatrix(1, 1, new double[] {1.0d, 1.0d}, new int[] {0, 0}, new int[] {0, 2}));
  }

  private static SolverInput infeasibleZeroCoefficientTwoVariableInput() {
    LpProblem problem =
        new LpProblem(
            "two-variable-zero-row-infeasible",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d, 1.0d}),
            List.of(LpVariableBounds.FREE, LpVariableBounds.FREE),
            List.of(new LpRowBounds(1.0d, Double.POSITIVE_INFINITY)),
            new LpProblemStats(1, 2, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(1, 2, new double[0], new int[0], new int[] {0, 0}));
  }

  private static SolverInput unboundedTwoVariableInput() {
    LpProblem problem =
        new LpProblem(
            "two-variable-unbounded-ray",
            new LpObjective(ObjectiveSense.MAXIMIZE, 0.0d, new double[] {1.0d, 0.0d}),
            List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY), LpVariableBounds.FREE),
            List.of(new LpRowBounds(0.0d, 0.0d)),
            new LpProblemStats(1, 2, 1));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(1, 2, new double[] {1.0d}, new int[] {1}, new int[] {0, 1}));
  }

  private static SolverInput unboundedTwoVariableMinimizationInput() {
    LpProblem problem =
        new LpProblem(
            "two-variable-minimization-ray",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d, 0.0d}),
            List.of(LpVariableBounds.FREE, new LpVariableBounds(0.0d, 0.0d)),
            List.of(),
            new LpProblemStats(0, 2, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 2, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput threeVariableInput() {
    LpProblem problem =
        new LpProblem(
            "three-variable",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d, 0.0d, 0.0d}),
            List.of(
                new LpVariableBounds(0.0d, 1.0d),
                new LpVariableBounds(0.0d, 1.0d),
                new LpVariableBounds(0.0d, 1.0d)),
            List.of(),
            new LpProblemStats(0, 3, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 3, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput input(final CanonicalLpFixture fixture) {
    return new SolverInput(
        fixture.problem(), fixture.matrix(), fixture.rowNames(), fixture.columnNames(), "OBJ");
  }

  private SolverWorkDirectory work() {
    return new SolverWorkDirectory(tempDir);
  }

  private static SolverStatus expectedStatus(final CanonicalLpFixture fixture) {
    return switch (fixture.evidence().resultKind()) {
      case OPTIMAL -> SolverStatus.OPTIMAL;
      case INFEASIBLE -> SolverStatus.INFEASIBLE;
      case UNBOUNDED -> SolverStatus.UNBOUNDED;
    };
  }

  private static ValidationEvidence validationEvidence(final SolverRunResult result) {
    return new ValidationEvidence(
        Optional.of(validationStatus(result.status())),
        result.objectiveValue(),
        result.primalValues());
  }

  private static ValidationStatus validationStatus(final SolverStatus status) {
    return switch (status) {
      case OPTIMAL -> ValidationStatus.OPTIMAL;
      case FEASIBLE -> ValidationStatus.FEASIBLE;
      case INFEASIBLE -> ValidationStatus.INFEASIBLE;
      case UNBOUNDED -> ValidationStatus.UNBOUNDED;
      case INFEASIBLE_OR_UNBOUNDED -> ValidationStatus.INFEASIBLE_OR_UNBOUNDED;
      case TIME_LIMIT -> ValidationStatus.TIME_LIMIT;
      case MEMORY_LIMIT -> ValidationStatus.MEMORY_LIMIT;
      case NUMERICAL_FAILURE -> ValidationStatus.NUMERICAL_FAILURE;
      case ERROR -> ValidationStatus.ERROR;
      case UNSUPPORTED -> ValidationStatus.UNSUPPORTED;
      case UNKNOWN -> ValidationStatus.UNKNOWN;
    };
  }
}
