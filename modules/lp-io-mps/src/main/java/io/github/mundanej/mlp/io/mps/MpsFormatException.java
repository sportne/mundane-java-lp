package io.github.mundanej.mlp.io.mps;

/** Exception raised for unsupported or malformed MPS content. */
public final class MpsFormatException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception.
   *
   * @param message diagnostic message
   */
  public MpsFormatException(final String message) {
    super(message);
  }
}
