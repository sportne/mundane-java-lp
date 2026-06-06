package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;
import java.util.List;
import java.util.Map;

/** Writes a small Markdown report for run records. */
public final class MarkdownReportWriter {
  /**
   * Renders run records as Markdown.
   *
   * @param records run records to render
   */
  public String render(final List<RunRecord> records) {
    Map<ReportStatistics.Key, ReportStatistics.Summary> summaries =
        ReportStatistics.summarize(records);
    StringBuilder builder = new StringBuilder();
    builder.append("# LP benchmark report\n\n");
    builder.append(
        "| Mode | Suite | Instance | Solver | Version | Solver Binary Path | Status | Objective | "
            + "Outcome | Accepted | Tolerance | Residuals | Threads | Time Limit | Parse Seconds | "
            + "Export Seconds | Solve Seconds | Validation Seconds | Total Seconds | "
            + "Warmup Count | Repetition Count | Solve Min Seconds | Solve Median Seconds | "
            + "Solve Max Seconds | Failure Count | Unavailable Count | "
            + "Peak Memory Bytes | OS | Arch | Java | Processors | Termination |\n");
    builder.append(
        "|---|---|---|---|---|---|---:|---:|---|---:|---|---|---:|---:|"
            + "---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|"
            + "---|---|---|---|---:|---|\n");
    for (RunRecord record : records) {
      ReportStatistics.Summary summary = summaries.get(ReportStatistics.Key.from(record));
      builder
          .append('|')
          .append(escape(record.runMode()))
          .append('|')
          .append(escape(record.suiteId()))
          .append('|')
          .append(escape(record.instanceId()))
          .append('|')
          .append(escape(record.solverResult().solverId().name()))
          .append('|')
          .append(escape(record.solverVersion()))
          .append('|')
          .append(escape(record.solverBinaryPath()))
          .append('|')
          .append(record.solverResult().status())
          .append('|')
          .append(ReportFields.objective(record))
          .append('|')
          .append(record.outcome())
          .append('|')
          .append(record.validationReport().accepted())
          .append('|')
          .append(record.validationReport().toleranceProfile())
          .append('|')
          .append(escape(ReportFields.residualSummary(record)))
          .append('|')
          .append(record.solverOptions().threads())
          .append('|')
          .append(record.solverOptions().timeLimit().toSeconds())
          .append('|')
          .append(escape(record.parseSecondsReportValue()))
          .append('|')
          .append(escape(record.exportSecondsReportValue()))
          .append('|')
          .append(record.solverResult().elapsedSeconds())
          .append('|')
          .append(escape(record.validationSecondsReportValue()))
          .append('|')
          .append(escape(record.totalSecondsReportValue()))
          .append('|')
          .append(summary.warmupCount())
          .append('|')
          .append(summary.repetitionCount())
          .append('|')
          .append(escape(summary.solveMinSeconds()))
          .append('|')
          .append(escape(summary.solveMedianSeconds()))
          .append('|')
          .append(escape(summary.solveMaxSeconds()))
          .append('|')
          .append(summary.failureCount())
          .append('|')
          .append(summary.unavailableCount())
          .append('|')
          .append(escape(record.peakMemoryBytes()))
          .append('|')
          .append(escape(record.machineFingerprint().osName()))
          .append('|')
          .append(escape(record.machineFingerprint().osArch()))
          .append('|')
          .append(escape(record.machineFingerprint().javaVersion()))
          .append('|')
          .append(record.machineFingerprint().availableProcessors())
          .append('|')
          .append(escape(ReportFields.termination(record)))
          .append('|')
          .append('\n');
    }
    return builder.toString();
  }

  private static String escape(final String value) {
    return value.replace("\r", "\\r").replace("\n", "\\n").replace("|", "\\|");
  }
}
