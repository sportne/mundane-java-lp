package io.github.mundanej.mlp.nativeapi;

/** Native API status for GraalVM smoke lanes. */
public final class NativeApiStatus {
  private NativeApiStatus() {}

  /** Returns current native API status. */
  public static String status() {
    return "native API smoke ready; C ABI remains deferred.";
  }
}
