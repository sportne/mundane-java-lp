package io.github.mundanej.mlp.harness.cli;

import io.github.mundanej.mlp.generators.TinyLpGenerator;
import io.github.mundanej.mlp.harness.MachineFingerprint;

/** Command-line entrypoint for the LP benchmark harness. */
public final class MlpBenchMain {
    private MlpBenchMain() {
    }

    /** Runs the G0 CLI placeholder. */
    public static void main(final String[] args) {
        if (args.length > 0 && "--help".equals(args[0])) {
            printHelp();
            return;
        }
        MachineFingerprint fingerprint = MachineFingerprint.capture();
        int columns = new TinyLpGenerator().singleBoundedVariable().stats().columns();
        System.out.println("mlpbench G0 scaffold");
        System.out.println("java=" + fingerprint.javaVersion());
        System.out.println("tinyColumns=" + columns);
    }

    private static void printHelp() {
        System.out.println("Usage: mlpbench [--help]");
        System.out.println("G0 placeholder. Future commands: run, generate, validate, report.");
    }
}
