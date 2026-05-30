package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.validation.ValidationReport;

/** Result of running one solver on one instance. */
public record RunRecord(String instanceId, SolverRunResult solverResult, ValidationReport validationReport) {
    /** Creates a run record. */
    public RunRecord {
        if (instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("instanceId must not be blank");
        }
        if (solverResult == null) {
            throw new IllegalArgumentException("solverResult must not be null");
        }
        if (validationReport == null) {
            throw new IllegalArgumentException("validationReport must not be null");
        }
    }
}
