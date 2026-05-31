package io.github.mundanej.mlp.harness.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.harness.MachineFingerprint;
import io.github.mundanej.mlp.harness.RunOutcome;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationFinding;
import io.github.mundanej.mlp.validation.ValidationReport;
import java.time.Duration;
import java.util.List;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

final class MarkdownReportWriterTest {
  @Test
  void rendersEmptyReport() {
    assertTrue(new MarkdownReportWriter().render(List.of()).contains("LP benchmark report"));
  }

  @Test
  void rendersAcceptedMarkdownReport() {
    String report =
        new MarkdownReportWriter()
            .render(
                List.of(
                    record(
                        SolverStatus.OPTIMAL,
                        OptionalDouble.of(1.0d),
                        RunOutcome.SUCCESS,
                        new ValidationReport(ToleranceProfile.STANDARD, List.of()),
                        "")));

    assertTrue(report.contains("|suite|instance|solver|1.0|OPTIMAL|1.0|SUCCESS|true|"));
    assertTrue(
        report.contains(
            "|STANDARD|none|2|30|0.0|0.0|0.25|0.5|0.75|not-measured|Linux|amd64|21|8|"));
  }

  @Test
  void rendersCsvReportWithEscapingAndFailureRows() {
    String report =
        new CsvReportWriter()
            .render(
                List.of(
                    record(
                        SolverStatus.ERROR,
                        OptionalDouble.empty(),
                        RunOutcome.ADAPTER_ERROR,
                        new ValidationReport(
                            ToleranceProfile.STRICT,
                            List.of(new ValidationFinding("X", "bad", 1.0d))),
                        "failed, badly")));

    assertTrue(report.startsWith("suite,instance,solver,version,status,objective,outcome"));
    assertTrue(report.contains("ERROR,,ADAPTER_ERROR,false,STRICT"));
    assertTrue(report.contains(",not-measured,"));
    assertTrue(report.contains("\"failed, badly\""));
  }

  @Test
  void rendersJsonReportStructurally() {
    String report =
        new JsonReportWriter()
            .render(
                List.of(
                    unmeasuredRecord(
                        SolverStatus.UNSUPPORTED,
                        OptionalDouble.empty(),
                        RunOutcome.SOLVER_UNAVAILABLE,
                        new ValidationReport(ToleranceProfile.LOOSE, List.of()),
                        "")));

    assertTrue(report.contains("\"suite\":\"suite\""));
    assertTrue(report.contains("\"status\":\"UNSUPPORTED\""));
    assertTrue(report.contains("\"objective\":null"));
    assertTrue(report.contains("\"outcome\":\"SOLVER_UNAVAILABLE\""));
    assertTrue(report.contains("\"parseSeconds\":\"not-measured\""));
    assertTrue(report.contains("\"peakMemoryBytes\":\"not-measured\""));
    assertTrue(report.contains("\"processors\":8"));
  }

  @Test
  void rendersEmptyCsvAndJsonDeterministically() {
    assertEquals(
        "suite,instance,solver,version,status,objective,outcome,accepted,tolerance,threads,"
            + "time_limit_seconds,parse_seconds,export_seconds,solve_seconds,validation_seconds,"
            + "total_seconds,peak_memory_bytes,residuals,os,arch,java,processors,termination\n",
        new CsvReportWriter().render(List.of()));
    assertEquals("[\n]\n", new JsonReportWriter().render(List.of()));
  }

  @Test
  void escapesMultilineDiagnostics() {
    RunRecord record =
        record(
            SolverStatus.ERROR,
            OptionalDouble.empty(),
            RunOutcome.ADAPTER_ERROR,
            new ValidationReport(ToleranceProfile.STANDARD, List.of()),
            "line1\nline2\tTabbed");

    String markdown = new MarkdownReportWriter().render(List.of(record));
    String json = new JsonReportWriter().render(List.of(record));

    assertTrue(markdown.contains("line1\\nline2"));
    assertTrue(json.contains("line1\\nline2\\tTabbed"));
  }

  @Test
  void rendersMeasuredTimingValuesAsJsonNumbers() {
    String report =
        new JsonReportWriter()
            .render(
                List.of(
                    record(
                        SolverStatus.OPTIMAL,
                        OptionalDouble.of(1.0d),
                        RunOutcome.SUCCESS,
                        new ValidationReport(ToleranceProfile.STANDARD, List.of()),
                        "")));

    assertTrue(report.contains("\"parseSeconds\":0.0"));
    assertTrue(report.contains("\"totalSeconds\":0.75"));
  }

  private static RunRecord record(
      final SolverStatus status,
      final OptionalDouble objective,
      final RunOutcome outcome,
      final ValidationReport validationReport,
      final String failureMessage) {
    return new RunRecord(
        "suite",
        "instance",
        new SolverRunResult(
            new SolverId("solver", "test"), status, objective, new double[0], 0.25d, "message"),
        validationReport,
        outcome,
        failureMessage,
        "1.0",
        new SolverOptions(Duration.ofSeconds(30), 2),
        new MachineFingerprint("Linux", "amd64", "21", 8),
        0.0d,
        0.0d,
        0.5d,
        0.75d,
        "not-measured");
  }

  private static RunRecord unmeasuredRecord(
      final SolverStatus status,
      final OptionalDouble objective,
      final RunOutcome outcome,
      final ValidationReport validationReport,
      final String failureMessage) {
    return new RunRecord(
        "suite",
        "instance",
        new SolverRunResult(
            new SolverId("solver", "test"), status, objective, new double[0], 0.25d, "message"),
        validationReport,
        outcome,
        failureMessage,
        "1.0",
        new SolverOptions(Duration.ofSeconds(30), 2),
        new MachineFingerprint("Linux", "amd64", "21", 8),
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        "not-measured");
  }
}
