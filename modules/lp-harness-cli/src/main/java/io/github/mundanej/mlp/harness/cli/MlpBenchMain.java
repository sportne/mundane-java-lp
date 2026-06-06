package io.github.mundanej.mlp.harness.cli;

import io.github.mundanej.mlp.generators.BenchmarkFixtureGenerator;
import io.github.mundanej.mlp.generators.ExpectedResultKind;
import io.github.mundanej.mlp.generators.GeneratedLpInstance;
import io.github.mundanej.mlp.generators.NetworkFlowGenerator;
import io.github.mundanej.mlp.harness.BenchmarkInstance;
import io.github.mundanej.mlp.harness.BenchmarkSuite;
import io.github.mundanej.mlp.harness.HarnessRunConfig;
import io.github.mundanej.mlp.harness.HarnessRunner;
import io.github.mundanej.mlp.harness.MachineFingerprint;
import io.github.mundanej.mlp.harness.RunOutcome;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.harness.report.CsvReportWriter;
import io.github.mundanej.mlp.harness.report.JsonReportWriter;
import io.github.mundanej.mlp.harness.report.MarkdownReportWriter;
import io.github.mundanej.mlp.io.mps.MpsLp;
import io.github.mundanej.mlp.io.mps.MpsReader;
import io.github.mundanej.mlp.solver.performance.PerformanceLpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationFinding;
import io.github.mundanej.mlp.validation.ValidationReport;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Command-line entrypoint for the LP benchmark harness. */
public final class MlpBenchMain {
  private static final Path PUBLIC_MANIFEST = Path.of("instances/public/manifest.example.json");
  private static final String MISSING_PUBLIC_INPUT = "MISSING_PUBLIC_INPUT";
  private static final String PUBLIC_INPUT_LOAD_FAILED = "PUBLIC_INPUT_LOAD_FAILED";

  private MlpBenchMain() {}

  /** Runs the benchmark smoke command. */
  public static void main(final String[] args) {
    if (args.length > 0 && "--help".equals(args[0])) {
      printHelp();
      return;
    }
    try {
      CliArguments arguments = CliArguments.parse(args);
      if (arguments.mode() == BenchmarkMode.EXPANDED) {
        runExpandedBenchmarkSuite(arguments.outputDirectory(), arguments.publicManifest());
      } else {
        runBenchmarkSmoke(arguments.outputDirectory(), arguments.publicManifest());
      }
    } catch (IOException exception) {
      throw new IllegalStateException("could not run benchmark suite", exception);
    }
  }

  private static void printHelp() {
    System.out.println("Usage: mlpbench [--expanded] [output-directory] [public-manifest]");
    System.out.println("Runs the built-in benchmark smoke suite by default.");
  }

  static BenchmarkSmokeResult runBenchmarkSmoke(final Path outputDirectory) throws IOException {
    return runBenchmarkSmoke(outputDirectory, PUBLIC_MANIFEST);
  }

  static BenchmarkSmokeResult runBenchmarkSmoke(
      final Path outputDirectory, final Path publicManifest) throws IOException {
    return runBenchmark(outputDirectory, publicManifest, BenchmarkMode.SMOKE);
  }

  static BenchmarkSmokeResult runExpandedBenchmarkSuite(final Path outputDirectory)
      throws IOException {
    return runExpandedBenchmarkSuite(outputDirectory, PUBLIC_MANIFEST);
  }

  static BenchmarkSmokeResult runExpandedBenchmarkSuite(
      final Path outputDirectory, final Path publicManifest) throws IOException {
    return runBenchmark(outputDirectory, publicManifest, BenchmarkMode.EXPANDED);
  }

