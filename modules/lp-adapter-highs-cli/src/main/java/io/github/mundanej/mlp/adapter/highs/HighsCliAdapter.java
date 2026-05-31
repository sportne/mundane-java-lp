package io.github.mundanej.mlp.adapter.highs;

import io.github.mundanej.mlp.io.mps.MpsFormatException;
import io.github.mundanej.mlp.io.mps.MpsLp;
import io.github.mundanej.mlp.io.mps.MpsWriter;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.OptionalDouble;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** CLI adapter for HiGHS. */
public final class HighsCliAdapter implements LpSolverAdapter {
  private static final SolverId ID = new SolverId("highs", "cli");
  private static final int MAX_LOG_BYTES = 16 * 1024;
  private static final Pattern STATUS_LINE_PATTERN =
      Pattern.compile("(?i)^\\s*(?:model\\s+)?status\\s*[:=]\\s*(.+?)\\s*$", Pattern.MULTILINE);
  private static final Pattern OBJECTIVE_LINE_PATTERN =
      Pattern.compile(
          "(?i)^\\s*(?:objective(?:\\s+value)?|primal\\s+objective)\\s*[:=]\\s*(\\S+).*$",
          Pattern.MULTILINE);
  private final String binaryName;

  /** Creates an adapter that invokes {@code highs}. */
  public HighsCliAdapter() {
    this("highs");
  }

  /**
   * Creates an adapter with an explicit binary name.
   *
   * @param binaryName binary to execute
   */
  public HighsCliAdapter(final String binaryName) {
    if (binaryName == null || binaryName.isBlank()) {
      throw new IllegalArgumentException("binaryName must not be blank");
    }
    this.binaryName = binaryName;
  }

  /** {@inheritDoc} */
  @Override
  public SolverId id() {
    return ID;
  }

  /** {@inheritDoc} */
  @Override
  public SolverRunResult solve(
      final SolverInput input,
      final SolverOptions options,
      final SolverWorkDirectory workDirectory) {
    long startNanos = System.nanoTime();
    try {
      Files.createDirectories(workDirectory.path());
      Path modelPath = workDirectory.path().resolve("model.mps");
      Path solutionPath = workDirectory.path().resolve("solution.sol");
      new MpsWriter()
          .write(
              new MpsLp(
                  input.problem(),
                  input.matrix(),
                  input.rowNames(),
                  input.columnNames(),
                  input.objectiveRowName()),
              modelPath);
      return runProcess(command(modelPath, solutionPath, options), options.timeLimit(), startNanos);
    } catch (MpsFormatException exception) {
      return result(
          SolverStatus.UNSUPPORTED, OptionalDouble.empty(), startNanos, exception.getMessage());
    } catch (IOException exception) {
      return result(SolverStatus.ERROR, OptionalDouble.empty(), startNanos, exception.getMessage());
    }
  }

  List<String> command(final Path modelPath, final Path solutionPath, final SolverOptions options) {
    return List.of(
        binaryName,
        "--model_file=" + modelPath,
        "--solution_file=" + solutionPath,
        "--write_solution_to_file=true",
        "--time_limit=" + options.timeLimit().toSeconds(),
        "--threads=" + options.threads());
  }

  SolverStatus parseStatus(final String output, final int exitCode) {
    Matcher matcher = STATUS_LINE_PATTERN.matcher(output);
    if (matcher.find()) {
      SolverStatus status = statusFromText(matcher.group(1));
      if (status != SolverStatus.UNKNOWN) {
        return status;
      }
    }
    SolverStatus status = fallbackStatusFromText(output);
    if (status != SolverStatus.UNKNOWN) {
      return status;
    }
    if (exitCode != 0) {
      return SolverStatus.ERROR;
    }
    return SolverStatus.UNKNOWN;
  }

  private static SolverStatus statusFromText(final String text) {
    String normalized = text.toLowerCase(java.util.Locale.ROOT);
    if (normalized.contains("not optimal")) {
      return SolverStatus.UNKNOWN;
    }
    if (normalized.contains("infeasible or unbounded")) {
      return SolverStatus.INFEASIBLE_OR_UNBOUNDED;
    }
    if (containsWord(normalized, "infeasible")) {
      return SolverStatus.INFEASIBLE;
    }
    if (containsWord(normalized, "unbounded")) {
      return SolverStatus.UNBOUNDED;
    }
    if (normalized.contains("time limit") || normalized.contains("time_limit")) {
      return SolverStatus.TIME_LIMIT;
    }
    if (normalized.contains("numerical")) {
      return SolverStatus.NUMERICAL_FAILURE;
    }
    if (normalized.contains("optimal")) {
      return SolverStatus.OPTIMAL;
    }
    return SolverStatus.UNKNOWN;
  }

