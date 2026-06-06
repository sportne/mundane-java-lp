package io.github.mundanej.mlp.examples.solvercomparison;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.testkit.LpTestInstances;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class SolverComparisonSmokeMainTest {
  @TempDir private Path tempDir;

  @Test
  void allUnavailableSolversStillProduceReports() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                unavailable("highs"),
                unavailable("clp"),
                unavailable("glpk"),
                unavailable("ortools"),
                unavailable("ojalgo"),
                unavailable("simple"),
                unavailable("performance")));

    assertEquals(7, result.records().size());
    assertEquals(0, result.successfulSolvers());
    assertEquals(0, result.failedSolvers());
    assertEquals(7, result.unavailableSolvers());
    assertEquals(0, result.unsupportedRecords());
    assertTrue(Files.readString(result.markdownPath()).contains("SOLVER_UNAVAILABLE"));
    assertTrue(Files.readString(result.jsonPath()).contains("\"outcome\":\"SOLVER_UNAVAILABLE\""));
    assertTrue(Files.readString(result.csvPath()).contains("SOLVER_UNAVAILABLE"));
    assertThrows(IllegalStateException.class, result::throwIfFailed);
  }

  @Test
  void atLeastOneAvailableSolverIsReported() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                optimal("highs"),
                unavailable("clp"),
                unavailable("glpk"),
                unavailable("ortools"),
                unavailable("ojalgo"),
                optimal("simple"),
                optimal("performance")));

    assertEquals(7, result.records().size());
    assertEquals(3, result.successfulSolvers());
    assertEquals(0, result.failedSolvers());
    assertEquals(4, result.unavailableSolvers());
    assertEquals(0, result.unsupportedRecords());
    assertEquals(SolverComparisonSmokeMain.ComparisonMode.SMOKE, result.mode());
    assertTrue(Files.readString(result.markdownPath()).contains("SUCCESS"));
    assertTrue(Files.readString(result.jsonPath()).contains("\"solver\":\"highs\""));
    assertTrue(Files.readString(result.jsonPath()).contains("\"mode\":\"smoke\""));
    assertTrue(Files.readString(result.csvPath()).contains("highs"));
    SolverComparisonSmokeMain.printSummary(result);
    result.throwIfFailed();
  }

  @Test
  void strictModePassesWhenAllRequiredSolversAreAvailable() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                optimal("highs"),
                optimal("clp"),
                optimal("glpk"),
                optimal("ortools"),
                optimal("ojalgo"),
                optimal("simple"),
                optimal("performance")),
            SolverComparisonSmokeMain.ComparisonMode.STRICT);

    assertEquals(112, result.records().size());
    assertEquals(7, result.successfulSolvers());
    assertEquals(0, result.failedSolvers());
    assertEquals(0, result.unavailableSolvers());
    assertEquals(0, result.unsupportedRecords());
    assertTrue(Files.readString(result.markdownPath()).contains("|strict|"));
    assertTrue(
        Files.readString(result.markdownPath()).contains("mps-roundtrip-single-bounded-variable"));
    assertTrue(Files.readString(result.jsonPath()).contains("\"mode\":\"strict\""));
    result.throwIfFailed();
  }

  @Test
  void strictModeFailsWhenRequiredSolverIsUnavailable() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                optimal("highs"),
                unavailable("clp"),
                optimal("glpk"),
                optimal("ortools"),
                optimal("ojalgo"),
                optimal("simple"),
                optimal("performance")),
            SolverComparisonSmokeMain.ComparisonMode.STRICT);

    assertEquals(6, result.successfulSolvers());
    assertEquals(1, result.unavailableSolvers());
    assertTrue(Files.exists(result.markdownPath()));
    assertThrows(IllegalStateException.class, result::throwIfFailed);
  }

  @Test
  void strictModeDoesNotFailWhenAvailableSolverReportsUnsupportedSubset() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                optimal("highs"),
                unsupportedFixture("clp", "single-bounded-variable"),
                optimal("glpk"),
                optimal("ortools"),
                optimal("ojalgo"),
                optimal("simple"),
                optimal("performance")),
            SolverComparisonSmokeMain.ComparisonMode.STRICT);

    assertEquals(112, result.records().size());
    assertEquals(7, result.successfulSolvers());
    assertEquals(0, result.unavailableSolvers());
    assertEquals(2, result.unsupportedRecords());
    result.throwIfFailed();
  }

  @Test
  void strictModeFailsWhenRequiredSolverHasNoAcceptedEvidence() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                optimal("highs"),
                unsupported("clp"),
                optimal("glpk"),
                optimal("ortools"),
                optimal("ojalgo"),
                optimal("simple"),
                optimal("performance")),
            SolverComparisonSmokeMain.ComparisonMode.STRICT);

    assertEquals(112, result.records().size());
    assertEquals(6, result.successfulSolvers());
    assertEquals(0, result.unavailableSolvers());
    assertEquals(16, result.unsupportedRecords());
    assertThrows(IllegalStateException.class, result::throwIfFailed);
  }

  @Test
  void adapterErrorsAreReportedAsFailures() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                error("highs"),
                unavailable("clp"),
                unavailable("glpk"),
                unavailable("ortools"),
                unavailable("ojalgo"),
                optimal("simple"),
                optimal("performance")));

    assertEquals(2, result.successfulSolvers());
    assertEquals(1, result.failedSolvers());
    assertEquals(4, result.unavailableSolvers());
    assertThrows(IllegalStateException.class, result::throwIfFailed);
  }

  @Test
  void validationFailuresAreReportedAsFailures() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                wrongObjective("highs"),
                unavailable("clp"),
                unavailable("glpk"),
                unavailable("ortools"),
                unavailable("ojalgo"),
                optimal("simple"),
                optimal("performance")));

    assertEquals(2, result.successfulSolvers());
    assertEquals(1, result.failedSolvers());
    assertEquals(4, result.unavailableSolvers());
    assertThrows(IllegalStateException.class, result::throwIfFailed);
  }

  @Test
  void requiredInProjectSolverUnavailableFailsAfterWritingReports() throws IOException {
    SolverComparisonSmokeMain.SmokeResult result =
        SolverComparisonSmokeMain.run(
            tempDir,
            List.of(
                unavailable("highs"),
                unavailable("clp"),
                unavailable("glpk"),
                unavailable("ortools"),
                unavailable("ojalgo"),
                unavailable("simple"),
                optimal("performance")));

    assertEquals(1, result.successfulSolvers());
    assertEquals(0, result.failedSolvers());
    assertEquals(6, result.unavailableSolvers());
    assertTrue(Files.exists(result.markdownPath()));
    assertThrows(IllegalStateException.class, result::throwIfFailed);
  }

  @Test
  void defaultAdaptersListCliThenJavaSolvers() {
    List<SolverId> ids =
        SolverComparisonSmokeMain.defaultAdapters().stream()
            .map(Supplier::get)
            .map(LpSolverAdapter::id)
            .toList();

    assertEquals(
        List.of(
            new SolverId("highs", "cli"),
            new SolverId("clp", "cli"),
            new SolverId("glpk", "cli"),
            new SolverId("ortools", "java"),
            new SolverId("ojalgo", "java"),
            new SolverId("simple", "java"),
            new SolverId("performance", "java")),
        ids);
  }

  @Test
  void parsesDefaultSmokeArguments() {
    SolverComparisonSmokeMain.CliArguments arguments =
        SolverComparisonSmokeMain.CliArguments.parse(new String[0]);

    assertEquals(SolverComparisonSmokeMain.ComparisonMode.SMOKE, arguments.mode());
    assertEquals(
        Path.of(SolverComparisonSmokeMain.DEFAULT_OUTPUT_DIRECTORY), arguments.outputDirectory());
  }

  @Test
  void parsesStrictArgumentsWithDefaultAndExplicitOutput() {
    SolverComparisonSmokeMain.CliArguments defaultStrict =
        SolverComparisonSmokeMain.CliArguments.parse(new String[] {"--strict"});
    SolverComparisonSmokeMain.CliArguments explicitStrict =
        SolverComparisonSmokeMain.CliArguments.parse(
            new String[] {"--strict", "--output-dir", "custom-strict"});

    assertEquals(SolverComparisonSmokeMain.ComparisonMode.STRICT, defaultStrict.mode());
    assertEquals(
        Path.of(SolverComparisonSmokeMain.STRICT_OUTPUT_DIRECTORY),
        defaultStrict.outputDirectory());
    assertEquals(SolverComparisonSmokeMain.ComparisonMode.STRICT, explicitStrict.mode());
    assertEquals(Path.of("custom-strict"), explicitStrict.outputDirectory());
  }

  @Test
  void parsesLegacyPositionalOutputAndRejectsBadArguments() {
    SolverComparisonSmokeMain.CliArguments arguments =
        SolverComparisonSmokeMain.CliArguments.parse(new String[] {"custom-smoke"});

    assertEquals(SolverComparisonSmokeMain.ComparisonMode.SMOKE, arguments.mode());
    assertEquals(Path.of("custom-smoke"), arguments.outputDirectory());
    assertThrows(
        IllegalArgumentException.class,
        () -> SolverComparisonSmokeMain.CliArguments.parse(new String[] {"--unknown"}));
    assertThrows(
        IllegalArgumentException.class,
        () -> SolverComparisonSmokeMain.CliArguments.parse(new String[] {"--output-dir"}));
  }

  @Test
  void commandMetadataReportsAvailableCommandVersion() {
    SolverComparisonSmokeMain.SolverMetadata metadata =
        SolverComparisonSmokeMain.commandMetadata("sh", List.of("-c", "printf 'solver 1.0\\n'"));

    assertEquals("solver 1.0", metadata.version());
    assertTrue(metadata.binaryPath().endsWith("/sh"));
  }

  @Test
  void commandMetadataReportsUnavailableCommand() {
    SolverComparisonSmokeMain.SolverMetadata metadata =
        SolverComparisonSmokeMain.commandMetadata(
            "missing-solver-command-for-test", List.of("--version"));

    assertEquals("unavailable", metadata.version());
    assertEquals("unavailable: command not found", metadata.binaryPath());
  }

  @Test
  void commandMetadataReportsTimedOutVersionCommand() {
    SolverComparisonSmokeMain.SolverMetadata metadata =
        SolverComparisonSmokeMain.commandMetadata("sh", List.of("-c", "sleep 10"));

    assertEquals("unavailable: version command timed out", metadata.version());
    assertTrue(metadata.binaryPath().endsWith("/sh"));
  }

  private static Supplier<LpSolverAdapter> unavailable(final String name) {
    return () ->
        new StubAdapter(
            name,
            SolverStatus.UNSUPPORTED,
            OptionalDouble.empty(),
            new double[0],
            "binary unavailable");
  }

  private static Supplier<LpSolverAdapter> optimal(final String name) {
    return () -> new EvidenceAdapter(name);
  }

  private static Supplier<LpSolverAdapter> unsupported(final String name) {
    return () ->
        new StubAdapter(
            name,
            SolverStatus.UNSUPPORTED,
            OptionalDouble.empty(),
            new double[0],
            "unsupported model shape");
  }

  private static Supplier<LpSolverAdapter> unsupportedFixture(
      final String name, final String fixtureName) {
    return () -> new FixtureUnsupportedAdapter(name, fixtureName);
  }

  private static Supplier<LpSolverAdapter> wrongObjective(final String name) {
    return () ->
        new StubAdapter(name, SolverStatus.OPTIMAL, OptionalDouble.of(1.0d), new double[0], "");
  }

  private static Supplier<LpSolverAdapter> error(final String name) {
    return () ->
        new StubAdapter(name, SolverStatus.ERROR, OptionalDouble.empty(), new double[0], "failed");
  }

  private record StubAdapter(
      String name, SolverStatus status, OptionalDouble objective, double[] primal, String message)
      implements LpSolverAdapter {
    @Override
    public SolverId id() {
      return new SolverId(name, "cli");
    }

    @Override
    public SolverRunResult solve(
        final SolverInput input,
        final SolverOptions options,
        final SolverWorkDirectory workDirectory) {
      return new SolverRunResult(id(), status, objective, primal, 0.0d, message);
    }
  }

  private record EvidenceAdapter(String name) implements LpSolverAdapter {
    @Override
    public SolverId id() {
      return new SolverId(name, "cli");
    }

    @Override
    public SolverRunResult solve(
        final SolverInput input,
        final SolverOptions options,
        final SolverWorkDirectory workDirectory) {
      CanonicalLpFixture fixture = LpTestInstances.tierOneFixture(input.problem().name());
      ValidationStatus status =
          LpTestInstances.expectedValidationResult(fixture).status().orElseThrow();
      SolverStatus solverStatus = SolverStatus.valueOf(status.name());
      return new SolverRunResult(
          id(),
          solverStatus,
          fixture.evidence().objectiveValue(),
          fixture.evidence().primal(),
          0.0d,
          "");
    }
  }

  private record FixtureUnsupportedAdapter(String name, String fixtureName)
      implements LpSolverAdapter {
    @Override
    public SolverId id() {
      return new SolverId(name, "cli");
    }

    @Override
    public SolverRunResult solve(
        final SolverInput input,
        final SolverOptions options,
        final SolverWorkDirectory workDirectory) {
      if (fixtureName.equals(input.problem().name())) {
        return new SolverRunResult(
            id(),
            SolverStatus.UNSUPPORTED,
            OptionalDouble.empty(),
            new double[0],
            0.0d,
            "unsupported model shape");
      }
      CanonicalLpFixture fixture = LpTestInstances.tierOneFixture(input.problem().name());
      ValidationStatus status =
          LpTestInstances.expectedValidationResult(fixture).status().orElseThrow();
      return new SolverRunResult(
          id(),
          SolverStatus.valueOf(status.name()),
          fixture.evidence().objectiveValue(),
          fixture.evidence().primal(),
          0.0d,
          "");
    }
  }
}
