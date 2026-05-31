package io.github.mundanej.mlp.examples.solvercomparison;

import io.github.mundanej.mlp.adapter.clp.ClpCliAdapter;
import io.github.mundanej.mlp.adapter.glpk.GlpkCliAdapter;
import io.github.mundanej.mlp.adapter.highs.HighsCliAdapter;
import io.github.mundanej.mlp.adapter.ojalgo.OjAlgoAdapter;
import io.github.mundanej.mlp.adapter.ortools.OrToolsJavaAdapter;
import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.harness.BenchmarkInstance;
import io.github.mundanej.mlp.harness.BenchmarkSuite;
import io.github.mundanej.mlp.harness.HarnessRunConfig;
import io.github.mundanej.mlp.harness.HarnessRunner;
import io.github.mundanej.mlp.harness.RunOutcome;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.harness.report.CsvReportWriter;
import io.github.mundanej.mlp.harness.report.JsonReportWriter;
import io.github.mundanej.mlp.harness.report.MarkdownReportWriter;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.testkit.LpTestInstances;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/** Runs a tiny solver comparison smoke suite. */
public final class SolverComparisonSmokeMain {
    static final String DEFAULT_OUTPUT_DIRECTORY = "build/reports/solver-comparison-smoke";

    private SolverComparisonSmokeMain() {
    }

    /** Runs the solver comparison smoke suite. */
    public static void main(final String[] args) throws IOException {
        Path outputDirectory = args.length == 0 ? Path.of(DEFAULT_OUTPUT_DIRECTORY) : Path.of(args[0]);
        SmokeResult result = run(outputDirectory, defaultAdapters());
        System.out.println("solver comparison smoke");
        System.out.println("suite=" + result.suiteId());
        System.out.println("records=" + result.records().size());
        System.out.println("successful=" + result.successfulSolvers());
        System.out.println("failed=" + result.failedSolvers());
        System.out.println("unavailable=" + result.unavailableSolvers());
        System.out.println("markdown=" + result.markdownPath());
        System.out.println("json=" + result.jsonPath());
        System.out.println("csv=" + result.csvPath());
        result.throwIfFailed();
    }

    static SmokeResult run(
            final Path outputDirectory,
            final List<Supplier<LpSolverAdapter>> adapterSuppliers) throws IOException {
        Files.createDirectories(outputDirectory);
        BenchmarkSuite suite = suite();
        List<LpSolverAdapter> adapters = adapterSuppliers.stream().map(Supplier::get).toList();
        List<RunRecord> records = new HarnessRunner().run(
                suite,
                adapters,
                new HarnessRunConfig(
                        outputDirectory.resolve("work"),
                        SolverOptions.defaults(),
                        ToleranceProfile.STANDARD));
        Path markdownPath = outputDirectory.resolve("report.md");
        Path jsonPath = outputDirectory.resolve("report.json");
        Path csvPath = outputDirectory.resolve("report.csv");
        Files.writeString(markdownPath, new MarkdownReportWriter().render(records));
        Files.writeString(jsonPath, new JsonReportWriter().render(records));
        Files.writeString(csvPath, new CsvReportWriter().render(records));
        return new SmokeResult(
                suite.id(),
                records,
                markdownPath,
                jsonPath,
                csvPath,
                successfulSolvers(records),
                failedSolvers(records),
                unavailableSolvers(records));
    }

    static List<Supplier<LpSolverAdapter>> defaultAdapters() {
        return List.of(
                HighsCliAdapter::new,
                ClpCliAdapter::new,
                GlpkCliAdapter::new,
                OrToolsJavaAdapter::new,
                OjAlgoAdapter::new);
    }

    private static BenchmarkSuite suite() {
        CanonicalLpFixture fixture = LpTestInstances.tierOneFixture("single-bounded-variable");
        return new BenchmarkSuite("solver-comparison-smoke", List.of(new BenchmarkInstance(
                fixture.problem().name(),
                fixture.problem(),
                fixture.matrix(),
                LpTestInstances.expectedValidationResult(fixture))));
    }

    private static long successfulSolvers(final List<RunRecord> records) {
        return records.stream()
                .filter(record -> record.outcome() == RunOutcome.SUCCESS)
                .map(record -> record.solverResult().solverId())
                .distinct()
                .count();
    }

    private static long failedSolvers(final List<RunRecord> records) {
        return records.stream()
                .filter(record -> record.outcome() == RunOutcome.ADAPTER_ERROR
                        || record.outcome() == RunOutcome.VALIDATION_FAILED)
                .map(record -> record.solverResult().solverId())
                .distinct()
                .count();
    }

    private static long unavailableSolvers(final List<RunRecord> records) {
        return records.stream()
                .filter(record -> record.outcome() == RunOutcome.SOLVER_UNAVAILABLE)
                .map(record -> record.solverResult().solverId())
                .distinct()
                .count();
    }

    /**
     * Solver comparison smoke result paths and counters.
     *
     * @param suiteId suite identifier
     * @param records run records in report order
     * @param markdownPath Markdown report path
     * @param jsonPath JSON report path
     * @param csvPath CSV report path
     * @param successfulSolvers count of solvers with accepted evidence
     * @param failedSolvers count of available solvers with errors or validation failures
     * @param unavailableSolvers count of unavailable solvers
     */
    public record SmokeResult(
            String suiteId,
            List<RunRecord> records,
            Path markdownPath,
            Path jsonPath,
            Path csvPath,
            long successfulSolvers,
            long failedSolvers,
            long unavailableSolvers) {
        /**
         * Creates a smoke result.
         *
         * @param suiteId suite identifier
         * @param records run records in report order
         * @param markdownPath Markdown report path
         * @param jsonPath JSON report path
         * @param csvPath CSV report path
         * @param successfulSolvers count of solvers with accepted evidence
         * @param failedSolvers count of available solvers with errors or validation failures
         * @param unavailableSolvers count of unavailable solvers
         */
        public SmokeResult {
            records = List.copyOf(records);
        }

        /** Throws when an available solver produced an error or validation failure. */
        public void throwIfFailed() {
            if (failedSolvers > 0) {
                throw new IllegalStateException("solver comparison smoke failed for "
                        + failedSolvers + " solver(s); reports are available at " + markdownPath);
            }
        }
    }
}
