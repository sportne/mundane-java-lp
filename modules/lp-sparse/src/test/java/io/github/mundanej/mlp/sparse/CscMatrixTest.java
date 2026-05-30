package io.github.mundanej.mlp.sparse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class CscMatrixTest {
    @Test
    void multipliesVector() {
        CscMatrix matrix = new CscMatrix(
                2,
                2,
                new double[] {1.0d, 2.0d, 3.0d},
                new int[] {0, 1, 1},
                new int[] {0, 2, 3});
        assertArrayEquals(new double[] {4.0d, 11.0d}, matrix.multiply(new double[] {4.0d, 1.0d}));
    }

    @Test
    void rejectsBadPointers() {
        assertThrows(IllegalArgumentException.class, () -> new CscMatrix(
                1,
                1,
                new double[] {1.0d},
                new int[] {0},
                new int[] {0, 2}));
    }
}
