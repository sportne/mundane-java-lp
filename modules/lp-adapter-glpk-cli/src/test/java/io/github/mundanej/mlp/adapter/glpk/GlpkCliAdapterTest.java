package io.github.mundanej.mlp.adapter.glpk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class GlpkCliAdapterTest {
    @Test
    void hasExpectedId() {
        assertEquals("glpk", new GlpkCliAdapter().id().name());
    }
}
