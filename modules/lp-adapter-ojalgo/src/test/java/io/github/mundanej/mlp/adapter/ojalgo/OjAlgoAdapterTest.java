package io.github.mundanej.mlp.adapter.ojalgo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class OjAlgoAdapterTest {
    @Test
    void hasExpectedId() {
        assertEquals("ojalgo", new OjAlgoAdapter().id().name());
    }
}
