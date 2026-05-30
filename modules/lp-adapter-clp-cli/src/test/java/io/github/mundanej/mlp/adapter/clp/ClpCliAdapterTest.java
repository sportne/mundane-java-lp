package io.github.mundanej.mlp.adapter.clp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class ClpCliAdapterTest {
    @Test
    void hasExpectedId() {
        assertEquals("clp", new ClpCliAdapter().id().name());
    }
}
