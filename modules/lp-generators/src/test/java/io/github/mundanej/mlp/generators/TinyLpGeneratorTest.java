package io.github.mundanej.mlp.generators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class TinyLpGeneratorTest {
    @Test
    void createsSingleVariableProblem() {
        assertEquals(1, new TinyLpGenerator().singleBoundedVariable().stats().columns());
        assertEquals("single-bounded-variable", new TinyLpGenerator().singleBoundedVariable().name());
    }
}
