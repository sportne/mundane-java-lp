package io.github.mundanej.mlp.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class LpTestInstancesTest {
    @Test
    void exposesTierOneFixtures() {
        assertEquals(10, LpTestInstances.tierOneFixtures().size());
    }
}
