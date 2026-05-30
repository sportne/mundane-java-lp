package io.github.mundanej.mlp.solver.spi;

import java.util.OptionalDouble;

/** Normalized solver run result. */
public record SolverRunResult(
        SolverId solverId,
        SolverStatus status,
        OptionalDouble objectiveValue,
        double elapsedSeconds,
        String message) {
    /** Creates a run result. */
    public SolverRunResult {
        if (solverId == null) {
            throw new IllegalArgumentException("solverId must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (objectiveValue == null) {
            throw new IllegalArgumentException("objectiveValue must not be null");
        }
        if (elapsedSeconds < 0.0d) {
            throw new IllegalArgumentException("elapsedSeconds must be non-negative");
        }
        if (message == null) {
            message = "";
        }
    }
}
