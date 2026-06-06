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

    assertTrue(
        report.contains(
            "|default|suite|instance|solver|1.0|not-measured|OPTIMAL|1.0|SUCCESS|true|"));
    assertTrue(
        report.contains("|STANDARD|none|2|30|0.0|0.0|0.25|0.5|0.75|0|1|0.25|0.25|0.25|0|0|"));
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

    assertTrue(report.startsWith("mode,suite,instance,solver,version,solver_binary_path,status"));
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
  void rendersRequiredPerformanceEvidenceFieldsAcrossFormats() {
    RunRecord record =
        record(
            SolverStatus.OPTIMAL,
            OptionalDouble.of(1.0d),
            RunOutcome.SUCCESS,
            new ValidationReport(ToleranceProfile.STANDARD, List.of()),
            "");

    String markdown = new MarkdownReportWriter().render(List.of(record));
    String csv = new CsvReportWriter().render(List.of(record));
    String json = new JsonReportWriter().render(List.of(record));

    assertTrue(markdown.contains("Parse Seconds"));
    assertTrue(markdown.contains("Export Seconds"));
    assertTrue(markdown.contains("Validation Seconds"));
    assertTrue(markdown.contains("Peak Memory Bytes"));
    assertTrue(markdown.contains("Processors"));
    assertTrue(csv.startsWith("mode,suite,instance,solver,version,solver_binary_path,status"));
    assertTrue(csv.contains("parse_seconds,export_seconds,solve_seconds,validation_seconds"));
    assertTrue(csv.contains("warmup_count,repetition_count,solve_min_seconds"));
    assertTrue(csv.contains("peak_memory_bytes,residuals,os,arch,java,processors,termination"));
    assertTrue(json.contains("\"version\":\"1.0\""));
    assertTrue(json.contains("\"mode\":\"default\""));
    assertTrue(json.contains("\"solverBinaryPath\":\"not-measured\""));
    assertTrue(json.contains("\"tolerance\":\"STANDARD\""));
    assertTrue(json.contains("\"timeLimitSeconds\":30"));
    assertTrue(json.contains("\"parseSeconds\":0.0"));
    assertTrue(json.contains("\"exportSeconds\":0.0"));
    assertTrue(json.contains("\"solveSeconds\":0.25"));
    assertTrue(json.contains("\"validationSeconds\":0.5"));
    assertTrue(json.contains("\"totalSeconds\":0.75"));
    assertTrue(json.contains("\"warmupCount\":0"));
    assertTrue(json.contains("\"repetitionCount\":1"));
    assertTrue(json.contains("\"solveMedianSeconds\":0.25"));
    assertTrue(json.contains("\"peakMemoryBytes\":\"not-measured\""));
    assertTrue(json.contains("\"termination\":\"message\""));
  }

  @Test
  void rendersEmptyCsvAndJsonDeterministically() {
    assertEquals(
        "mode,suite,instance,solver,version,solver_binary_path,status,objective,outcome,"
            + "accepted,tolerance,threads,time_limit_seconds,parse_seconds,export_seconds,"
            + "solve_seconds,validation_seconds,total_seconds,warmup_count,repetition_count,"
            + "solve_min_seconds,solve_median_seconds,solve_max_seconds,failure_count,"
            + "unavailable_count,peak_memory_bytes,residuals,os,arch,java,processors,"
            + "termination\n",
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

  @Test
  void rendersModeVersionAndSolverPathAcrossFormats() {
    RunRecord record =
        record(
                SolverStatus.OPTIMAL,
                OptionalDouble.of(1.0d),
                RunOutcome.SUCCESS,
                new ValidationReport(ToleranceProfile.STANDARD, List.of()),
                "")
            .withReportMetadata("strict", "HiGHS 1.14.0", "/usr/local/bin/highs");

    String markdown = new MarkdownReportWriter().render(List.of(record));
    String csv = new CsvReportWriter().render(List.of(record));
    String json = new JsonReportWriter().render(List.of(record));

    assertTrue(
        markdown.contains("|strict|suite|instance|solver|HiGHS 1.14.0|/usr/local/bin/highs|"));
    assertTrue(csv.contains("strict,suite,instance,solver,HiGHS 1.14.0,/usr/local/bin/highs,"));
    assertTrue(json.contains("\"mode\":\"strict\""));
    assertTrue(json.contains("\"solverBinaryPath\":\"/usr/local/bin/highs\""));
  }

  @Test
  void summarizesRepeatedAcceptedTimingSamplesAndExcludesRejectedRuns() {
    RunRecord first =
        record(
            SolverStatus.OPTIMAL,
            OptionalDouble.of(1.0d),
            RunOutcome.SUCCESS,
            new ValidationReport(ToleranceProfile.STANDARD, List.of()),
            "",
            0.10d);
    RunRecord second =
        record(
            SolverStatus.OPTIMAL,
            OptionalDouble.of(1.0d),
            RunOutcome.SUCCESS,
            new ValidationReport(ToleranceProfile.STANDARD, List.of()),
            "",
            0.30d);
    RunRecord rejected =
        record(
            SolverStatus.OPTIMAL,
            OptionalDouble.of(2.0d),
            RunOutcome.VALIDATION_FAILED,
            new ValidationReport(
                ToleranceProfile.STANDARD,
                List.of(new ValidationFinding("OBJECTIVE_MISMATCH", "bad objective", 1.0d))),
            "",
            5.00d);

    String json = new JsonReportWriter().render(List.of(first, second, rejected));
    String csv = new CsvReportWriter().render(List.of(first, second, rejected));
    String markdown = new MarkdownReportWriter().render(List.of(first, second, rejected));

    assertTrue(json.contains("\"repetitionCount\":3"));
    assertTrue(json.contains("\"solveMinSeconds\":0.1"));
    assertTrue(json.contains("\"solveMedianSeconds\":0.2"));
    assertTrue(json.contains("\"solveMaxSeconds\":0.3"));
    assertTrue(json.contains("\"failureCount\":1"));
    assertTrue(csv.contains(",0,3,0.1,0.2,0.3,1,0,"));
    assertTrue(markdown.contains("|0|3|0.1|0.2|0.3|1|0|"));
  }

  @Test
  void excludesLimitStatusesFromTimingAndCountsUnavailableRecords() {
    RunRecord timeout =
        record(
            SolverStatus.TIME_LIMIT,
            OptionalDouble.empty(),
            RunOutcome.SUCCESS,
            new ValidationReport(ToleranceProfile.STANDARD, List.of()),
            "",
            5.00d);
    RunRecord unsupported =
        record(
            SolverStatus.UNSUPPORTED,
            OptionalDouble.empty(),
            RunOutcome.UNSUPPORTED,
            new ValidationReport(
                ToleranceProfile.STANDARD,
                List.of(new ValidationFinding("UNSUPPORTED", "unsupported subset", 1.0d))),
            "unsupported subset",
            0.00d);

    String json = new JsonReportWriter().render(List.of(timeout, unsupported));
    String csv = new CsvReportWriter().render(List.of(timeout, unsupported));
    String markdown = new MarkdownReportWriter().render(List.of(timeout, unsupported));

    assertTrue(json.contains("\"repetitionCount\":2"));
    assertTrue(json.contains("\"solveMinSeconds\":\"not-measured\""));
    assertTrue(json.contains("\"solveMedianSeconds\":\"not-measured\""));
    assertTrue(json.contains("\"solveMaxSeconds\":\"not-measured\""));
    assertTrue(json.contains("\"failureCount\":1"));
    assertTrue(json.contains("\"unavailableCount\":1"));
    assertTrue(csv.contains(",0,2,not-measured,not-measured,not-measured,1,1,"));
    assertTrue(markdown.contains("|0|2|not-measured|not-measured|not-measured|1|1|"));
  }

  private static RunRecord record(
      final SolverStatus status,
      final OptionalDouble objective,
      final RunOutcome outcome,
      final ValidationReport validationReport,
      final String failureMessage) {
    return record(status, objective, outcome, validationReport, failureMessage, 0.25d);
  }

  private static RunRecord record(
      final SolverStatus status,
      final OptionalDouble objective,
      final RunOutcome outcome,
      final ValidationReport validationReport,
      final String failureMessage,
      final double solveSeconds) {
    return new RunRecord(
        "suite",
        "instance",
        new SolverRunResult(
            new SolverId("solver", "test"),
            status,
            objective,
            new double[0],
            solveSeconds,
            "message"),
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
