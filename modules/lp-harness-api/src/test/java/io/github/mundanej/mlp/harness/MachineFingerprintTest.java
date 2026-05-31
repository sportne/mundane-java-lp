package io.github.mundanej.mlp.harness;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

final class MachineFingerprintTest {
  @Test
  void capturesJavaVersion() {
    assertFalse(MachineFingerprint.capture().javaVersion().isBlank());
  }
}