  private static SolverStatus fallbackStatusFromText(final String text) {
    String normalized = text.toLowerCase(java.util.Locale.ROOT);
    if (normalized.contains("not optimal")) {
      return SolverStatus.UNKNOWN;
    }
    if (normalized.contains("infeasible or unbounded")) {
      return SolverStatus.INFEASIBLE_OR_UNBOUNDED;
    }
    if (containsWord(normalized, "infeasible")) {
      return SolverStatus.INFEASIBLE;
    }
    if (containsWord(normalized, "unbounded")) {
      return SolverStatus.UNBOUNDED;
    }
    if (normalized.contains("time limit") || normalized.contains("time_limit")) {
      return SolverStatus.TIME_LIMIT;
    }
    if (normalized.contains("numerical")) {
      return SolverStatus.NUMERICAL_FAILURE;
    }
    if (normalized.contains("optimal solution") || normalized.contains("solved to optimality")) {
      return SolverStatus.OPTIMAL;
    }
    return SolverStatus.UNKNOWN;
  }

  OptionalDouble parseObjective(final String output) {
    return parseObjectiveResult(output).objective();
  }

  boolean hasMalformedObjective(final String output) {
    return parseObjectiveResult(output).malformed();
  }

  private static ObjectiveParseResult parseObjectiveResult(final String output) {
    Matcher matcher = OBJECTIVE_LINE_PATTERN.matcher(output);
    if (!matcher.find()) {
      return new ObjectiveParseResult(OptionalDouble.empty(), false);
    }
    try {
      double value = Double.parseDouble(matcher.group(1));
      if (!Double.isFinite(value)) {
        return new ObjectiveParseResult(OptionalDouble.empty(), true);
      }
      return new ObjectiveParseResult(OptionalDouble.of(value), false);
    } catch (NumberFormatException exception) {
      return new ObjectiveParseResult(OptionalDouble.empty(), true);
    }
  }

  private static boolean containsWord(final String text, final String word) {
    return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
  }

  private SolverRunResult runProcess(
      final List<String> command, final Duration timeout, final long startNanos) {
    Process process;
    try {
      process = new ProcessBuilder(command).redirectErrorStream(true).start();
    } catch (IOException exception) {
      return result(
          SolverStatus.UNSUPPORTED,
          OptionalDouble.empty(),
          startNanos,
          "HiGHS binary unavailable: " + binaryName);
    }
    StreamCollector collector = new StreamCollector(process.getInputStream(), MAX_LOG_BYTES);
    Thread reader = new Thread(collector, "highs-output-reader");
    reader.setDaemon(true);
    reader.start();
    try {
      boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!finished) {
        process.destroyForcibly();
        reader.join(1000L);
        return result(
            SolverStatus.TIME_LIMIT, OptionalDouble.empty(), startNanos, collector.output());
      }
      reader.join(1000L);
      String output = collector.output();
      SolverStatus status = parseStatus(output, process.exitValue());
      if (hasMalformedObjective(output)) {
        return result(
            SolverStatus.ERROR,
            OptionalDouble.empty(),
            startNanos,
            "HiGHS reported malformed objective. " + output);
      }
      OptionalDouble objective = parseObjective(output);
      if (status == SolverStatus.OPTIMAL && objective.isEmpty()) {
        return result(
            SolverStatus.ERROR,
            OptionalDouble.empty(),
            startNanos,
            "HiGHS reported optimal without parseable objective. " + output);
      }
      return result(status, objective, startNanos, output);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
      return result(SolverStatus.ERROR, OptionalDouble.empty(), startNanos, "interrupted");
    }
  }

  private static SolverRunResult result(
      final SolverStatus status,
      final OptionalDouble objective,
      final long startNanos,
      final String message) {
    double elapsedSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0d;
    return new SolverRunResult(
        ID, status, objective, new double[0], elapsedSeconds, bound(message));
  }

  private static String bound(final String message) {
    if (message == null) {
      return "";
    }
    if (message.length() <= MAX_LOG_BYTES) {
      return message;
    }
    return message.substring(0, MAX_LOG_BYTES) + "\n[truncated]";
  }

  private static final class StreamCollector implements Runnable {
    private final InputStream stream;
    private final int maxBytes;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private boolean truncated;

    private StreamCollector(final InputStream stream, final int maxBytes) {
      this.stream = stream;
      this.maxBytes = maxBytes;
    }

    @Override
    public void run() {
      byte[] buffer = new byte[1024];
      int read;
      try {
        while ((read = stream.read(buffer)) >= 0) {
          int remaining = maxBytes - captured.size();
          if (remaining > 0) {
            captured.write(buffer, 0, Math.min(read, remaining));
          }
          if (read > remaining) {
            truncated = true;
          }
        }
      } catch (IOException ignored) {
        truncated = true;
      }
    }

    private String output() {
      String output = captured.toString(StandardCharsets.UTF_8);
      return truncated ? output + "\n[truncated]" : output;
    }
  }

  private record ObjectiveParseResult(OptionalDouble objective, boolean malformed) {}
}
