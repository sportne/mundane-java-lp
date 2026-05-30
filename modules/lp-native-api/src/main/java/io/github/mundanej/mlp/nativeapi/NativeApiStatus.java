package io.github.mundanej.mlp.nativeapi;

/** Native API scaffold status. */
public final class NativeApiStatus {
    private NativeApiStatus() {
    }

    /** Returns current native API status. */
    public static String status() {
        return "G0 native API placeholder; C ABI is deferred until harness CLI stabilizes.";
    }
}
