package io.github.mundanej.mlp.solver.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
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

final class SimpleLpSolverAdapterTest {
  @TempDir private Path tempDir;

  @Test
  void exposesStableSolverId() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    assertEquals("simple", adapter.id().name());
    assertEquals("java", adapter.id().mode());
  }

  @Test
  void solvesEmptyLpSmokePath() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(emptyInput(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(7.0d, result.objectiveValue().orElseThrow());
    assertEquals(0, result.primalValues().length);
    assertTrue(result.message().contains("trivial empty LP"));
  }

  @Test
  void reportsUnsupportedOutsideSmokePath() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();

    SolverRunResult result =
        adapter.solve(
            singleVariableInput(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.objectiveValue().isEmpty());
    assertEquals(0, result.primalValues().length);
    assertTrue(result.message().contains("G9-002"));
  }

  @Test
  void rejectsNullInputs() {
    SimpleLpSolverAdapter adapter = new SimpleLpSolverAdapter();
    SolverInput input = emptyInput();
    SolverWorkDirectory workDirectory = new SolverWorkDirectory(tempDir);

    assertThrows(
        NullPointerException.class,
        () -> adapter.solve(null, SolverOptions.defaults(), workDirectory));
    assertThrows(NullPointerException.class, () -> adapter.solve(input, null, workDirectory));
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

  private static SolverInput singleVariableInput() {
    LpProblem problem =
        new LpProblem(
            "single-variable",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
            List.of(new LpVariableBounds(0.0d, 1.0d)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }
}
