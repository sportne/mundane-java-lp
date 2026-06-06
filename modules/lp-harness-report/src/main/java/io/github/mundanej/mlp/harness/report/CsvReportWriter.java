package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;
import java.util.List;
import java.util.Map;

/** Writes deterministic CSV reports for run records. */
public final class CsvReportWriter {
  /**
   * Renders run records as CSV.
   *
   * @param records run records in report order
   */
  public String render(final List<RunRecord> records) {
    Map<ReportStatistics.Key, ReportStatistics.Summary> summaries =
        ReportStatistics.summarize(records);
    StringBuilder out = new StringBuilder();
    out.append("mode,suite,instance,solver,version,solver_binary_path,status,objective,outcome,")
        .append("accepted,tolerance,threads,time_limit_seconds,parse_seconds,export_seconds,")
        .append("solve_seconds,validation_seconds,total_seconds,warmup_count,repetition_count,")
        .append("solve_min_seconds,solve_median_seconds,solve_max_seconds,failure_count,")
        .append(
            "unavailable_count,peak_memory_bytes,residuals,os,arch,java,processors,termination\n");
    for (RunRecord record : records) {
      ReportStatistics.Summary summary = summaries.get(ReportStatistics.Key.from(record));
      append(out, record.runMode());
      append(out, record.suiteId());
      append(out, record.instanceId());
      append(out, record.solverResult().solverId().name());
      append(out, record.solverVersion());
      append(out, record.solverBinaryPath());
      append(out, record.solverResult().status().name());
      append(out, ReportFields.objective(record));
      append(out, record.outcome().name());
      append(out, Boolean.toString(record.validationReport().accepted()));
      append(out, record.validationReport().toleranceProfile().name());
      append(out, Integer.toString(record.solverOptions().threads()));
      append(out, Long.toString(record.solverOptions().timeLimit().toSeconds()));
      append(out, record.parseSecondsReportValue());
      append(out, record.exportSecondsReportValue());
      append(out, Double.toString(record.solverResult().elapsedSeconds()));
      append(out, record.validationSecondsReportValue());
      append(out, record.totalSecondsReportValue());
      append(out, Integer.toString(summary.warmupCount()));
      append(out, Integer.toString(summary.repetitionCount()));
      append(out, summary.solveMinSeconds());
      append(out, summary.solveMedianSeconds());
      append(out, summary.solveMaxSeconds());
      append(out, Integer.toString(summary.failureCount()));
      append(out, Integer.toString(summary.unavailableCount()));
      append(out, record.peakMemoryBytes());
      append(out, ReportFields.residualSummary(record));
      append(out, record.machineFingerprint().osName());
      append(out, record.machineFingerprint().osArch());
      append(out, record.machineFingerprint().javaVersion());
      append(out, Integer.toString(record.machineFingerprint().availableProcessors()));
      out.append(escape(ReportFields.termination(record))).append('\n');
    }
    return out.toString();
  }

  private static void append(final StringBuilder out, final String value) {
    out.append(escape(value)).append(',');
  }

  private static String escape(final String value) {
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
