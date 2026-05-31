package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;
import java.util.List;

/** Writes a small Markdown report for run records. */
public final class MarkdownReportWriter {
    /**
     * Renders run records as Markdown.
     *
     * @param records run records to render
     */
    public String render(final List<RunRecord> records) {
        StringBuilder builder = new StringBuilder();
        builder.append("# LP benchmark report\n\n");
        builder.append("| Suite | Instance | Solver | Version | Status | Objective | Outcome | Accepted | "
                + "Tolerance | Residuals | Threads | Time Limit | Parse Seconds | Export Seconds | "
                + "Solve Seconds | Validation Seconds | Total Seconds | OS | Arch | Java | Processors | "
                + "Termination |\n");
        builder.append("|---|---|---|---|---:|---:|---|---:|---|---|---:|---:|---:|---:|---:|---:|---:|---|---|---|---:|---|\n");
        for (RunRecord record : records) {
            builder.append('|').append(escape(record.suiteId()))
                    .append('|').append(escape(record.instanceId()))
                    .append('|').append(escape(record.solverResult().solverId().name()))
                    .append('|').append(escape(record.solverVersion()))
                    .append('|').append(record.solverResult().status())
                    .append('|').append(ReportFields.objective(record))
                    .append('|').append(record.outcome())
                    .append('|').append(record.validationReport().accepted())
                    .append('|').append(record.validationReport().toleranceProfile())
                    .append('|').append(escape(ReportFields.residualSummary(record)))
                    .append('|').append(record.solverOptions().threads())
                    .append('|').append(record.solverOptions().timeLimit().toSeconds())
                    .append('|').append(record.parseSeconds())
                    .append('|').append(record.exportSeconds())
                    .append('|').append(record.solverResult().elapsedSeconds())
                    .append('|').append(record.validationSeconds())
                    .append('|').append(record.totalSeconds())
                    .append('|').append(escape(record.machineFingerprint().osName()))
                    .append('|').append(escape(record.machineFingerprint().osArch()))
                    .append('|').append(escape(record.machineFingerprint().javaVersion()))
                    .append('|').append(record.machineFingerprint().availableProcessors())
                    .append('|').append(escape(ReportFields.termination(record)))
                    .append('|').append('\n');
        }
        return builder.toString();
    }

    private static String escape(final String value) {
        return value.replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("|", "\\|");
    }
}
