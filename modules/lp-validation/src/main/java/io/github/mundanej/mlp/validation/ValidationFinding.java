package io.github.mundanej.mlp.validation;

/**
 * One validation finding.
 *
 * @param code stable finding code
 * @param message human-readable finding message
 * @param magnitude violation magnitude
 */
public record ValidationFinding(String code, String message, double magnitude) {
    /**
     * Creates a finding.
     *
     * @param code stable finding code
     * @param message human-readable finding message
     * @param magnitude violation magnitude
     */
    public ValidationFinding {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
