package io.github.mundanej.mlp.harness;

/** Small machine metadata snapshot for benchmark reports. */
public record MachineFingerprint(String osName, String osArch, String javaVersion, int availableProcessors) {
    /** Captures local machine metadata. */
    public static MachineFingerprint capture() {
        return new MachineFingerprint(
                System.getProperty("os.name", "unknown"),
                System.getProperty("os.arch", "unknown"),
                System.getProperty("java.version", "unknown"),
                Runtime.getRuntime().availableProcessors());
    }
}
