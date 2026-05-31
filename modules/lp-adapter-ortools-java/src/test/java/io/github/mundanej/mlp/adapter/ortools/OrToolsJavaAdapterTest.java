package io.github.mundanej.mlp.adapter.ortools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
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
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

final class OrToolsJavaAdapterTest {
  @TempDir private Path tempDir;

  @Test
  void hasExpectedId() {
    assertEquals("ortools", new OrToolsJavaAdapter().id().name());
    assertEquals("java", new OrToolsJavaAdapter().id().mode());
    assertThrows(NullPointerException.class, () -> new OrToolsJavaAdapter(null));
    assertThrows(NullPointerException.class, () -> new OrToolsJavaAdapter(() -> {}, null));
  }

  @Test
  void normalizesResultStatuses() {
    assertEquals(SolverStatus.OPTIMAL, OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.OPTIMAL));
    assertEquals(
        SolverStatus.FEASIBLE, OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.FEASIBLE));
    assertEquals(
        SolverStatus.INFEASIBLE, OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.INFEASIBLE));
    assertEquals(
        SolverStatus.UNBOUNDED, OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.UNBOUNDED));
    assertEquals(
        SolverStatus.NUMERICAL_FAILURE,
        OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.ABNORMAL));
    assertEquals(
        SolverStatus.ERROR, OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.MODEL_INVALID));
    assertEquals(
        SolverStatus.UNKNOWN, OrToolsJavaAdapter.normalize(MPSolver.ResultStatus.NOT_SOLVED));
  }

  @Test
  void reportsNativeRuntimeFailureAsUnsupported() {
    SolverRunResult result =
        new OrToolsJavaAdapter(
                () -> {
                  throw new UnsatisfiedLinkError("native unavailable");
                })
            .solve(input(0.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("native runtime unavailable"));
  }

  @Test
  void usesThrowableClassNameWhenNativeRuntimeFailureHasNoMessage() {
    SolverRunResult result =
        new OrToolsJavaAdapter(
                () -> {
                  throw new IllegalStateException();
                })
            .solve(input(0.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("IllegalStateException"));
  }

  @Test
  void usesThrowableClassNameWhenNativeLinkageFailureHasNoMessage() {
    SolverRunResult result =
        new OrToolsJavaAdapter(
                () -> {
                  throw new UnsatisfiedLinkError();
                })
            .solve(input(0.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("UnsatisfiedLinkError"));
  }

  @Test
  void reportsMissingGlopSolverAsUnsupported() {
    SolverRunResult result =
        new OrToolsJavaAdapter(() -> {}, () -> null)
            .solve(input(0.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("GLOP solver is unavailable"));
  }

  @Test
  void reportsSolverFactoryLinkageFailureAsUnsupported() {
    SolverRunResult result =
        new OrToolsJavaAdapter(
                () -> {},
                () -> {
                  throw new UnsatisfiedLinkError("late linkage failure");
                })
            .solve(input(0.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("late linkage failure"));
  }

  @Test
  void reportsSolverFactoryRuntimeFailureAsError() {
    SolverRunResult result =
        new OrToolsJavaAdapter(
                () -> {},
                () -> {
                  throw new IllegalStateException("factory failure");
                })
            .solve(input(0.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.ERROR, result.status());
    assertTrue(result.message().contains("factory failure"));
  }

  @Test
  void rejectsNullSolveInputs() {
    OrToolsJavaAdapter adapter = new OrToolsJavaAdapter(() -> {});

    assertThrows(
        NullPointerException.class,
        () -> adapter.solve(null, SolverOptions.defaults(), new SolverWorkDirectory(tempDir)));
    assertThrows(
        NullPointerException.class,
        () -> adapter.solve(input(1.0d), null, new SolverWorkDirectory(tempDir)));
  }

  @Test
  @EnabledIf("ortoolsRuntimeAvailable")
  void guardedIntegrationSmokeSolvesTinyLp() {
    SolverRunResult result =
        new OrToolsJavaAdapter()
            .solve(input(1.0d), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(0.0d, result.objectiveValue().orElseThrow(), 1.0e-7);
    assertArrayEquals(new double[] {0.0d}, result.primalValues(), 1.0e-7);
  }

  @Test
  @EnabledIf("ortoolsRuntimeAvailable")
  void guardedIntegrationSolvesMatrixBackedLp() {
    SolverRunResult result =
        new OrToolsJavaAdapter()
            .solve(matrixBackedInput(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.OPTIMAL, result.status());
    assertEquals(11.0d, result.objectiveValue().orElseThrow(), 1.0e-7);
    assertArrayEquals(new double[] {4.0d, 0.0d}, result.primalValues(), 1.0e-7);
  }

  @Test
  @EnabledIf("ortoolsRuntimeAvailable")
  void reportsUnsupportedNonFiniteModelData() {
    SolverRunResult result =
        new OrToolsJavaAdapter()
            .solve(input(Double.NaN), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("objective coefficient must be finite"));
  }

  @Test
  @EnabledIf("ortoolsRuntimeAvailable")
  void guardedIntegrationDetectsInfeasibleLp() {
    SolverRunResult result =
        new OrToolsJavaAdapter()
            .solve(infeasibleInput(), SolverOptions.defaults(), new SolverWorkDirectory(tempDir));

    assertEquals(SolverStatus.INFEASIBLE, result.status());
    assertTrue(result.objectiveValue().isEmpty());
    assertArrayEquals(new double[0], result.primalValues());
  }

  private static boolean ortoolsRuntimeAvailable() {
    try {
      Loader.loadNativeLibraries();
      return MPSolver.createSolver("GLOP") != null;
    } catch (LinkageError | RuntimeException exception) {
      return false;
    }
  }

  private static SolverInput input(final double objectiveCoefficient) {
    LpProblem problem =
        new LpProblem(
            "tiny",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {objectiveCoefficient}),
            List.of(new LpVariableBounds(0.0d, 1.0d)),
            List.of(),
            new LpProblemStats(0, 1, 0));
    return SolverInput.withGeneratedNames(
        problem, new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}));
  }

  private static SolverInput matrixBackedInput() {
    LpProblem problem =
        new LpProblem(
            "matrix-backed",
            new LpObjective(ObjectiveSense.MAXIMIZE, 3.0d, new double[] {2.0d, 1.0d}),
            List.of(
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY),
                new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
            List.of(
                new LpRowBounds(Double.NEGATIVE_INFINITY, 4.0d),
                new LpRowBounds(Double.NEGATIVE_INFINITY, 2.0d)),
            new LpProblemStats(2, 2, 3));
    return new SolverInput(
        problem,
        new CsrMatrix(
            2, 2, new double[] {1.0d, 1.0d, 1.0d}, new int[] {0, 1, 1}, new int[] {0, 2, 3}),
        List.of("capacity", "y-limit"),
        List.of("x", "y"),
        "OBJ");
  }

  private static SolverInput infeasibleInput() {
    LpProblem problem =
        new LpProblem(
            "infeasible",
            new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {0.0d}),
            List.of(LpVariableBounds.FREE),
            List.of(
                new LpRowBounds(1.0d, Double.POSITIVE_INFINITY),
                new LpRowBounds(Double.NEGATIVE_INFINITY, 0.0d)),
            new LpProblemStats(2, 1, 2));
    return new SolverInput(
        problem,
        new CsrMatrix(2, 1, new double[] {1.0d, 1.0d}, new int[] {0, 0}, new int[] {0, 1, 2}),
        List.of("lower", "upper"),
        List.of("x"),
        "OBJ");
  }
}
