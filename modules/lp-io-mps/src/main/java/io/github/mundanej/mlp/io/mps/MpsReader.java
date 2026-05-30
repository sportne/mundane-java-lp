package io.github.mundanej.mlp.io.mps;

import io.github.mundanej.mlp.model.LpObjective;
import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpProblemStats;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.sparse.CsrMatrix;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Reader for the supported 0.1.0 MPS LP subset. */
public final class MpsReader {
    /**
     * Reads an MPS file and returns the canonical problem metadata.
     *
     * @param path MPS file path
     */
    public LpProblem read(final Path path) throws IOException {
        return readLp(path).problem();
    }

    /**
     * Reads an MPS file and returns metadata, matrix coefficients, and names.
     *
     * @param path MPS file path
     */
    public MpsLp readLp(final Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        Parser parser = new Parser();
        for (String line : Files.readAllLines(path)) {
            parser.accept(line);
        }
        return parser.finish();
    }

    private enum Section {
        NONE,
        ROWS,
        COLUMNS,
        RHS,
        BOUNDS,
        ENDATA
    }

    private static final class Parser {
        private String problemName;
        private String objectiveRowName;
        private Section section = Section.NONE;
        private boolean sawRows;
        private boolean sawColumns;
        private boolean sawRhs;
        private boolean sawEndata;
        private final LinkedHashMap<String, Character> rows = new LinkedHashMap<>();
        private final LinkedHashMap<String, ColumnData> columns = new LinkedHashMap<>();
        private final LinkedHashMap<String, Double> rhs = new LinkedHashMap<>();
        private final LinkedHashMap<String, BoundData> bounds = new LinkedHashMap<>();
        private String rhsSetName;
        private String boundSetName;

