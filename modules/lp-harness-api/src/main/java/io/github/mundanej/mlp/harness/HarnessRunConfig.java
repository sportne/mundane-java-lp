package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import java.nio.file.Path;

/**
 * Configuration for a harness execution.
 *
 * @param workRoot root directory for per-run solver work directories
 * @param solverOptions solver options passed to every adapter
 * @param toleranceProfile validation tolerance profile
 */
public record HarnessRunConfig(
        Path workRoot,
        SolverOptions solverOptions,
        ToleranceProfile toleranceProfile) {
    /**
     * Creates run configuration.
     *
     * @param workRoot root directory for per-run solver work directories
     * @param solverOptions solver options passed to every adapter
     * @param toleranceProfile validation tolerance profile
     */
    public HarnessRunConfig {
        if (workRoot == null) {
            throw new IllegalArgumentException("workRoot must not be null");
        }
        if (solverOptions == null) {
            throw new IllegalArgumentException("solverOptions must not be null");
        }
        if (toleranceProfile == null) {
            throw new IllegalArgumentException("toleranceProfile must not be null");
        }
    }
}
