package io.github.mundanej.mlp.harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationFinding;
import io.github.mundanej.mlp.validation.ValidationReport;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

final class HarnessContractsTest {
  @Test
  void createsBenchmarkInstanceWithExplicitMatrix() {
    BenchmarkInstance instance =
        new BenchmarkInstance(
            "instance", oneRowProblem(), oneEntryMatrix(), ExpectedValidationResult.optimal(1.0d));

    assertEquals("instance", instance.id());
    assertEquals(1, instance.matrix().nonzeros());
    assertTrue(instance.expectedResult().objectiveValue().isPresent());
  }

  @Test
  void createsBenchmarkInstanceWithEmptyMatrix() {
    BenchmarkInstance instance = new BenchmarkInstance("empty", emptyProblem());

    assertEquals(0, instance.matrix().rows());
    assertEquals(1, instance.matrix().columns());
    assertEquals(0, instance.matrix().nonzeros());
    assertTrue(instance.expectedResult().status().isEmpty());
  }

  @Test
  void rejectsInvalidBenchmarkInstances() {
    ExpectedValidationResult expected = ExpectedValidationResult.optimal(0.0d);
    assertThrows(
        IllegalArgumentException.class,
        () -> new BenchmarkInstance(" ", emptyProblem(), emptyMatrix(), expected));
    assertThrows(
        IllegalArgumentException.class,
        () -> new BenchmarkInstance("id", null, emptyMatrix(), expected));
    assertThrows(
        IllegalArgumentException.class,
        () -> new BenchmarkInstance("id", emptyProblem(), null, expected));
    assertThrows(
        IllegalArgumentException.class,
        () -> new BenchmarkInstance("id", emptyProblem(), emptyMatrix(), null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new BenchmarkInstance("id", oneRowProblem(), emptyMatrix(), expected));
    assertThrows(IllegalArgumentException.class, () -> new BenchmarkInstance("id", null));
  }

  @Test
  void benchmarkSuiteCopiesInstances() {
    BenchmarkInstance instance = new BenchmarkInstance("empty", emptyProblem());
    BenchmarkSuite suite = new BenchmarkSuite("suite", List.of(instance));

    assertEquals("suite", suite.id());
    assertEquals(List.of(instance), suite.instances());
    assertThrows(UnsupportedOperationException.class, () -> suite.instances().add(instance));
    assertThrows(IllegalArgumentException.class, () -> new BenchmarkSuite(null, List.of(instance)));
    assertThrows(IllegalArgumentException.class, () -> new BenchmarkSuite(" ", List.of(instance)));
    assertThrows(NullPointerException.class, () -> new BenchmarkSuite("suite", null));
  }

  @Test
  void runConfigRejectsNulls() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new HarnessRunConfig(null, SolverOptions.defaults(), ToleranceProfile.STANDARD));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HarnessRunConfig(Path.of("work"), null, ToleranceProfile.STANDARD));
    assertThrows(
        IllegalArgumentException.class,
        () -> new HarnessRunConfig(Path.of("work"), SolverOptions.defaults(), null));
  }

  @Test
  void runRecordNormalizesOptionalMetadata() {
    RunRecord record =
        new RunRecord(
            "suite",
            "instance",
            result(SolverStatus.OPTIMAL, 0.25d),
            acceptedReport(),
            RunOutcome.SUCCESS,
            null,
            " ",
            SolverOptions.defaults(),
            MachineFingerprint.capture(),
            0.1d,
            0.2d,
            0.3d,
            0.4d,
            " ");

    assertEquals("", record.failureMessage());
    assertEquals("default", record.runMode());
    assertEquals("not-measured", record.solverVersion());
    assertEquals("not-measured", record.solverBinaryPath());
    assertEquals("not-measured", record.peakMemoryBytes());
    assertEquals(0.4d, record.totalSeconds());
  }

  @Test
  void runRecordCopiesReportMetadata() {
    RunRecord record =
        new RunRecord(
                "suite",
                "instance",
                result(SolverStatus.OPTIMAL, 0.25d),
                acceptedReport(),
                RunOutcome.SUCCESS,
                "",
                "version",
                SolverOptions.defaults(),
                MachineFingerprint.capture(),
                0.1d,
                0.2d,
                0.3d,
                0.4d,
                "not-measured")
            .withReportMetadata("strict", "solver 1.0", "/usr/local/bin/solver");

    assertEquals("strict", record.runMode());
    assertEquals("solver 1.0", record.solverVersion());
    assertEquals("/usr/local/bin/solver", record.solverBinaryPath());
  }

  @Test
  void runRecordReportsUnmeasuredTimingBuckets() {
    RunRecord record =
        new RunRecord(
            "suite",
            "instance",
            result(SolverStatus.OPTIMAL, 0.25d),
            acceptedReport(),
            RunOutcome.SUCCESS,
            "",
            "version",
            SolverOptions.defaults(),
            MachineFingerprint.capture(),
            Double.NaN,
            Double.NaN,
            0.3d,
            0.4d,
            "not-measured");

    assertTrue(Double.isNaN(record.parseSeconds()));
    assertEquals("not-measured", record.parseSecondsReportValue());
    assertEquals("not-measured", record.exportSecondsReportValue());
    assertEquals("0.3", record.validationSecondsReportValue());
    assertEquals("0.4", record.totalSecondsReportValue());
  }

  @Test
  void runRecordRejectsInvalidRequiredFields() {
    SolverRunResult result = result(SolverStatus.OPTIMAL, 0.0d);
    ValidationReport report = acceptedReport();
    assertThrows(
        IllegalArgumentException.class,
        () -> fullRecord(null, "instance", result, report, RunOutcome.SUCCESS));
    assertThrows(
        IllegalArgumentException.class,
        () -> fullRecord("suite", " ", result, report, RunOutcome.SUCCESS));
    assertThrows(
        IllegalArgumentException.class,
        () -> fullRecord("suite", "instance", null, report, RunOutcome.SUCCESS));
    assertThrows(
        IllegalArgumentException.class,
        () -> fullRecord("suite", "instance", result, null, RunOutcome.SUCCESS));
    assertThrows(
        IllegalArgumentException.class,
        () -> fullRecord("suite", "instance", result, report, null));
  }

  @Test
  void runRecordRejectsInvalidMetadataAndTimings() {
    SolverRunResult result = result(SolverStatus.OPTIMAL, 0.0d);
    ValidationReport report = acceptedReport();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RunRecord(
                "suite",
                "instance",
                result,
                report,
                RunOutcome.SUCCESS,
                "",
                "version",
                null,
                MachineFingerprint.capture(),
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                "not-measured"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RunRecord(
                "suite",
                "instance",
                result,
                report,
                RunOutcome.SUCCESS,
                "",
                "version",
                SolverOptions.defaults(),
                null,
                0.0d,
                0.0d,
                0.0d,
                0.0d,
                "not-measured"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RunRecord(
                "suite",
                "instance",
                result,
                report,
                RunOutcome.SUCCESS,
                "",
                "version",
                SolverOptions.defaults(),
                MachineFingerprint.capture(),
                -0.1d,
                0.0d,
                0.0d,
                0.0d,
                "not-measured"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RunRecord(
                "suite",
                "instance",
                result,
                report,
                RunOutcome.SUCCESS,
                "",
                "version",
                SolverOptions.defaults(),
                MachineFingerprint.capture(),
                Double.POSITIVE_INFINITY,
                0.0d,
                0.0d,
                0.0d,
                "not-measured"));
  }

  @Test
  void convenienceRunRecordConstructorsSetOutcome() {
    RunRecord success =
        new RunRecord("instance", result(SolverStatus.OPTIMAL, 0.0d), acceptedReport());
    RunRecord failure =
        new RunRecord("instance", result(SolverStatus.OPTIMAL, 1.0d), rejectedReport());
    RunRecord explicit =
        new RunRecord(
            "suite",
            "instance",
            result(SolverStatus.ERROR, Double.NaN),
            rejectedReport(),
            RunOutcome.ADAPTER_ERROR,
            "failed");

    assertEquals("default", success.suiteId());
    assertEquals(RunOutcome.SUCCESS, success.outcome());
    assertEquals(RunOutcome.VALIDATION_FAILED, failure.outcome());
    assertEquals(RunOutcome.ADAPTER_ERROR, explicit.outcome());
    assertFalse(explicit.failureMessage().isBlank());
    assertSame(RunOutcome.SUCCESS, RunOutcome.valueOf("SUCCESS"));
  }

  private static RunRecord fullRecord(
      final String suiteId,
      final String instanceId,
      final SolverRunResult solverResult,
      final ValidationReport validationReport,
      final RunOutcome outcome) {
    return new RunRecord(
        suiteId,
        instanceId,
        solverResult,
        validationReport,
        outcome,
        "",
        "version",
        SolverOptions.defaults(),
        MachineFingerprint.capture(),
        0.0d,
        0.0d,
        0.0d,
        0.0d,
        "not-measured");
  }

  private static SolverRunResult result(final SolverStatus status, final double objective) {
    OptionalDouble objectiveValue =
        Double.isNaN(objective) ? OptionalDouble.empty() : OptionalDouble.of(objective);
    return new SolverRunResult(
        new SolverId("solver", "test"), status, objectiveValue, new double[0], 0.0d, "");
  }

  private static ValidationReport acceptedReport() {
    return new ValidationReport(ToleranceProfile.STANDARD, List.of());
  }

  private static ValidationReport rejectedReport() {
    return new ValidationReport(
        ToleranceProfile.STANDARD, List.of(new ValidationFinding("CODE", "message", 1.0d)));
  }

  private static LpProblem emptyProblem() {
    return new LpProblem(
        "empty",
        new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
        List.of(new LpVariableBounds(0.0d, 1.0d)),
        List.of(),
        new LpProblemStats(0, 1, 0));
  }

  private static CsrMatrix emptyMatrix() {
    return new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0});
  }

  private static LpProblem oneRowProblem() {
    return new LpProblem(
        "one-row",
        new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d}),
        List.of(new LpVariableBounds(0.0d, 10.0d)),
        List.of(new LpRowBounds(1.0d, 1.0d)),
        new LpProblemStats(1, 1, 1));
  }

  private static CsrMatrix oneEntryMatrix() {
    return new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 1});
  }
}
