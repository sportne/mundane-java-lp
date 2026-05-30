package io.github.mundanej.mlp.harness;

/**
 * Small machine metadata snapshot for benchmark reports.
 *
 * @param osName operating system name
 * @param osArch operating system architecture
 * @param javaVersion Java runtime version
 * @param availableProcessors available processor count
 */
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