  private static BenchmarkSmokeResult runBenchmark(
      final Path outputDirectory, final Path publicManifest, final BenchmarkMode mode)
      throws IOException {
    Files.createDirectories(outputDirectory);
    List<RunRecord> records = generatedBenchmarkRecords(outputDirectory, mode);
    List<RunRecord> allRecords =
        new ArrayList<>(
            records.stream()
                .map(
                    record ->
                        record.withReportMetadata(
                            mode.reportValue(), "not-measured", "not-measured"))
                .toList());
    allRecords.addAll(
        publicBenchmarkRecords(publicManifest, outputDirectory.resolve("public-work"), mode));
    Path markdownPath = outputDirectory.resolve("report.md");
    Path jsonPath = outputDirectory.resolve("report.json");
    Path csvPath = outputDirectory.resolve("report.csv");
    Files.writeString(markdownPath, new MarkdownReportWriter().render(allRecords));
    Files.writeString(jsonPath, new JsonReportWriter().render(allRecords));
    Files.writeString(csvPath, new CsvReportWriter().render(allRecords));
    long accepted =
        allRecords.stream().filter(record -> record.outcome() == RunOutcome.SUCCESS).count();
    long missingPublicInputs =
        allRecords.stream()
            .filter(
                record -> record.failureMessage().contains("missing local public benchmark file"))
            .count();
    MachineFingerprint fingerprint = MachineFingerprint.capture();
    System.out.println(mode.title());
    System.out.println("mode=" + mode.reportValue());
    System.out.println("java=" + fingerprint.javaVersion());
    System.out.println("records=" + allRecords.size());
    System.out.println("accepted=" + accepted);
    System.out.println("missingPublicInputs=" + missingPublicInputs);
    System.out.println("markdown=" + markdownPath);
    System.out.println("json=" + jsonPath);
    System.out.println("csv=" + csvPath);
    return new BenchmarkSmokeResult(allRecords, markdownPath, jsonPath, csvPath);
  }

  private static List<RunRecord> generatedBenchmarkRecords(
      final Path outputDirectory, final BenchmarkMode mode) {
    List<RunRecord> records = new ArrayList<>();
    for (GeneratedLpInstance generated : generatedInstances(mode)) {
      BenchmarkInstance instance =
          new BenchmarkInstance(
              generated.id(),
              generated.fixture().problem(),
              generated.fixture().matrix(),
              expected(generated));
      records.addAll(
          new HarnessRunner()
              .run(
                  new BenchmarkSuite(mode.generatedSuiteId(), List.of(instance)),
                  List.of(
                      new PerformanceLpSolverAdapter(),
                      new EvidenceAdapter(
                          generated.fixture().evidence().objectiveValue().orElseThrow(),
                          generated.fixture().evidence().primal())),
                  new HarnessRunConfig(
                      outputDirectory.resolve("work"),
                      SolverOptions.defaults(),
                      ToleranceProfile.STANDARD)));
    }
    return records;
  }

  private static List<GeneratedLpInstance> generatedInstances(final BenchmarkMode mode) {
    return switch (mode) {
      case SMOKE -> List.of(new NetworkFlowGenerator().threeNode(7L));
      case EXPANDED -> new BenchmarkFixtureGenerator().suite();
    };
  }

  private static List<RunRecord> publicBenchmarkRecords(
      final Path publicManifest, final Path workRoot, final BenchmarkMode mode) throws IOException {
    Path manifestPath = resolveInputPath(publicManifest);
    List<RunRecord> records = new ArrayList<>();
    for (PublicBenchmarkCandidate candidate : readPublicManifest(manifestPath)) {
      if (!"MPS".equals(candidate.format()) || "rejected".equals(candidate.status())) {
        continue;
      }
      Path localPath = resolvePublicPath(manifestPath, candidate.localPath());
      if (!Files.exists(localPath)) {
        records.add(missingPublicInputRecord(candidate, localPath, mode));
        continue;
      }
      try {
        MpsLp lp = new MpsReader().readLp(localPath);
        BenchmarkInstance instance =
            new BenchmarkInstance(
                candidate.id(),
                lp.problem(),
                lp.matrix(),
                new ExpectedValidationResult(Optional.empty(), OptionalDouble.empty()));
        records.addAll(
            new HarnessRunner()
                .run(
                    new BenchmarkSuite(mode.publicSuiteId(), List.of(instance)),
                    List.of(new PublicMetadataAdapter()),
                    new HarnessRunConfig(
                        workRoot, SolverOptions.defaults(), ToleranceProfile.STANDARD)));
      } catch (RuntimeException | IOException exception) {
        records.add(publicLoadFailureRecord(candidate, localPath, exception, mode));
      }
    }
    return records;
  }

  private static Path resolveInputPath(final Path path) {
    if (path.isAbsolute() || Files.exists(path)) {
      return path;
    }
    Path directory = Path.of("").toAbsolutePath();
    while (directory != null) {
      Path candidate = directory.resolve(path);
      if (Files.exists(candidate)) {
        return candidate;
      }
      directory = directory.getParent();
    }
    return path;
  }

