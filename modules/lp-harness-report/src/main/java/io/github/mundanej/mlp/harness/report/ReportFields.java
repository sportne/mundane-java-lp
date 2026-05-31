package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunRecord;

final class ReportFields {
    private ReportFields() {
    }

    static String objective(final RunRecord record) {
        return record.solverResult().objectiveValue().isPresent()
                ? Double.toString(record.solverResult().objectiveValue().getAsDouble())
                : "";
    }

    static String residualSummary(final RunRecord record) {
        return record.validationReport().findings().isEmpty()
                ? "none"
                : Integer.toString(record.validationReport().findings().size());
    }

    static String termination(final RunRecord record) {
        return record.failureMessage().isBlank() ? record.solverResult().message() : record.failureMessage();
    }
}
