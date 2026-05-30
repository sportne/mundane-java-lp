package io.github.mundanej.mlp.io.mps;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.generators.CanonicalLpFixtures;
import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class MpsRoundTripTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsSupportedTierOneFixtures() throws Exception {
        List<CanonicalLpFixture> supported = CanonicalLpFixtures.tierOne().stream()
                .filter(MpsRoundTripTest::isSupportedMpsFixture)
                .toList();
        assertEquals(6, supported.size());
        for (CanonicalLpFixture fixture : supported) {
            Path path = tempDir.resolve(fixture.problem().name() + ".mps");
            MpsLp original = fromFixture(fixture);

            new MpsWriter().write(original, path);
            MpsLp actual = new MpsReader().readLp(path);

            assertEquivalent(original, actual);
        }
    }

    @Test
    void readReturnsProblemMetadataForCompatibility() throws Exception {
        Path path = tempDir.resolve("tiny.mps");
        Files.writeString(path, """
                NAME TINY
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                ENDATA
                """);
        assertEquals("TINY", new MpsReader().read(path).name());
    }

    @Test
    void rejectsMissingRequiredSections() throws Exception {
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                RHS
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                """);
    }

    @Test
    void rejectsUnsupportedSectionsAndFeatures() throws Exception {
        assertUnsupported("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                RANGES
                 RNG row 1.0
                ENDATA
                """);
        assertUnsupported("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 MARK0000 'MARKER' 'INTORG'
                RHS
                ENDATA
                """);
        assertUnsupported("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                BOUNDS
                 BV BND1 x
                ENDATA
                """);
    }

    @Test
    void rejectsMalformedReferences() throws Exception {
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x missing 1.0
                RHS
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                 RHS1 missing 1.0
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                BOUNDS
                 LO BND1 y 0.0
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                ENDATA
                RHS
                """);
    }

    @Test
    void writerRejectsUnsupportedCanonicalFixtures() {
        assertThrows(MpsFormatException.class, () -> new MpsWriter().write(
                fromFixture(CanonicalLpFixtures.twoVariableFeasibleOptimum()),
                tempDir.resolve("max.mps")));
        assertThrows(MpsFormatException.class, () -> new MpsWriter().write(
                fromFixture(CanonicalLpFixtures.freeVariable()),
                tempDir.resolve("ranged.mps")));
        assertThrows(MpsFormatException.class, () -> new MpsWriter().write(
                problemWithObjectiveConstant(),
                tempDir.resolve("constant.mps")));
    }

    @Test
    void roundTripsZeroOnlyColumn() throws Exception {
        MpsLp original = new MpsLp(
                new LpProblem(
                        "zero-only-column",
                        new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {0.0d}),
                        List.of(new LpVariableBounds(0.0d, 1.0d)),
                        List.of(),
                        new LpProblemStats(0, 1, 0)),
                new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}),
                List.of(),
                List.of("x"),
                "OBJ");
        Path path = tempDir.resolve("zero-only-column.mps");

        new MpsWriter().write(original, path);
        MpsLp actual = new MpsReader().readLp(path);

        assertEquivalent(original, actual);
    }

    private static boolean isSupportedMpsFixture(final CanonicalLpFixture fixture) {
        return fixture.problem().objective().sense() == ObjectiveSense.MINIMIZE
                && fixture.problem().rowBounds().stream().noneMatch(MpsRoundTripTest::isRanged);
    }

    private static boolean isRanged(final LpRowBounds bounds) {
        return Double.isFinite(bounds.lower())
                && Double.isFinite(bounds.upper())
                && Double.compare(bounds.lower(), bounds.upper()) != 0;
    }

    private static MpsLp fromFixture(final CanonicalLpFixture fixture) {
        return new MpsLp(fixture.problem(), fixture.matrix(), fixture.rowNames(), fixture.columnNames(), "OBJ");
    }

    private static MpsLp problemWithObjectiveConstant() {
        return new MpsLp(
                new LpProblem(
                        "constant",
                        new LpObjective(ObjectiveSense.MINIMIZE, 1.0d, new double[] {1.0d}),
                        List.of(new LpVariableBounds(0.0d, Double.POSITIVE_INFINITY)),
                        List.of(),
                        new LpProblemStats(0, 1, 0)),
                new CsrMatrix(0, 1, new double[0], new int[0], new int[] {0}),
                List.of(),
                List.of("x"),
                "OBJ");
    }

    private static void assertEquivalent(final MpsLp expected, final MpsLp actual) {
        assertEquals(expected.problem().name(), actual.problem().name());
        assertEquals(expected.problem().objective().sense(), actual.problem().objective().sense());
        assertArrayEquals(expected.problem().objective().coefficients(), actual.problem().objective().coefficients());
        assertEquals(expected.problem().variableBounds(), actual.problem().variableBounds());
        assertEquals(expected.problem().rowBounds(), actual.problem().rowBounds());
        assertEquals(expected.rowNames(), actual.rowNames());
        assertEquals(expected.columnNames(), actual.columnNames());
        assertEquals(expected.objectiveRowName(), actual.objectiveRowName());
        assertArrayEquals(expected.matrix().values(), actual.matrix().values());
        assertArrayEquals(expected.matrix().columnIndices(), actual.matrix().columnIndices());
        assertArrayEquals(expected.matrix().rowPointers(), actual.matrix().rowPointers());
    }

    private void assertMalformed(final String content) throws Exception {
        Path path = tempDir.resolve("bad-" + System.nanoTime() + ".mps");
        Files.writeString(path, content);
        MpsFormatException exception = assertThrows(MpsFormatException.class, () -> new MpsReader().readLp(path));
        assertEquals(true, exception.getMessage().startsWith("Malformed MPS:"));
    }

    private void assertUnsupported(final String content) throws Exception {
        Path path = tempDir.resolve("bad-" + System.nanoTime() + ".mps");
        Files.writeString(path, content);
        MpsFormatException exception = assertThrows(MpsFormatException.class, () -> new MpsReader().readLp(path));
        assertEquals(true, exception.getMessage().startsWith("Unsupported MPS feature:"));
    }
}
