package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;
import java.util.List;

/** Writes deterministic JSON reports for run records. */
public final class JsonReportWriter {
    /** Renders run records as a JSON array. */
    public String render(final List<RunRecord> records) {
        StringBuilder out = new StringBuilder();
        out.append("[\n");
        for (int index = 0; index < records.size(); index++) {
            RunRecord record = records.get(index);
            out.append("  {");
            field(out, "suite", record.suiteId()).append(',');
            field(out, "instance", record.instanceId()).append(',');
            field(out, "solver", record.solverResult().solverId().name()).append(',');
            field(out, "version", record.solverVersion()).append(',');
            field(out, "status", record.solverResult().status().name()).append(',');
            numericField(out, "objective", ReportFields.objective(record)).append(',');
            field(out, "outcome", record.outcome().name()).append(',');
            out.append("\"accepted\":").append(record.validationReport().accepted()).append(',');
            field(out, "tolerance", record.validationReport().toleranceProfile().name()).append(',');
            field(out, "residuals", ReportFields.residualSummary(record)).append(',');
            out.append("\"threads\":").append(record.solverOptions().threads()).append(',');
            out.append("\"timeLimitSeconds\":").append(record.solverOptions().timeLimit().toSeconds()).append(',');
            out.append("\"parseSeconds\":").append(record.parseSeconds()).append(',');
            out.append("\"exportSeconds\":").append(record.exportSeconds()).append(',');
            out.append("\"solveSeconds\":").append(record.solverResult().elapsedSeconds()).append(',');
            out.append("\"validationSeconds\":").append(record.validationSeconds()).append(',');
            out.append("\"totalSeconds\":").append(record.totalSeconds()).append(',');
            field(out, "os", record.machineFingerprint().osName()).append(',');
            field(out, "arch", record.machineFingerprint().osArch()).append(',');
            field(out, "java", record.machineFingerprint().javaVersion()).append(',');
            out.append("\"processors\":").append(record.machineFingerprint().availableProcessors()).append(',');
            field(out, "termination", ReportFields.termination(record));
            out.append('}');
            if (index + 1 < records.size()) {
                out.append(',');
            }
            out.append('\n');
        }
        out.append("]\n");
        return out.toString();
    }

    private static StringBuilder field(final StringBuilder out, final String name, final String value) {
        return out.append('"').append(name).append("\":\"").append(escape(value)).append('"');
    }

    private static StringBuilder numericField(final StringBuilder out, final String name, final String value) {
        out.append('"').append(name).append("\":");
        return value.isBlank() ? out.append("null") : out.append(value);
    }

    private static String escape(final String value) {
        StringBuilder escaped = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
