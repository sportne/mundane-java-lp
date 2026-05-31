package io.github.mundanej.mlp.sparse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class CsrMatrixTest {
  @Test
  void multipliesEmptyMatrix() {
    CsrMatrix matrix = new CsrMatrix(0, 2, new double[0], new int[0], new int[] {0});
    assertEquals(0, matrix.rows());
    assertEquals(2, matrix.columns());
    assertEquals(0, matrix.nonzeros());
    assertArrayEquals(new double[0], matrix.multiply(new double[] {1.0d, 2.0d}));
  }

  @Test
  void multipliesSingleEntryMatrix() {
    CsrMatrix matrix = new CsrMatrix(1, 1, new double[] {2.0d}, new int[] {0}, new int[] {0, 1});
    assertArrayEquals(new double[] {6.0d}, matrix.multiply(new double[] {3.0d}));
  }

  @Test
  void multipliesRectangularMatrix() {
    CsrMatrix matrix =
        new CsrMatrix(
            2, 3, new double[] {1.0d, 2.0d, 3.0d}, new int[] {0, 2, 1}, new int[] {0, 2, 3});
    assertArrayEquals(new double[] {7.0d, 6.0d}, matrix.multiply(new double[] {1.0d, 2.0d, 3.0d}));
  }

  @Test
  void multipliesIntoCallerOwnedOutput() {
    CsrMatrix matrix =
        new CsrMatrix(
            3, 3, new double[] {1.0d, 2.0d, 3.0d}, new int[] {0, 2, 1}, new int[] {0, 2, 3, 3});
    double[] output = {99.0d, 99.0d, 99.0d};

    matrix.multiplyInto(new double[] {1.0d, 2.0d, 3.0d}, output);

    assertArrayEquals(new double[] {7.0d, 6.0d, 0.0d}, output);
  }

  @Test
  void copiesRowsIntoCallerOwnedOutputAndClearsStaleValues() {
    CsrMatrix matrix =
        new CsrMatrix(
            3,
            4,
            new double[] {1.0d, 2.0d, 3.0d, -1.0d},
            new int[] {0, 2, 1, 2},
            new int[] {0, 2, 3, 4});
    double[] output = {99.0d, 99.0d, 99.0d, 99.0d};

    matrix.copyRowInto(0, output);
    assertArrayEquals(new double[] {1.0d, 0.0d, 2.0d, 0.0d}, output);

    matrix.copyRowInto(1, output);
    assertArrayEquals(new double[] {0.0d, 3.0d, 0.0d, 0.0d}, output);

    matrix.copyRowInto(2, output);
    assertArrayEquals(new double[] {0.0d, 0.0d, -1.0d, 0.0d}, output);
  }

  @Test
  void accessorsReturnDefensiveCopies() {
    double[] values = {1.0d};
    int[] indices = {0};
    int[] pointers = {0, 1};
    CsrMatrix matrix = new CsrMatrix(1, 1, values, indices, pointers);
    values[0] = 9.0d;
    indices[0] = 9;
    pointers[1] = 9;

    double[] valuesCopy = matrix.values();
    int[] indicesCopy = matrix.columnIndices();
    int[] pointersCopy = matrix.rowPointers();
    valuesCopy[0] = 8.0d;
    indicesCopy[0] = 8;
    pointersCopy[1] = 8;

    assertArrayEquals(new double[] {1.0d}, matrix.values());
    assertArrayEquals(new int[] {0}, matrix.columnIndices());
    assertArrayEquals(new int[] {0, 1}, matrix.rowPointers());
  }

  @Test
  void rejectsBadPointers() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {0}, new int[] {0, 2}));
    assertThrows(
        IllegalArgumentException.class,
        () -> new CsrMatrix(1, 1, new double[0], new int[0], new int[] {1, 1}));
    assertThrows(
        IllegalArgumentException.class,
        () -> new CsrMatrix(1, 1, new double[0], new int[0], new int[] {0, -1}));
  }

  @Test
  void rejectsInvalidShapeAndIndexData() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new CsrMatrix(-1, 1, new double[0], new int[0], new int[] {0}));
    assertThrows(
        IllegalArgumentException.class,
        () -> new CsrMatrix(1, 1, new double[] {1.0d}, new int[0], new int[] {0, 1}));
    assertThrows(
        IllegalArgumentException.class,
        () -> new CsrMatrix(1, 1, new double[] {1.0d}, new int[] {1}, new int[] {0, 1}));
  }

  @Test
  void rejectsVectorLengthMismatch() {
    CsrMatrix matrix = new CsrMatrix(1, 2, new double[0], new int[0], new int[] {0, 0});
    assertThrows(IllegalArgumentException.class, () -> matrix.multiply(new double[] {1.0d}));
    assertThrows(
        IllegalArgumentException.class,
        () -> matrix.multiplyInto(new double[] {1.0d}, new double[] {0.0d}));
  }

  @Test
  void rejectsOutputLengthMismatch() {
    CsrMatrix matrix = new CsrMatrix(2, 1, new double[0], new int[0], new int[] {0, 0, 0});
    assertThrows(
        IllegalArgumentException.class,
        () -> matrix.multiplyInto(new double[] {1.0d}, new double[] {0.0d}));
  }

  @Test
  void rejectsCopyRowIntoInvalidArguments() {
    CsrMatrix matrix = new CsrMatrix(2, 1, new double[0], new int[0], new int[] {0, 0, 0});
    assertThrows(IllegalArgumentException.class, () -> matrix.copyRowInto(-1, new double[] {0.0d}));
    assertThrows(IllegalArgumentException.class, () -> matrix.copyRowInto(2, new double[] {0.0d}));
    assertThrows(IllegalArgumentException.class, () -> matrix.copyRowInto(0, new double[0]));
  }

  @Test
  void rejectsMultiplyIntoAliasedInputAndOutput() {
    CsrMatrix matrix =
        new CsrMatrix(2, 2, new double[] {1.0d, 1.0d}, new int[] {1, 0}, new int[] {0, 1, 2});
    double[] vector = {1.0d, 2.0d};

    assertThrows(IllegalArgumentException.class, () -> matrix.multiplyInto(vector, vector));
  }
}
