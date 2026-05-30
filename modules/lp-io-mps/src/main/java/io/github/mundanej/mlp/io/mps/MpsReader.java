package io.github.mundanej.mlp.io.mps;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.ObjectiveSense;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Minimal G0 MPS reader placeholder. */
public final class MpsReader {
    /**
     * Reads enough MPS metadata for G0 smoke tests.
     *
     * @param path MPS file path
     */
    public LpProblem read(final Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        String name = "unnamed";
        boolean sawEnd = false;
        for (String line : Files.readAllLines(path)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("NAME")) {
                String[] tokens = trimmed.split("\\s+");
                if (tokens.length > 1) {
                    name = tokens[1];
                }
            }
            if (trimmed.equals("ENDATA")) {
                sawEnd = true;
            }
        }
        if (!sawEnd) {
            throw new MpsFormatException("MPS file missing ENDATA");
        }
        return new LpProblem(
                name,
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[0]),
                List.of(),
                List.of(),
                new LpProblemStats(0, 0, 0));
    }
}
