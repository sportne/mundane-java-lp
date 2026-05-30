package io.github.mundanej.mlp.solver.spi;

import java.nio.file.Path;

/** Work directory made available to solver adapters. */
public record SolverWorkDirectory(Path path) {
    /** Creates a work directory. */
    public SolverWorkDirectory {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
    }
}
