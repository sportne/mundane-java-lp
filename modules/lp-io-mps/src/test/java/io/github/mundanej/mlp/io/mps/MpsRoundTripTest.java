package io.github.mundanej.mlp.io.mps;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import io.github.mundanej.mlp.testkit.LpTestInstances;
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
        List<CanonicalLpFixture> supported = LpTestInstances.tierOneFixtures().stream()
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
    void writerSupportsCompatibilityProblemShell() throws Exception {
        Path path = tempDir.resolve("shell.mps");

        new MpsWriter().write(LpTestInstances.singleBoundedVariable(), path);

        String content = Files.readString(path);
        assertTrue(content.contains("NAME single-bounded-variable"));
        assertEquals("single-bounded-variable", new MpsReader().read(path).name());
    }

    @Test
    void envelopeValidatesShapeNamesAndCopies() {
        MpsLp lp = fromFixture(LpTestInstances.tierOneFixture("single-bounded-variable"));

        assertThrows(UnsupportedOperationException.class, () -> lp.columnNames().add("other"));
        assertThrows(NullPointerException.class, () -> new MpsLp(
                null,
                lp.matrix(),
                lp.rowNames(),
                lp.columnNames(),
                lp.objectiveRowName()));
        assertThrows(NullPointerException.class, () -> new MpsLp(
                lp.problem(),
                null,
                lp.rowNames(),
                lp.columnNames(),
                lp.objectiveRowName()));
        assertThrows(NullPointerException.class, () -> new MpsLp(
                lp.problem(),
                lp.matrix(),
                null,
                lp.columnNames(),
                lp.objectiveRowName()));
        assertThrows(NullPointerException.class, () -> new MpsLp(
                lp.problem(),
                lp.matrix(),
                lp.rowNames(),
                null,
                lp.objectiveRowName()));
        assertThrows(NullPointerException.class, () -> new MpsLp(
                lp.problem(),
                lp.matrix(),
                lp.rowNames(),
                lp.columnNames(),
                null));
        assertThrows(IllegalArgumentException.class, () -> new MpsLp(
                lp.problem(),
                lp.matrix(),
                lp.rowNames(),
                lp.columnNames(),
                " "));
        assertThrows(IllegalArgumentException.class, () -> new MpsLp(
                lp.problem(),
                lp.matrix(),
                lp.rowNames(),
                List.of(" "),
                lp.objectiveRowName()));
        assertThrows(IllegalArgumentException.class, () -> new MpsLp(
                twoColumnProblem(),
                new CsrMatrix(0, 2, new double[0], new int[0], new int[] {0}),
                List.of(),
                List.of("x", "x"),
                "OBJ"));
    }

    @Test
    void rejectsMissingRequiredSections() throws Exception {
        assertMalformed("""
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                COLUMNS
                 x OBJ 1.0
                RHS
                ENDATA
                """);
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
        assertMalformed("""
                NAME BAD
                ROWS
                 L row
                COLUMNS
                 x row 1.0
                RHS
                ENDATA
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
        assertUnsupported("""
                NAME BAD
                ROWS
                 X row
                COLUMNS
                 x row 1.0
                RHS
                ENDATA
                """);
        assertUnsupported("""
                NAME BAD
                ROWS
                 N OBJ
                 N ALT
                COLUMNS
                 x OBJ 1.0
                RHS
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
        assertMalformed("""
                NAME BAD
                ROWS data
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ nope
                RHS
                ENDATA
                """);
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ NaN
                RHS
                ENDATA
                """);
    }

    @Test
    void writerRejectsUnsupportedCanonicalFixtures() {
        assertThrows(MpsFormatException.class, () -> new MpsWriter().write(
                fromFixture(LpTestInstances.tierOneFixture("two-variable-feasible-optimum")),
                tempDir.resolve("max.mps")));
        assertThrows(MpsFormatException.class, () -> new MpsWriter().write(
                fromFixture(LpTestInstances.tierOneFixture("free-variable-row-bounded")),
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

    @Test
    void roundTripsRowAndBoundVariants() throws Exception {
        MpsLp original = new MpsLp(
                new LpProblem(
                        "variants",
                        new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d, 0.0d, 0.0d, 0.0d}),
                        List.of(
                                LpVariableBounds.FREE,
                                new LpVariableBounds(3.0d, 3.0d),
                                new LpVariableBounds(2.0d, 5.0d),
                                new LpVariableBounds(Double.NEGATIVE_INFINITY, 4.0d)),
                        List.of(
                                new LpRowBounds(2.0d, Double.POSITIVE_INFINITY),
                                new LpRowBounds(Double.NEGATIVE_INFINITY, 4.0d)),
                        new LpProblemStats(2, 4, 0)),
                new CsrMatrix(2, 4, new double[0], new int[0], new int[] {0, 0, 0}),
                List.of("lower", "upper"),
                List.of("free", "fixed", "boxed", "minus"),
                "OBJ");
        Path path = tempDir.resolve("variants.mps");

        new MpsWriter().write(original, path);
        MpsLp actual = new MpsReader().readLp(path);

        assertEquivalent(original, actual);
    }

    @Test
    void rejectsMalformedBoundAndSetRecords() throws Exception {
        assertMalformed("""
                NAME BAD
                ROWS
                 N OBJ
                COLUMNS
                 x OBJ 1.0
                RHS
                 RHS1 OBJ 0.0
                ENDATA
                """);
        assertUnsupported("""
                NAME BAD
                ROWS
                 N OBJ
                 L row
                COLUMNS
                 x OBJ 1.0
                RHS
                 RHS1 row 1.0
                 RHS2 row 2.0
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
                 LO B1 x 0.0
                 UP B2 x 1.0
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
                 LO B1 x
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
                 FR B1 x 0.0
                ENDATA
                """);
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

    private static LpProblem twoColumnProblem() {
        return new LpProblem(
                "two-column",
                new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, new double[] {1.0d, 2.0d}),
                List.of(
                        new LpVariableBounds(0.0d, 1.0d),
                        new LpVariableBounds(0.0d, 1.0d)),
                List.of(),
                new LpProblemStats(0, 2, 0));
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
