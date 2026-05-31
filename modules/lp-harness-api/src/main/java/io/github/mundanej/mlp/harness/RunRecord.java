package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.validation.ValidationReport;

/**
 * Result of running one solver on one instance.
 *
 * @param suiteId benchmark suite identifier
 * @param instanceId benchmark instance identifier
 * @param solverResult normalized solver result
 * @param validationReport validation report
 * @param outcome deterministic harness outcome
 * @param failureMessage failure diagnostic; blank when not applicable
 */
public record RunRecord(
        String suiteId,
        String instanceId,
        SolverRunResult solverResult,
        ValidationReport validationReport,
        RunOutcome outcome,
        String failureMessage) {
    /**
     * Creates a run record.
     *
     * @param suiteId benchmark suite identifier
     * @param instanceId benchmark instance identifier
     * @param solverResult normalized solver result
     * @param validationReport validation report
     * @param outcome deterministic harness outcome
     * @param failureMessage failure diagnostic; blank when not applicable
     */
    public RunRecord {
        if (suiteId == null || suiteId.isBlank()) {
            throw new IllegalArgumentException("suiteId must not be blank");
        }
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("instanceId must not be blank");
        }
        if (solverResult == null) {
            throw new IllegalArgumentException("solverResult must not be null");
        }
        if (validationReport == null) {
            throw new IllegalArgumentException("validationReport must not be null");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("outcome must not be null");
        }
        if (failureMessage == null) {
            failureMessage = "";
        }
    }

    /**
     * Creates a run record with the legacy minimal fields.
     *
     * @param instanceId benchmark instance identifier
     * @param solverResult normalized solver result
     * @param validationReport validation report
     */
    public RunRecord(
            final String instanceId,
            final SolverRunResult solverResult,
            final ValidationReport validationReport) {
        this("default", instanceId, solverResult, validationReport,
                validationReport.accepted() ? RunOutcome.SUCCESS : RunOutcome.VALIDATION_FAILED, "");
    }
}
