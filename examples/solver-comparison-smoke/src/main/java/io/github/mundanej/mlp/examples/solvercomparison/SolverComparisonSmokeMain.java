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
import io.github.mundanej.mlp.solver.performance.PerformanceLpSolverAdapter;
import io.github.mundanej.mlp.solver.simple.SimpleLpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.testkit.LpTestInstances;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Runs a tiny solver comparison smoke suite. */
public final class SolverComparisonSmokeMain {
  static final String DEFAULT_OUTPUT_DIRECTORY = "build/reports/solver-comparison-smoke";
  static final String STRICT_OUTPUT_DIRECTORY = "build/reports/strict-solver-comparison";
  private static final Set<String> SMOKE_REQUIRED_SOLVER_NAMES = Set.of("simple", "performance");
  private static final Set<String> STRICT_REQUIRED_SOLVER_NAMES =
      Set.of("highs", "clp", "glpk", "ortools", "ojalgo", "simple", "performance");

  private SolverComparisonSmokeMain() {}

  /** Runs the solver comparison smoke suite. */
  public static void main(final String[] args) throws IOException {
    CliArguments arguments = CliArguments.parse(args);
    SmokeResult result = run(arguments.outputDirectory(), defaultAdapters(), arguments.mode());
    printSummary(result);
    result.throwIfFailed();
  }

  static void printSummary(final SmokeResult result) {
    System.out.println(result.mode().title());
    System.out.println("mode=" + result.mode().reportValue());
    System.out.println("suite=" + result.suiteId());
    System.out.println("records=" + result.records().size());
    System.out.println("successful=" + result.successfulSolvers());
    System.out.println("failed=" + result.failedSolvers());
    System.out.println("unavailable=" + result.unavailableSolvers());
    System.out.println("markdown=" + result.markdownPath());
    System.out.println("json=" + result.jsonPath());
    System.out.println("csv=" + result.csvPath());
  }

  static SmokeResult run(
      final Path outputDirectory, final List<Supplier<LpSolverAdapter>> adapterSuppliers)
      throws IOException {
    return run(outputDirectory, adapterSuppliers, ComparisonMode.SMOKE);
  }

  static SmokeResult run(
      final Path outputDirectory,
      final List<Supplier<LpSolverAdapter>> adapterSuppliers,
      final ComparisonMode mode)
      throws IOException {
    Files.createDirectories(outputDirectory);
    BenchmarkSuite suite = suite();
    List<LpSolverAdapter> adapters = adapterSuppliers.stream().map(Supplier::get).toList();
    List<RunRecord> records =
        new HarnessRunner()
            .run(
                suite,
                adapters,
                new HarnessRunConfig(
                    outputDirectory.resolve("work"),
                    SolverOptions.defaults(),
                    ToleranceProfile.STANDARD));
    records = addReportMetadata(records, mode);
    Path markdownPath = outputDirectory.resolve("report.md");
    Path jsonPath = outputDirectory.resolve("report.json");
    Path csvPath = outputDirectory.resolve("report.csv");
    Files.writeString(markdownPath, new MarkdownReportWriter().render(records));
    Files.writeString(jsonPath, new JsonReportWriter().render(records));
    Files.writeString(csvPath, new CsvReportWriter().render(records));
    return new SmokeResult(
        mode,
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
        OjAlgoAdapter::new,
        SimpleLpSolverAdapter::new,
        PerformanceLpSolverAdapter::new);
  }

