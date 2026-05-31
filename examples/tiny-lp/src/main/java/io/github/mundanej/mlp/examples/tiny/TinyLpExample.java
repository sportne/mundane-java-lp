package io.github.mundanej.mlp.examples.tiny;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.generators.CanonicalLpFixtures;
import io.github.mundanej.mlp.harness.BenchmarkInstance;
import io.github.mundanej.mlp.harness.BenchmarkSuite;
import io.github.mundanej.mlp.harness.HarnessRunConfig;
import io.github.mundanej.mlp.harness.HarnessRunner;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.io.mps.MpsLp;
import io.github.mundanej.mlp.io.mps.MpsReader;
import io.github.mundanej.mlp.io.mps.MpsWriter;
import io.github.mundanej.mlp.solver.simple.SimpleLpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Tiny LP example. */
public final class TinyLpExample {
  private TinyLpExample() {}

  /** Runs the example. */
  public static void main(final String[] args) throws IOException {
    Path outputDirectory =
        args.length == 0 ? Path.of("build/reports/tiny-lp-mps") : Path.of(args[0]);
    RunRecord record = runSimpleMpsSmoke(outputDirectory);
    System.out.println("tiny-lp simple MPS smoke");
    System.out.println("instance=" + record.instanceId());
    System.out.println("solver=" + record.solverResult().solverId().name());
    System.out.println("status=" + record.solverResult().status());
    System.out.println("outcome=" + record.outcome());
    System.out.println("mps=" + outputDirectory.resolve("single-bounded-variable.mps"));
  }

  /**
   * Writes a supported Tier 1 fixture to MPS, reads it back, and validates the simple solver
   * through the harness.
   *
   * @param outputDirectory directory for the MPS file and harness work area
   */
  public static RunRecord runSimpleMpsSmoke(final Path outputDirectory) throws IOException {
    Files.createDirectories(outputDirectory);
    CanonicalLpFixture fixture = CanonicalLpFixtures.singleBoundedVariable();
    Path mpsPath = outputDirectory.resolve("single-bounded-variable.mps");
    MpsLp original =
        new MpsLp(
            fixture.problem(), fixture.matrix(), fixture.rowNames(), fixture.columnNames(), "OBJ");
    new MpsWriter().write(original, mpsPath);
    MpsLp roundTripped = new MpsReader().readLp(mpsPath);
    BenchmarkInstance instance =
        new BenchmarkInstance(
            "tiny-mps-single-bounded-variable",
            roundTripped.problem(),
            roundTripped.matrix(),
            ExpectedValidationResult.optimal(fixture.evidence().objectiveValue().orElseThrow()));
    return new HarnessRunner()
        .run(
            new BenchmarkSuite("tiny-lp-simple-mps", List.of(instance)),
            List.of(new SimpleLpSolverAdapter()),
            new HarnessRunConfig(
                outputDirectory.resolve("work"),
                SolverOptions.defaults(),
                ToleranceProfile.STANDARD))
        .getFirst();
  }
}
