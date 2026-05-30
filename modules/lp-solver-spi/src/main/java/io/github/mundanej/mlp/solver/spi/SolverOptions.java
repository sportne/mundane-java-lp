package io.github.mundanej.mlp.solver.spi;

import java.time.Duration;

/** Common solver options. */
public record SolverOptions(Duration timeLimit, int threads) {
    /** Creates options. */
    public SolverOptions {
        if (timeLimit == null) {
            throw new IllegalArgumentException("timeLimit must not be null");
        }
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be at least one");
        }
    }

    /** Default solver options. */
    public static SolverOptions defaults() {
        return new SolverOptions(Duration.ofSeconds(60), 1);
    }
}
