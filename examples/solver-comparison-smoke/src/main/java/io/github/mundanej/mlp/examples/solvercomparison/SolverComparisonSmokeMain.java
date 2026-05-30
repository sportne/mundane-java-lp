package io.github.mundanej.mlp.examples.solvercomparison;

import io.github.mundanej.mlp.harness.MachineFingerprint;

/** G0 solver comparison smoke placeholder. */
public final class SolverComparisonSmokeMain {
    private SolverComparisonSmokeMain() {
    }

    /** Runs the placeholder. */
    public static void main(final String[] args) {
        MachineFingerprint fingerprint = MachineFingerprint.capture();
        System.out.println("solver comparison smoke placeholder");
        System.out.println("java=" + fingerprint.javaVersion());
    }
}
