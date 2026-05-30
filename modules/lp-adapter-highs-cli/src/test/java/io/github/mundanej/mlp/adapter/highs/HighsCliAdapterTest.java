package io.github.mundanej.mlp.adapter.highs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class HighsCliAdapterTest {
    @Test
    void hasExpectedId() {
        assertEquals("highs", new HighsCliAdapter().id().name());
    }
}
