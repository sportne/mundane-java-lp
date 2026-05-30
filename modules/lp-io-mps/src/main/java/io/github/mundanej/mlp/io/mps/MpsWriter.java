package io.github.mundanej.mlp.io.mps;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Writer for the supported 0.1.0 MPS LP subset. */
public final class MpsWriter {
    /**
     * Writes an LP problem shell for compatibility with G0 smoke callers.
     *
     * @param problem LP problem to describe
     * @param path output path
     */
    public void write(final LpProblem problem, final Path path) throws IOException {
        Objects.requireNonNull(problem, "problem");
        CsrMatrix matrix = new CsrMatrix(
                problem.stats().rows(),
                problem.stats().columns(),
                new double[0],
                new int[0],
                zeroPointers(problem.stats()));
        write(new MpsLp(problem, matrix, generatedNames("R", problem.stats().rows()),
                generatedNames("X", problem.stats().columns()), "OBJ"), path);
    }

    /**
     * Writes an MPS LP envelope.
     *
     * @param lp LP envelope
     * @param path output path
     */
    public void write(final MpsLp lp, final Path path) throws IOException {
        Objects.requireNonNull(lp, "lp");
        Objects.requireNonNull(path, "path");
        validateSupported(lp);
        StringBuilder out = new StringBuilder();
        out.append("NAME ").append(lp.problem().name()).append(System.lineSeparator());
        writeRows(lp, out);
        writeColumns(lp, out);
        writeRhs(lp, out);
        writeBounds(lp, out);
        out.append("ENDATA").append(System.lineSeparator());
        Files.writeString(path, out.toString());
    }

    private static int[] zeroPointers(final LpProblemStats stats) {
        return new int[stats.rows() + 1];
    }

    private static List<String> generatedNames(final String prefix, final int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> prefix + index)
                .toList();
    }

    private static void validateSupported(final MpsLp lp) {
        if (lp.problem().objective().sense() != ObjectiveSense.MINIMIZE) {
            throw new MpsFormatException("Unsupported MPS feature: maximization objective");
        }
        if (Double.compare(lp.problem().objective().constant(), 0.0d) != 0) {
            throw new MpsFormatException("Unsupported MPS feature: objective constants");
        }
        for (LpRowBounds rowBounds : lp.problem().rowBounds()) {
            if (Double.isFinite(rowBounds.lower())
                    && Double.isFinite(rowBounds.upper())
                    && Double.compare(rowBounds.lower(), rowBounds.upper()) != 0) {
                throw new MpsFormatException("Unsupported MPS feature: ranged rows");
            }
        }
    }

    private static void writeRows(final MpsLp lp, final StringBuilder out) {
        out.append("ROWS").append(System.lineSeparator());
        out.append(" N ").append(lp.objectiveRowName()).append(System.lineSeparator());
        for (int index = 0; index < lp.rowNames().size(); index++) {
            out.append(' ').append(rowType(lp.problem().rowBounds().get(index)))
                    .append(' ').append(lp.rowNames().get(index))
                    .append(System.lineSeparator());
        }
    }

    private static char rowType(final LpRowBounds rowBounds) {
        if (Double.compare(rowBounds.lower(), rowBounds.upper()) == 0) {
            return 'E';
        }
        if (Double.isInfinite(rowBounds.lower()) && rowBounds.lower() < 0.0d) {
            return 'L';
        }
        if (Double.isInfinite(rowBounds.upper()) && rowBounds.upper() > 0.0d) {
            return 'G';
        }
        throw new MpsFormatException("Unsupported MPS feature: ranged rows");
    }

    private static void writeColumns(final MpsLp lp, final StringBuilder out) {
        out.append("COLUMNS").append(System.lineSeparator());
        double[] objective = lp.problem().objective().coefficients();
        double[] values = lp.matrix().values();
        int[] indices = lp.matrix().columnIndices();
        int[] pointers = lp.matrix().rowPointers();
        for (int column = 0; column < lp.columnNames().size(); column++) {
            String columnName = lp.columnNames().get(column);
            boolean wroteColumn = false;
            if (Double.compare(objective[column], 0.0d) != 0) {
                writePair(out, columnName, lp.objectiveRowName(), objective[column]);
                wroteColumn = true;
            }
            for (int row = 0; row < lp.rowNames().size(); row++) {
                for (int offset = pointers[row]; offset < pointers[row + 1]; offset++) {
                    if (indices[offset] == column) {
                        writePair(out, columnName, lp.rowNames().get(row), values[offset]);
                        wroteColumn = true;
                    }
                }
            }
            if (!wroteColumn) {
                writePair(out, columnName, lp.objectiveRowName(), 0.0d);
            }
        }
    }

    private static void writeRhs(final MpsLp lp, final StringBuilder out) {
        out.append("RHS").append(System.lineSeparator());
        for (int index = 0; index < lp.rowNames().size(); index++) {
            LpRowBounds bounds = lp.problem().rowBounds().get(index);
            double rhs = rhsValue(bounds);
            if (Double.compare(rhs, 0.0d) != 0) {
                out.append(" RHS1 ").append(lp.rowNames().get(index)).append(' ').append(rhs)
                        .append(System.lineSeparator());
            }
        }
    }

    private static double rhsValue(final LpRowBounds bounds) {
        if (Double.compare(bounds.lower(), bounds.upper()) == 0) {
            return bounds.lower();
        }
        if (Double.isFinite(bounds.upper())) {
            return bounds.upper();
        }
        if (Double.isFinite(bounds.lower())) {
            return bounds.lower();
        }
        return 0.0d;
    }

    private static void writeBounds(final MpsLp lp, final StringBuilder out) {
        out.append("BOUNDS").append(System.lineSeparator());
        for (int index = 0; index < lp.columnNames().size(); index++) {
            double lower = lp.problem().variableBounds().get(index).lower();
            double upper = lp.problem().variableBounds().get(index).upper();
            String columnName = lp.columnNames().get(index);
            if (Double.isInfinite(lower) && lower < 0.0d && Double.isInfinite(upper) && upper > 0.0d) {
                out.append(" FR BND1 ").append(columnName).append(System.lineSeparator());
            } else if (Double.compare(lower, upper) == 0) {
                out.append(" FX BND1 ").append(columnName).append(' ').append(lower).append(System.lineSeparator());
            } else {
                if (Double.compare(lower, 0.0d) != 0) {
                    out.append(" LO BND1 ").append(columnName).append(' ').append(lower).append(System.lineSeparator());
                }
                if (Double.isFinite(upper)) {
                    out.append(" UP BND1 ").append(columnName).append(' ').append(upper).append(System.lineSeparator());
                }
                if (Double.isInfinite(lower) && lower < 0.0d && Double.isInfinite(upper) && upper > 0.0d) {
                    out.append(" FR BND1 ").append(columnName).append(System.lineSeparator());
                } else if (Double.isInfinite(lower) && lower < 0.0d) {
                    out.append(" MI BND1 ").append(columnName).append(System.lineSeparator());
                }
            }
        }
    }

    private static void writePair(
            final StringBuilder out,
            final String columnName,
            final String rowName,
            final double value) {
        out.append(' ').append(columnName).append(' ').append(rowName).append(' ').append(value)
                .append(System.lineSeparator());
    }
}
