package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.solver.spi.SolverOptions;
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
 * @param solverVersion solver version or not-measured
 * @param solverOptions solver options
 * @param machineFingerprint machine metadata
 * @param parseSeconds parse time in seconds
 * @param exportSeconds export/canonicalization time in seconds
 * @param validationSeconds validation time in seconds
 * @param totalSeconds total wall time in seconds
 * @param peakMemoryBytes peak memory in bytes or not-measured
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
    double totalSeconds,
    String peakMemoryBytes) {
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
   * @param peakMemoryBytes peak memory in bytes or not-measured
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
    requireTiming(parseSeconds, "parseSeconds");
    requireTiming(exportSeconds, "exportSeconds");
    requireTiming(validationSeconds, "validationSeconds");
    requireTiming(totalSeconds, "totalSeconds");
    if (peakMemoryBytes == null || peakMemoryBytes.isBlank()) {
      peakMemoryBytes = "not-measured";
    }
  }

  /** Returns parse seconds for reports, or {@code not-measured}. */
  public String parseSecondsReportValue() {
    return timingReportValue(parseSeconds);
  }

  /** Returns export/canonicalization seconds for reports, or {@code not-measured}. */
  public String exportSecondsReportValue() {
    return timingReportValue(exportSeconds);
  }

  /** Returns validation seconds for reports, or {@code not-measured}. */
  public String validationSecondsReportValue() {
    return timingReportValue(validationSeconds);
  }

  /** Returns total wall seconds for reports, or {@code not-measured}. */
  public String totalSecondsReportValue() {
    return timingReportValue(totalSeconds);
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
    this(
        suiteId,
        instanceId,
        solverResult,
        validationReport,
        outcome,
        failureMessage,
        "not-measured",
        SolverOptions.defaults(),
        MachineFingerprint.capture(),
        Double.NaN,
        Double.NaN,
        Double.NaN,
        solverResult.elapsedSeconds(),
        "not-measured");
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
    this(
        "default",
        instanceId,
        solverResult,
        validationReport,
        validationReport.accepted() ? RunOutcome.SUCCESS : RunOutcome.VALIDATION_FAILED,
        "");
  }

  private static void requireTiming(final double value, final String label) {
    if (Double.isNaN(value)) {
      return;
    }
    if (!Double.isFinite(value) || value < 0.0d) {
      throw new IllegalArgumentException(label + " must be non-negative or not-measured");
    }
  }

  private static String timingReportValue(final double value) {
    return Double.isNaN(value) ? "not-measured" : Double.toString(value);
  }
}