  private static ExpectedValidationResult expected(final GeneratedLpInstance instance) {
    return new ExpectedValidationResult(
        Optional.of(statusFor(instance.fixture().evidence().resultKind())),
        instance.fixture().evidence().objectiveValue());
  }

  private static ValidationStatus statusFor(final ExpectedResultKind resultKind) {
    return switch (resultKind) {
      case OPTIMAL -> ValidationStatus.OPTIMAL;
      case INFEASIBLE -> ValidationStatus.INFEASIBLE;
      case UNBOUNDED -> ValidationStatus.UNBOUNDED;
    };
  }

  private static List<PublicBenchmarkCandidate> readPublicManifest(final Path publicManifest)
      throws IOException {
    String json = Files.readString(publicManifest);
    Matcher objectMatcher =
        Pattern.compile("\\{\\s*\"id\"\\s*:.*?\\}", Pattern.DOTALL).matcher(json);
    List<PublicBenchmarkCandidate> candidates = new ArrayList<>();
    while (objectMatcher.find()) {
      String object = objectMatcher.group();
      candidates.add(
          new PublicBenchmarkCandidate(
              stringField(object, "id"),
              stringField(object, "format"),
              stringField(object, "localPath"),
              stringField(object, "status")));
    }
    if (candidates.isEmpty()) {
      throw new IOException("public benchmark manifest has no instances: " + publicManifest);
    }
    return List.copyOf(candidates);
  }

