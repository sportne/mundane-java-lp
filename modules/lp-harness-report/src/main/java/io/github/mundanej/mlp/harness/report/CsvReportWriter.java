package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;
import java.util.List;

/** Writes deterministic CSV reports for run records. */
public final class CsvReportWriter {
  /**
   * Renders run records as CSV.
   *
   * @param records run records in report order
   */
  public String render(final List<RunRecord> records) {
    StringBuilder out = new StringBuilder();
    out.append("suite,instance,solver,version,status,objective,outcome,accepted,tolerance,threads,")
        .append("time_limit_seconds,parse_seconds,export_seconds,solve_seconds,validation_seconds,")
        .append("total_seconds,peak_memory_bytes,residuals,os,arch,java,processors,termination\n");
    for (RunRecord record : records) {
      append(out, record.suiteId());
      append(out, record.instanceId());
      append(out, record.solverResult().solverId().name());
      append(out, record.solverVersion());
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
