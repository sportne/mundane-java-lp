package io.github.mundanej.mlp.solver.spi;

/** Stable solver identifier. */
public record SolverId(String name, String mode) {
    /** Creates an identifier. */
    public SolverId {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (mode == null || mode.isBlank()) {
            throw new IllegalArgumentException("mode must not be blank");
        }
    }
}