  private static BenchmarkSuite suite() {
    CanonicalLpFixture fixture = LpTestInstances.tierOneFixture("single-bounded-variable");
    return new BenchmarkSuite(
        "solver-comparison-smoke",
        List.of(
            new BenchmarkInstance(
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
        .filter(
            record ->
                record.outcome() == RunOutcome.ADAPTER_ERROR
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

  private static List<RunRecord> addReportMetadata(
      final List<RunRecord> records, final ComparisonMode mode) {
    Map<String, SolverMetadata> metadata =
        Map.of(
            "highs", commandMetadata("highs", List.of("--version")),
            "clp", commandMetadata("clp", List.of("-stop")),
            "glpk", commandMetadata("glpsol", List.of("--version")),
            "ortools", new SolverMetadata("9.15.6755", "in-process Gradle dependency"),
            "ojalgo", new SolverMetadata("56.2.1", "in-process Gradle dependency"),
            "simple", new SolverMetadata("project", "in-project"),
            "performance", new SolverMetadata("project", "in-project"));
    return records.stream()
        .map(
            record -> {
              SolverMetadata solverMetadata =
                  metadata.getOrDefault(
                      record.solverResult().solverId().name(),
                      new SolverMetadata("not-measured", "not-measured"));
              return record.withReportMetadata(
                  mode.reportValue(), solverMetadata.version(), solverMetadata.binaryPath());
            })
        .toList();
  }

  static SolverMetadata commandMetadata(
      final String commandName, final List<String> versionArguments) {
    Path commandPath = findCommand(commandName);
    if (commandPath == null) {
      return new SolverMetadata("unavailable", "unavailable: command not found");
    }
    String version = firstVersionLine(commandPath, versionArguments);
    return new SolverMetadata(version, commandPath.toString());
  }

  private static Path findCommand(final String commandName) {
    String pathValue = System.getenv("PATH");
    if (pathValue == null || pathValue.isBlank()) {
      return null;
    }
    for (String directory : pathValue.split(java.io.File.pathSeparator)) {
      if (directory.isBlank()) {
        continue;
      }
      Path candidate = Path.of(directory).resolve(commandName);
      if (Files.isExecutable(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  private static String firstVersionLine(
      final Path commandPath, final List<String> versionArguments) {
    List<String> command = new ArrayList<>();
    command.add(commandPath.toString());
    command.addAll(versionArguments);
    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      boolean finished = process.waitFor(5, TimeUnit.SECONDS);
      if (!finished) {
        process.destroyForcibly();
        process.waitFor(1, TimeUnit.SECONDS);
        return "unavailable: version command timed out";
      }
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      if (process.exitValue() != 0) {
        return "unavailable: version command failed";
      }
      return output.lines().filter(line -> !line.isBlank()).findFirst().orElse("unavailable");
    } catch (IOException exception) {
      return "unavailable: version command failed";
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return "unavailable: version command interrupted";
    }
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
      ComparisonMode mode,
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
        throw new IllegalStateException(
            mode.reportValue()
                + " solver comparison failed for "
                + failedSolvers
                + " solver(s); reports are available at "
                + markdownPath);
      }
      long unavailableRequiredSolvers = unavailableRequiredSolvers();
      if (unavailableRequiredSolvers > 0) {
        throw new IllegalStateException(
            mode.reportValue()
                + " solver comparison missing "
                + unavailableRequiredSolvers
                + " required solver(s); reports are available at "
                + markdownPath);
      }
    }

    private long unavailableRequiredSolvers() {
      Set<String> presentRequiredNames = new HashSet<>();
      Set<String> unavailableRequiredNames = new HashSet<>();
      for (RunRecord record : records) {
        String solverName = record.solverResult().solverId().name();
        if (!mode.requiredSolverNames().contains(solverName)) {
          continue;
        }
        presentRequiredNames.add(solverName);
        if (record.outcome() == RunOutcome.SOLVER_UNAVAILABLE) {
          unavailableRequiredNames.add(solverName);
        }
      }
      long absentRequiredNames =
          mode.requiredSolverNames().stream()
              .filter(name -> !presentRequiredNames.contains(name))
              .count();
      return unavailableRequiredNames.size() + absentRequiredNames;
    }
  }

  enum ComparisonMode {
    SMOKE("smoke", "solver comparison smoke", SMOKE_REQUIRED_SOLVER_NAMES),
    STRICT("strict", "strict solver comparison", STRICT_REQUIRED_SOLVER_NAMES);

    private final String reportValue;
    private final String title;
    private final Set<String> requiredSolverNames;

    ComparisonMode(
        final String reportValue, final String title, final Set<String> requiredSolverNames) {
      this.reportValue = reportValue;
      this.title = title;
      this.requiredSolverNames = Set.copyOf(requiredSolverNames);
    }

    String reportValue() {
      return reportValue;
    }

    String title() {
      return title;
    }

    Set<String> requiredSolverNames() {
      return requiredSolverNames;
    }
  }

  record SolverMetadata(String version, String binaryPath) {}

  record CliArguments(ComparisonMode mode, Path outputDirectory) {
    static CliArguments parse(final String[] args) {
      ComparisonMode mode = ComparisonMode.SMOKE;
      Path outputDirectory = null;
      for (int index = 0; index < args.length; index++) {
        String argument = args[index];
        if ("--strict".equals(argument)) {
          mode = ComparisonMode.STRICT;
        } else if ("--output-dir".equals(argument)) {
          if (index + 1 >= args.length) {
            throw new IllegalArgumentException("--output-dir requires a path argument");
          }
          outputDirectory = Path.of(args[++index]);
        } else if (argument.startsWith("--")) {
          throw new IllegalArgumentException("unknown argument: " + argument);
        } else {
          outputDirectory = Path.of(argument);
        }
      }
      if (outputDirectory == null) {
        outputDirectory =
            mode == ComparisonMode.STRICT
                ? Path.of(STRICT_OUTPUT_DIRECTORY)
                : Path.of(DEFAULT_OUTPUT_DIRECTORY);
      }
      return new CliArguments(mode, outputDirectory);
    }
  }
}
