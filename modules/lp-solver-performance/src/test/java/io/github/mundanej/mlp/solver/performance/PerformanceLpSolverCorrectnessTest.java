package io.github.mundanej.mlp.solver.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.generators.GeneratedLpInstance;
import io.github.mundanej.mlp.generators.NumericalStressGenerator;
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
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.LpSolutionValidator;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationEvidence;
import io.github.mundanej.mlp.validation.ValidationReport;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class PerformanceLpSolverCorrectnessTest {
  private static final List<String> SUPPORTED_TIER_ONE_FIXTURES =
      List.of(
          "single-bounded-variable",
          "two-variable-feasible-optimum",
          "unbounded-nonnegative-ray",
          "redundant-row",
          "equality-row",
          "degenerate-optimum");

  private static final List<String> UNSUPPORTED_TIER_ONE_FIXTURES =
      List.of("infeasible-rows", "fixed-variable", "free-variable-row-bounded", "ranged-row");

  @TempDir private Path tempDir;
  private final PerformanceLpSolverAdapter adapter = new PerformanceLpSolverAdapter();
  private final LpSolutionValidator validator = new LpSolutionValidator();

  @Test
  void validatesSupportedTierOneFixtures() {
    for (String fixtureName : SUPPORTED_TIER_ONE_FIXTURES) {
      CanonicalLpFixture fixture = LpTestInstances.tierOneFixture(fixtureName);
      SolverRunResult result = adapter.solve(input(fixture), SolverOptions.defaults(), work());

      assertEquals(expectedStatus(fixture), result.status(), fixtureName);
      ValidationReport report =
          validator.validate(
              fixture.problem(),
              fixture.matrix(),
              LpTestInstances.expectedValidationResult(fixture),
              validationEvidence(result),
              ToleranceProfile.STANDARD);
      assertTrue(report.accepted(), () -> fixtureName + " " + report.findings());
    }
  }

  @Test
  void recordsUnsupportedTierOneOutcomesExplicitly() {
    for (String fixtureName : UNSUPPORTED_TIER_ONE_FIXTURES) {
      SolverRunResult result =
          adapter.solve(
              input(LpTestInstances.tierOneFixture(fixtureName)), SolverOptions.defaults(), work());

      assertEquals(SolverStatus.UNSUPPORTED, result.status(), fixtureName);
      assertTrue(result.objectiveValue().isEmpty(), fixtureName);
      assertEquals(0, result.primalValues().length, fixtureName);
    }
  }

  @Test
  void validatesSupportedNumericalStressFixtures() {
    for (GeneratedLpInstance instance : new NumericalStressGenerator().suite()) {
      CanonicalLpFixture fixture = instance.fixture();
      if (fixture.problem().name().equals("stress-ill-conditioned-ranged")) {
        continue;
      }

      SolverRunResult result = adapter.solve(input(fixture), SolverOptions.defaults(), work());

      assertEquals(SolverStatus.OPTIMAL, result.status(), fixture.problem().name());
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
  void recordsUnsupportedNumericalStressOutcomesExplicitly() {
    CanonicalLpFixture fixture =
        new NumericalStressGenerator().illConditionedUnsupported().fixture();

    SolverRunResult result = adapter.solve(input(fixture), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.UNSUPPORTED, result.status());
    assertTrue(result.message().contains("ranged rows"));
  }

  @Test
  void validatesSupportedInfeasibleShape() {
    SolverInput input = supportedInfeasibleInput();

    SolverRunResult result = adapter.solve(input, SolverOptions.defaults(), work());

    assertEquals(SolverStatus.INFEASIBLE, result.status());
    ValidationReport report =
        validator.validate(
            input.problem(),
            input.matrix(),
            ExpectedValidationResult.statusOnly(ValidationStatus.INFEASIBLE),
            validationEvidence(result),
            ToleranceProfile.STANDARD);
    assertTrue(report.accepted(), () -> report.findings().toString());
  }

  @Test
  void validatesEqualityFixture() {
    assertValidatedTierOneFixture("equality-row");
  }

  @Test
  void validatesDegenerateOptimumFixture() {
    assertValidatedTierOneFixture("degenerate-optimum");
  }

  @Test
  void acceptsObjectiveAtToleranceBoundary() {
    CanonicalLpFixture fixture = LpTestInstances.tierOneFixture("single-bounded-variable");
    SolverRunResult result = adapter.solve(input(fixture), SolverOptions.defaults(), work());
    double boundaryObjective =
        result.objectiveValue().orElseThrow() + ToleranceProfile.STANDARD.feasibilityTolerance();

    ValidationReport report =
        validator.validate(
            fixture.problem(),
            fixture.matrix(),
            new ExpectedValidationResult(
                Optional.of(ValidationStatus.OPTIMAL), OptionalDouble.of(boundaryObjective)),
            validationEvidence(result),
            ToleranceProfile.STANDARD);

    assertTrue(report.accepted(), () -> report.findings().toString());
  }

  private void assertValidatedTierOneFixture(final String fixtureName) {
    CanonicalLpFixture fixture = LpTestInstances.tierOneFixture(fixtureName);

    SolverRunResult result = adapter.solve(input(fixture), SolverOptions.defaults(), work());

    assertEquals(SolverStatus.OPTIMAL, result.status());
    ValidationReport report =
        validator.validate(
            fixture.problem(),
            fixture.matrix(),
            LpTestInstances.expectedValidationResult(fixture),
            validationEvidence(result),
            ToleranceProfile.STANDARD);
    assertTrue(report.accepted(), () -> report.findings().toString());
  }

  private SolverWorkDirectory work() {
    return new SolverWorkDirectory(tempDir);
  }

  private static SolverInput input(final CanonicalLpFixture fixture) {
    return new SolverInput(
        fixture.problem(), fixture.matrix(), fixture.rowNames(), fixture.columnNames(), "OBJ");
  }

  private static SolverInput supportedInfeasibleInput() {
    LpProblem problem =
        new LpProblem(
            "supported-infeasible",
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
