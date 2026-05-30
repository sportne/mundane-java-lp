package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.model.LpProblem;

/**
 * A named LP benchmark instance.
 *
 * @param id instance identifier
 * @param problem LP problem
 */
public record BenchmarkInstance(String id, LpProblem problem) {
    /**
     * Creates an instance.
     *
     * @param id instance identifier
     * @param problem LP problem
     */
    public BenchmarkInstance {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (problem == null) {
            throw new IllegalArgumentException("problem must not be null");
        }
    }
}