  private static String stringField(final String object, final String field) throws IOException {
    Matcher matcher =
        Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"").matcher(object);
    if (!matcher.find()) {
      throw new IOException("public benchmark manifest entry missing " + field);
    }
    return matcher.group(1);
  }

  private static Path resolvePublicPath(final Path publicManifest, final String localPath) {
    Path path = Path.of(localPath);
    if (path.isAbsolute()) {
      return path;
    }
    Path manifestParent = publicManifest.toAbsolutePath().getParent();
    Path manifestRoot = manifestParent == null ? null : manifestParent.getParent();
    Path projectRoot = manifestRoot == null ? null : manifestRoot.getParent();
    if (localPath.startsWith("instances/public/") && projectRoot != null) {
      return projectRoot.resolve(path);
    }
    return path;
  }

  private static RunRecord missingPublicInputRecord(
      final PublicBenchmarkCandidate candidate, final Path localPath, final BenchmarkMode mode) {
    SolverOptions options = SolverOptions.defaults();
    SolverRunResult result =
        new SolverRunResult(
            new SolverId("public-benchmark", "manifest"),
            SolverStatus.UNSUPPORTED,
            OptionalDouble.empty(),
            new double[0],
            0.0d,
            "missing local public benchmark file: " + localPath);
    ValidationReport report =
        new ValidationReport(
            ToleranceProfile.STANDARD,
            List.of(
                new ValidationFinding(
                    MISSING_PUBLIC_INPUT,
                    "public benchmark candidate is not downloaded locally",
                    1.0d)));
    return new RunRecord(
        mode.publicSuiteId(),
        candidate.id(),
        result,
        report,
        RunOutcome.SOLVER_UNAVAILABLE,
        result.message(),
        mode.reportValue(),
        "not-measured",
        "not-measured",
        options,
        MachineFingerprint.capture(),
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        "not-measured");
  }

  private static RunRecord publicLoadFailureRecord(
      final PublicBenchmarkCandidate candidate,
      final Path localPath,
      final Exception exception,
      final BenchmarkMode mode) {
    SolverOptions options = SolverOptions.defaults();
    String message =
        "could not load public benchmark file " + localPath + ": " + exception.getMessage();
    SolverRunResult result =
        new SolverRunResult(
            new SolverId("public-benchmark", "manifest"),
            SolverStatus.ERROR,
            OptionalDouble.empty(),
            new double[0],
            0.0d,
            message);
    ValidationReport report =
        new ValidationReport(
            ToleranceProfile.STANDARD,
            List.of(
                new ValidationFinding(
                    PUBLIC_INPUT_LOAD_FAILED,
                    "public benchmark candidate could not be loaded",
                    1.0d)));
    return new RunRecord(
        mode.publicSuiteId(),
        candidate.id(),
        result,
        report,
        RunOutcome.ADAPTER_ERROR,
        message,
        mode.reportValue(),
        "not-measured",
        "not-measured",
        options,
        MachineFingerprint.capture(),
        Double.NaN,
        Double.NaN,
        Double.NaN,
        Double.NaN,
        "not-measured");
  }

  /**
   * Benchmark smoke result paths and records.
   *
   * @param records run records in report order
   * @param markdownPath Markdown report path
   * @param jsonPath JSON report path
   * @param csvPath CSV report path
   */
  public record BenchmarkSmokeResult(
      List<RunRecord> records, Path markdownPath, Path jsonPath, Path csvPath) {
    /**
     * Creates a benchmark smoke result.
     *
     * @param records run records in report order
     * @param markdownPath Markdown report path
     * @param jsonPath JSON report path
     * @param csvPath CSV report path
     */
    public BenchmarkSmokeResult {
      records = List.copyOf(records);
    }
  }

  private record EvidenceAdapter(double objectiveValue, double[] primal)
      implements LpSolverAdapter {
    private EvidenceAdapter {
      primal = primal.clone();
    }

    @Override
    public SolverId id() {
      return new SolverId("generated-evidence", "built-in");
    }

    @Override
    public SolverRunResult solve(
        final SolverInput input,
        final SolverOptions options,
        final SolverWorkDirectory workDirectory) {
      return new SolverRunResult(
          id(),
          SolverStatus.OPTIMAL,
          OptionalDouble.of(objectiveValue),
          primal,
          0.0d,
          "built-in generated evidence adapter");
    }
  }

  private record PublicBenchmarkCandidate(
      String id, String format, String localPath, String status) {}

  private record PublicMetadataAdapter() implements LpSolverAdapter {
    @Override
    public SolverId id() {
      return new SolverId("public-benchmark", "metadata");
    }

    @Override
    public SolverRunResult solve(
        final SolverInput input,
        final SolverOptions options,
        final SolverWorkDirectory workDirectory) {
      return new SolverRunResult(
          id(),
          SolverStatus.UNSUPPORTED,
          OptionalDouble.empty(),
          new double[0],
          0.0d,
          "public benchmark loaded; no benchmark solver configured");
    }
  }

  private enum BenchmarkMode {
    SMOKE(
        "benchmark-smoke",
        "mlpbench benchmark smoke",
        Path.of("build/reports/benchmark-smoke"),
        "benchmark-smoke-generated",
        "benchmark-smoke-public"),
    EXPANDED(
        "expanded-benchmark-suite",
        "mlpbench expanded benchmark suite",
        Path.of("build/reports/expanded-benchmark-suite"),
        "expanded-benchmark-generated",
        "expanded-benchmark-public");

    private final String reportValue;
    private final String title;
    private final Path defaultOutputDirectory;
    private final String generatedSuiteId;
    private final String publicSuiteId;

    BenchmarkMode(
        final String reportValue,
        final String title,
        final Path defaultOutputDirectory,
        final String generatedSuiteId,
        final String publicSuiteId) {
      this.reportValue = reportValue;
      this.title = title;
      this.defaultOutputDirectory = defaultOutputDirectory;
      this.generatedSuiteId = generatedSuiteId;
      this.publicSuiteId = publicSuiteId;
    }

    String reportValue() {
      return reportValue;
    }

    String title() {
      return title;
    }

    Path defaultOutputDirectory() {
      return defaultOutputDirectory;
    }

    String generatedSuiteId() {
      return generatedSuiteId;
    }

    String publicSuiteId() {
      return publicSuiteId;
    }
  }

  private record CliArguments(BenchmarkMode mode, Path outputDirectory, Path publicManifest) {
    static CliArguments parse(final String[] args) {
      BenchmarkMode mode = BenchmarkMode.SMOKE;
      int index = 0;
      if (args.length > 0 && "--expanded".equals(args[0])) {
        mode = BenchmarkMode.EXPANDED;
        index = 1;
      }
      Path outputDirectory =
          index < args.length ? Path.of(args[index++]) : mode.defaultOutputDirectory();
      Path publicManifest = index < args.length ? Path.of(args[index]) : PUBLIC_MANIFEST;
      return new CliArguments(mode, outputDirectory, publicManifest);
    }
  }
}
