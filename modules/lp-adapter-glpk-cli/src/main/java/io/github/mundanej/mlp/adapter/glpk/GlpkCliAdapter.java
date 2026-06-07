package io.github.mundanej.mlp.adapter.glpk;

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

/** CLI adapter for GLPK using MPS export and bounded process logs. */
public final class GlpkCliAdapter implements LpSolverAdapter {
  private static final SolverId ID = new SolverId("glpk", "cli");
  private static final int MAX_LOG_BYTES = 16 * 1024;
  private static final Pattern OBJECTIVE_LINE_PATTERN =
      Pattern.compile(
          "(?i)^.*(?:objective\\s*[:=].*?=|\\bobj\\s*=)\\s*(\\S+).*$", Pattern.MULTILINE);
  private final String binaryName;

  /** Creates an adapter that invokes {@code glpsol}. */
  public GlpkCliAdapter() {
    this("glpsol");
  }

  /**
   * Creates an adapter with an explicit binary name.
   *
   * @param binaryName binary to execute
   */
  public GlpkCliAdapter(final String binaryName) {
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
        "--freemps",
        modelPath.toString(),
        "--tmlim",
        Long.toString(options.timeLimit().toSeconds()),
        "--write",
        solutionPath.toString());
  }

  SolverStatus parseStatus(final String output, final int exitCode) {
    String normalized = output.toLowerCase(java.util.Locale.ROOT);
    if (normalized.contains("integer optimization")) {
      return SolverStatus.UNSUPPORTED;
    }
    if (normalized.contains("infeasible or unbounded")) {
      return SolverStatus.INFEASIBLE_OR_UNBOUNDED;
    }
    if (normalized.contains("no primal feasible solution")
        || normalized.contains("problem has no feasible solution")
        || normalized.contains("status:     infeasible")
        || containsWord(normalized, "infeasible")) {
      return SolverStatus.INFEASIBLE;
    }
    if (normalized.contains("unbounded") || normalized.contains("no dual feasible solution")) {
      return SolverStatus.UNBOUNDED;
    }
    if (normalized.contains("time limit") || normalized.contains("time limit exceeded")) {
      return SolverStatus.TIME_LIMIT;
    }
    if (normalized.contains("numerical")) {
      return SolverStatus.NUMERICAL_FAILURE;
    }
    if (normalized.contains("optimal lp solution found")
        || normalized.contains("optimal solution found")
        || normalized.contains("status:     optimal")
        || normalized.contains("status: optimal")) {
      return SolverStatus.OPTIMAL;
    }
    if (exitCode != 0) {
      return SolverStatus.ERROR;
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
          "GLPK binary unavailable: " + binaryName);
    }
    StreamCollector collector = new StreamCollector(process.getInputStream(), MAX_LOG_BYTES);
    Thread reader = new Thread(collector, "glpk-output-reader");
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
            "GLPK reported malformed objective. " + output);
      }
      OptionalDouble objective = parseObjective(output);
      if (status == SolverStatus.OPTIMAL && objective.isEmpty()) {
        return result(
            SolverStatus.ERROR,
            OptionalDouble.empty(),
            startNanos,
            "GLPK reported optimal without parseable objective. " + output);
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

  private static boolean containsWord(final String text, final String word) {
    return Pattern.compile("\\b" + Pattern.quote(word) + "\\b").matcher(text).find();
  }

  private record ObjectiveParseResult(OptionalDouble objective, boolean malformed) {}

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
}
