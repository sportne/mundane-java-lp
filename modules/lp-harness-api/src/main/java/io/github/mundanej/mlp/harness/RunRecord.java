package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
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
 * @param solverVersion solver version or not-measured
 * @param solverOptions solver options
 * @param machineFingerprint machine metadata
 * @param parseSeconds parse time in seconds
 * @param exportSeconds export/canonicalization time in seconds
 * @param validationSeconds validation time in seconds
 * @param totalSeconds total wall time in seconds
 */
public record RunRecord(
        String suiteId,
        String instanceId,
        SolverRunResult solverResult,
        ValidationReport validationReport,
        RunOutcome outcome,
        String failureMessage,
        String solverVersion,
        SolverOptions solverOptions,
        MachineFingerprint machineFingerprint,
        double parseSeconds,
        double exportSeconds,
        double validationSeconds,
        double totalSeconds) {
    /**
     * Creates a run record.
     *
     * @param suiteId benchmark suite identifier
     * @param instanceId benchmark instance identifier
     * @param solverResult normalized solver result
     * @param validationReport validation report
     * @param outcome deterministic harness outcome
     * @param failureMessage failure diagnostic; blank when not applicable
     * @param solverVersion solver version or not-measured
     * @param solverOptions solver options
     * @param machineFingerprint machine metadata
     * @param parseSeconds parse time in seconds
     * @param exportSeconds export/canonicalization time in seconds
     * @param validationSeconds validation time in seconds
     * @param totalSeconds total wall time in seconds
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
        if (solverVersion == null || solverVersion.isBlank()) {
            solverVersion = "not-measured";
        }
        if (solverOptions == null) {
            throw new IllegalArgumentException("solverOptions must not be null");
        }
        if (machineFingerprint == null) {
            throw new IllegalArgumentException("machineFingerprint must not be null");
        }
        if (parseSeconds < 0.0d || exportSeconds < 0.0d
                || validationSeconds < 0.0d || totalSeconds < 0.0d) {
            throw new IllegalArgumentException("timings must be non-negative");
        }
    }

    /**
     * Creates a run record without explicit report metadata.
     *
     * @param suiteId benchmark suite identifier
     * @param instanceId benchmark instance identifier
     * @param solverResult normalized solver result
     * @param validationReport validation report
     * @param outcome deterministic harness outcome
     * @param failureMessage failure diagnostic; blank when not applicable
     */
    public RunRecord(
            final String suiteId,
            final String instanceId,
            final SolverRunResult solverResult,
            final ValidationReport validationReport,
            final RunOutcome outcome,
            final String failureMessage) {
        this(suiteId, instanceId, solverResult, validationReport, outcome, failureMessage,
                "not-measured", SolverOptions.defaults(), MachineFingerprint.capture(),
                0.0d, 0.0d, 0.0d, solverResult.elapsedSeconds());
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