        void accept(final String line) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("*")) {
                return;
            }
            String[] tokens = trimmed.split("\\s+");
            if (sawEndata) {
                throw malformed("data after ENDATA");
            }
            switch (tokens[0]) {
                case "NAME" -> readName(tokens);
                case "ROWS" -> enterRows(tokens);
                case "COLUMNS" -> enterColumns(tokens);
                case "RHS" -> enterRhs(tokens);
                case "BOUNDS" -> enterBounds(tokens);
                case "ENDATA" -> enterEndata(tokens);
                case "RANGES", "SOS", "QMATRIX", "QSECTION", "QUADOBJ", "INDICATORS", "BASIS", "SOLUTION" ->
                        throw unsupported(tokens[0]);
                default -> readRecord(tokens);
            }
        }

        MpsLp finish() {
            if (!sawEndata) {
                throw malformed("missing ENDATA section");
            }
            if (problemName == null) {
                throw malformed("missing NAME section");
            }
            if (!sawRows) {
                throw malformed("missing ROWS section");
            }
            if (!sawColumns) {
                throw malformed("missing COLUMNS section");
            }
            if (!sawRhs) {
                throw malformed("missing RHS section");
            }
            if (objectiveRowName == null) {
                throw malformed("missing objective row");
            }
            List<String> rowNames = new ArrayList<>(rows.keySet());
            List<String> columnNames = new ArrayList<>(columns.keySet());
            List<LpRowBounds> rowBounds = rowNames.stream()
                    .map(this::rowBounds)
                    .toList();
            List<LpVariableBounds> variableBounds = columnNames.stream()
                    .map(this::variableBounds)
                    .toList();
            double[] objective = columnNames.stream()
                    .mapToDouble(column -> columns.get(column).objective)
                    .toArray();
            MatrixData matrix = matrix(rowNames, columnNames);
            LpProblem problem = new LpProblem(
                    problemName,
                    new LpObjective(ObjectiveSense.MINIMIZE, 0.0d, objective),
                    variableBounds,
                    rowBounds,
                    new LpProblemStats(rowNames.size(), columnNames.size(), matrix.values.length));
            return new MpsLp(
                    problem,
                    new CsrMatrix(rowNames.size(), columnNames.size(), matrix.values, matrix.columnIndices, matrix.rowPointers),
                    rowNames,
                    columnNames,
                    objectiveRowName);
        }

        private void readName(final String[] tokens) {
            if (problemName != null || section != Section.NONE) {
                throw malformed("NAME must be the first section");
            }
            if (tokens.length != 2) {
                throw malformed("NAME requires exactly one problem name");
            }
            problemName = tokens[1];
        }

        private void enterRows(final String[] tokens) {
            requireHeader(tokens, "ROWS");
            section = Section.ROWS;
            sawRows = true;
        }

        private void enterColumns(final String[] tokens) {
            requireHeader(tokens, "COLUMNS");
            section = Section.COLUMNS;
            sawColumns = true;
        }

        private void enterRhs(final String[] tokens) {
            requireHeader(tokens, "RHS");
            section = Section.RHS;
            sawRhs = true;
        }

        private void enterBounds(final String[] tokens) {
            requireHeader(tokens, "BOUNDS");
            section = Section.BOUNDS;
        }

        private void enterEndata(final String[] tokens) {
            requireHeader(tokens, "ENDATA");
            section = Section.ENDATA;
            sawEndata = true;
        }

        private static void requireHeader(final String[] tokens, final String name) {
            if (tokens.length != 1) {
                throw malformed(name + " section header must not contain data");
            }
        }

        private void readRecord(final String[] tokens) {
            switch (section) {
                case ROWS -> readRow(tokens);
                case COLUMNS -> readColumn(tokens);
                case RHS -> readRhsRecord(tokens);
                case BOUNDS -> readBound(tokens);
                default -> throw malformed("record outside supported section");
            }
        }

        private void readRow(final String[] tokens) {
            if (tokens.length != 2) {
                throw malformed("ROWS records require row type and row name");
            }
            char type = tokens[0].charAt(0);
            if (tokens[0].length() != 1 || "NELG".indexOf(type) < 0) {
                throw unsupported("row type " + tokens[0]);
            }
            if (type == 'N') {
                if (objectiveRowName != null) {
                    throw unsupported("multiple objective rows");
                }
                objectiveRowName = tokens[1];
                return;
            }
            if (rows.putIfAbsent(tokens[1], type) != null) {
                throw malformed("duplicate row " + tokens[1]);
            }
        }

        private void readColumn(final String[] tokens) {
            if (tokens.length < 3 || tokens.length > 5 || tokens.length % 2 == 0) {
                throw malformed("COLUMNS records require column and row/value pairs");
            }
            String columnName = tokens[0];
            if (containsMarker(tokens)) {
                throw unsupported("integer markers");
            }
            ColumnData column = columns.computeIfAbsent(columnName, ignored -> new ColumnData());
            readCoefficient(columnName, column, tokens[1], tokens[2]);
            if (tokens.length == 5) {
                readCoefficient(columnName, column, tokens[3], tokens[4]);
            }
        }

        private static boolean containsMarker(final String[] tokens) {
            for (String token : tokens) {
                if ("'MARKER'".equals(token) || "MARKER".equals(token)
                        || "'INTORG'".equals(token) || "'INTEND'".equals(token)) {
                    return true;
                }
            }
            return false;
        }

        private void readCoefficient(
                final String columnName,
                final ColumnData column,
                final String rowName,
                final String valueToken) {
            double value = parseDouble(valueToken);
            if (rowName.equals(objectiveRowName)) {
                column.objective += value;
                return;
            }
            if (!rows.containsKey(rowName)) {
                throw malformed("column " + columnName + " references undeclared row " + rowName);
            }
            column.coefficients.merge(rowName, value, Double::sum);
        }

        private void readRhsRecord(final String[] tokens) {
            if (tokens.length < 3 || tokens.length > 5 || tokens.length % 2 == 0) {
                throw malformed("RHS records require set and row/value pairs");
            }
            if (rhsSetName == null) {
                rhsSetName = tokens[0];
            } else if (!rhsSetName.equals(tokens[0])) {
                throw unsupported("multiple RHS sets");
            }
            readRhsValue(tokens[1], tokens[2]);
            if (tokens.length == 5) {
                readRhsValue(tokens[3], tokens[4]);
            }
        }

        private void readRhsValue(final String rowName, final String valueToken) {
            if (!rows.containsKey(rowName)) {
                throw malformed("RHS references undeclared row " + rowName);
            }
            rhs.put(rowName, parseDouble(valueToken));
        }

        private void readBound(final String[] tokens) {
            if (tokens.length != 3 && tokens.length != 4) {
                throw malformed("BOUNDS records require type, set, column, and optional value");
            }
            String type = tokens[0];
            if ("BV".equals(type) || "LI".equals(type) || "UI".equals(type) || "SC".equals(type)) {
                throw unsupported("integer or binary bounds");
            }
            if (boundSetName == null) {
                boundSetName = tokens[1];
            } else if (!boundSetName.equals(tokens[1])) {
                throw unsupported("multiple bound sets");
            }
            String columnName = tokens[2];
            if (!columns.containsKey(columnName)) {
                throw malformed("BOUNDS references undeclared column " + columnName);
            }
            BoundData bound = bounds.computeIfAbsent(columnName, ignored -> new BoundData());
            switch (type) {
                case "LO" -> bound.lower = parseRequiredBoundValue(tokens, type);
                case "UP" -> bound.upper = parseRequiredBoundValue(tokens, type);
                case "FX" -> {
                    double value = parseRequiredBoundValue(tokens, type);
                    bound.lower = value;
                    bound.upper = value;
                }
                case "FR" -> {
                    requireNoBoundValue(tokens, type);
                    bound.lower = Double.NEGATIVE_INFINITY;
                    bound.upper = Double.POSITIVE_INFINITY;
                }
                case "MI" -> {
                    requireNoBoundValue(tokens, type);
                    bound.lower = Double.NEGATIVE_INFINITY;
                }
                case "PL" -> {
                    requireNoBoundValue(tokens, type);
                    bound.upper = Double.POSITIVE_INFINITY;
                }
                default -> throw unsupported("bound type " + type);
            }
        }

        private static double parseRequiredBoundValue(final String[] tokens, final String type) {
            if (tokens.length != 4) {
                throw malformed(type + " bound requires a value");
            }
            return parseDouble(tokens[3]);
        }

        private static void requireNoBoundValue(final String[] tokens, final String type) {
            if (tokens.length != 3) {
                throw malformed(type + " bound must not carry a value");
            }
        }

        private LpRowBounds rowBounds(final String rowName) {
            double value = rhs.getOrDefault(rowName, 0.0d);
            return switch (rows.get(rowName)) {
                case 'E' -> new LpRowBounds(value, value);
                case 'L' -> new LpRowBounds(Double.NEGATIVE_INFINITY, value);
                case 'G' -> new LpRowBounds(value, Double.POSITIVE_INFINITY);
                default -> throw malformed("unsupported row type for " + rowName);
            };
        }

        private LpVariableBounds variableBounds(final String columnName) {
            BoundData bound = bounds.getOrDefault(columnName, new BoundData());
            return new LpVariableBounds(bound.lower, bound.upper);
        }

        private MatrixData matrix(final List<String> rowNames, final List<String> columnNames) {
            Map<String, Integer> columnIndex = new LinkedHashMap<>();
            for (int index = 0; index < columnNames.size(); index++) {
                columnIndex.put(columnNames.get(index), index);
            }
            List<Double> values = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            int[] rowPointers = new int[rowNames.size() + 1];
            for (int row = 0; row < rowNames.size(); row++) {
                String rowName = rowNames.get(row);
                for (String columnName : columnNames) {
                    Double value = columns.get(columnName).coefficients.get(rowName);
                    if (value != null && Double.compare(value, 0.0d) != 0) {
                        values.add(value);
                        indices.add(columnIndex.get(columnName));
                    }
                }
                rowPointers[row + 1] = values.size();
            }
            return new MatrixData(
                    values.stream().mapToDouble(Double::doubleValue).toArray(),
                    indices.stream().mapToInt(Integer::intValue).toArray(),
                    rowPointers);
        }

        private static double parseDouble(final String token) {
            try {
                double value = Double.parseDouble(token);
                if (!Double.isFinite(value)) {
                    throw malformed("non-finite numeric value " + token);
                }
                return value;
            } catch (NumberFormatException exception) {
                throw malformed("invalid numeric value " + token);
            }
        }
    }

    private static MpsFormatException unsupported(final String feature) {
        return new MpsFormatException("Unsupported MPS feature: " + feature);
    }

    private static MpsFormatException malformed(final String reason) {
        return new MpsFormatException("Malformed MPS: " + reason);
    }

    private static final class ColumnData {
        private double objective;
        private final LinkedHashMap<String, Double> coefficients = new LinkedHashMap<>();
    }

    private static final class BoundData {
        private double lower;
        private double upper = Double.POSITIVE_INFINITY;
    }

    private record MatrixData(double[] values, int[] columnIndices, int[] rowPointers) {
    }
}
