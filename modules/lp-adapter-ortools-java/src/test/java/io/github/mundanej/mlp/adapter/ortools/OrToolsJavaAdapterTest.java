package io.github.mundanej.mlp.adapter.ortools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class OrToolsJavaAdapterTest {
    @Test
    void hasExpectedId() {
        assertEquals("ortools", new OrToolsJavaAdapter().id().name());
    }
}
