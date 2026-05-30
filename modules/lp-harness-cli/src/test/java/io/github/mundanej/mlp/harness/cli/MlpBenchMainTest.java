package io.github.mundanej.mlp.harness.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

final class MlpBenchMainTest {
    @Test
    void helpDoesNotThrow() {
        assertDoesNotThrow(() -> MlpBenchMain.main(new String[] {"--help"}));
    }
}
