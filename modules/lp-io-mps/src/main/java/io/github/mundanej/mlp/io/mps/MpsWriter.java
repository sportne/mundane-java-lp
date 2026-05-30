package io.github.mundanej.mlp.io.mps;

import io.github.mundanej.mlp.model.LpProblem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Minimal G0 MPS writer placeholder. */
public final class MpsWriter {
    /** Writes a validation-only minimal MPS shell. */
    public void write(final LpProblem problem, final Path path) throws IOException {
        Objects.requireNonNull(problem, "problem");
        Objects.requireNonNull(path, "path");
        Files.writeString(path, "NAME          " + problem.name() + System.lineSeparator()
                + "ENDATA" + System.lineSeparator());
    }
}
