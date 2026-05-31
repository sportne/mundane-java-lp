package io.github.mundanej.mlp.solver.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class PerformanceLpSolverAdapterTest {
  @TempDir private Path tempDir;

  @Test
  void exposesStableSolverId() {
    PerformanceLpSolverAdapter adapter = new PerformanceLpSolverAdapter();

    assertEquals("performance", adapter.id().name());
    assertEquals("java", adapter.id().mode());
  }

  @Test
  void solvesEmptyLpSmokePath() {
    SolverRunResult result = solve(emptyInput());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(3.0d, result.objectiveValue().orElseThrow());
    assertEquals(0, result.primalValues().length);
    assertTrue(result.message().contains("optimal"));
  }

  @Test
  void solvesOneVariableBoundedMinimization() {
    SolverRunResult result = solve(oneVariableInput());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(0.0d, result.objectiveValue().orElseThrow());
    assertEquals(0.0d, result.primalValues()[0]);
  }

  @Test
  void solvesTwoVariableMaximizationSmokePath() {
    SolverRunResult result = solve(twoVariableMaximizationInput());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(10.0d, result.objectiveValue().orElseThrow());
    assertEquals(2.0d, result.primalValues()[0]);
    assertEquals(2.0d, result.primalValues()[1]);
  }

  @Test
  void reportsInfeasibleForContradictoryRows() {
    SolverRunResult result = solve(infeasibleInput());

    assertEquals(SolverStatus.INFEASIBLE, result.status());
    assertTrue(result.objectiveValue().isEmpty());
  }

  @Test
  void reportsUnboundedForImprovingRay() {
    SolverRunResult result = solve(unboundedInput());

    assertEquals(SolverStatus.UNBOUNDED, result.status());
    assertTrue(result.objectiveValue().isEmpty());
  }

  @Test
  void reportsUnsupportedForFreeVariables() {
    SolverRunResult result = solve(freeVariableInput());

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.objectiveValue().isEmpty());
    assertEquals(0, result.primalValues().length);
    assertTrue(result.message().contains("zero lower bounds"));
  }

  @Test
  void reportsInfeasibleForZeroColumnRowViolation() {
    SolverRunResult result = solve(infeasibleZeroColumnInput());

    assertEquals(SolverStatus.INFEASIBLE, result.status());
    assertTrue(result.objectiveValue().isEmpty());
  }

  @Test
  void reportsUnsupportedForNonFiniteObjectiveData() {
    SolverRunResult result = solve(nonFiniteObjectiveInput());

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("objective coefficients"));
  }

  @Test
  void reportsUnsupportedForNanBounds() {
    SolverRunResult result = solve(nanBoundInput());

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("row bounds"));
  }

  @Test
  void rejectsNullInputs() {
    PerformanceLpSolverAdapter adapter = new PerformanceLpSolverAdapter();
    SolverInput input = emptyInput();

    assertThrows(
        NullPointerException.class, () -> adapter.solve(null, SolverOptions.defaults(), work()));
    assertThrows(NullPointerException.class, () -> adapter.solve(input, null, work()));
    assertThrows(
        NullPointerException.class, () -> adapter.solve(input, SolverOptions.defaults(), null));
  }

  private SolverWorkDirectory work() {
    return new SolverWorkDirectory(tempDir);
  }

  private SolverRunResult solve(final SolverInput input) {
    return new PerformanceLpSolverAdapter().solve(input, SolverOptions.defaults(), work());
  }

  private static SolverInput emptyInput() {
    LpProblem problem =
        new LpProblem(
            "empty",
            new LpObjective(ObjectiveSense.MINIMIZE, 3.0d, new double[0]),
            List.of(),
            List.of(),
            new LpProblemStats(0, 0, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 0, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput oneVariableInput() {
    LpProblem problem =
        new LpProblem(
            "one-variable",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, 1.0d)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput infeasibleZeroColumnInput() {
    LpProblem problem =
        new LpProblem(
            "empty-infeasible",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[0]),
            List.of(),
            List.of(new LpRowBounds(1.0d, Double.POSITIVE_INFINITY)),
            new LpProblemStats(1, 0, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(1, 0, new double[0], new int[0], new int[] {0, 0}));
  }

  private static SolverInput twoVariableMaximizationInput() {
    LpProblem problem =
        new LpProblem(
            "two-variable-max",
            new LpObjective(ObjectiveSense.MAXIMIZE, 0.0d, new double[] {3.0d, 2.0d}),
            List.of(
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY),
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(
                new LpRowBounds(Double.NEGATIVE_INFINITY, 4.0d),
                new LpRowBounds(Double.NEGATIVE_INFINITY, 2.0d)),
            new LpProblemStats(2, 2, 3));
    return SolverInput.withGeneratedNames(
        problem,
        new CsrMatrix(
            2, 2, new double[] {1.0d, 1.0d, 1.0d}, new int[] {0, 1, 0}, new int[] {0, 2, 3}));
  }

  private static SolverInput infeasibleInput() {
    LpProblem problem =
        new LpProblem(
            "infeasible",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(
                new LpRowBounds(2.0d, Double.POSITIVE_INFINITY),
                new LpRowBounds(Double.NEGATIVE_INFINITY, 1.0d)),
            new LpProblemStats(2, 1, 2));
    return SolverInput.withGeneratedNames(
        problem,
        new CsrMatrix(2, 1, new double[] {1.0d, 1.0d}, new int[] {0, 0}, new int[] {0, 1, 2}));
  }

  private static SolverInput unboundedInput() {
    LpProblem problem =
        new LpProblem(
            "unbounded",
            new LpObjective(ObjectiveSense.MAXIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput freeVariableInput() {
    LpProblem problem =
        new LpProblem(
            "free-variable",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(LpVariableBounds.FREE),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput nonFiniteObjectiveInput() {
    LpProblem problem =
        new LpProblem(
            "non-finite-objective",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {Double.NaN}),
            List.of(new LpVariableBounds(0.0d, 1.0d)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput nanBoundInput() {
    LpProblem problem =
        new LpProblem(
            "nan-row-bound",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, 1.0d)),
            List.of(new LpRowBounds(Double.NaN, 1.0d)),
            new LpProblemStats(1, 1, 1));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 1}));
  }
}
