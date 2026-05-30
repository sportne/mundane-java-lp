package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;
import java.util.List;

/** Writes a small Markdown report for run records. */
public final class MarkdownReportWriter {
    /** Renders run records as Markdown. */
    public String render(final List<RunRecord> records) {
        StringBuilder builder = new StringBuilder();
        builder.append("# LP benchmark report\n\n");
        builder.append("| Instance | Solver | Status | Accepted |\n");
        builder.append("|---|---|---:|---:|\n");
        for (RunRecord record : records) {
            builder.append('|').append(record.instanceId())
                    .append('|').append(record.solverResult().solverId().name())
                    .append('|').append(record.solverResult().status())
                    .append('|').append(record.validationReport().accepted())
                    .append('|').append('\n');
        }
        return builder.toString();
    }
}
