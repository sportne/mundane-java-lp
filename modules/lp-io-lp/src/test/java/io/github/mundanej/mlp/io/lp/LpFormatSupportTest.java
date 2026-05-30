package io.github.mundanej.mlp.io.lp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class LpFormatSupportTest {
    @Test
    void reportsDeferredStatus() {
        assertTrue(LpFormatSupport.status().contains("deferred"));
    }
}
