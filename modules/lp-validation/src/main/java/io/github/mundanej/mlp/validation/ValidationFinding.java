package io.github.mundanej.mlp.validation;

/** One validation finding. */
public record ValidationFinding(String code, String message, double magnitude) {
    /** Creates a finding. */
    public ValidationFinding {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
