package io.github.mundanej.mlp.harness.cli;

import io.github.mundanej.mlp.generators.CanonicalLpFixture;
import io.github.mundanej.mlp.generators.CanonicalLpFixtures;
import io.github.mundanej.mlp.generators.ExpectedResultKind;
import io.github.mundanej.mlp.harness.BenchmarkInstance;
import io.github.mundanej.mlp.harness.BenchmarkSuite;
import io.github.mundanej.mlp.harness.HarnessRunConfig;
import io.github.mundanej.mlp.harness.HarnessRunner;
import io.github.mundanej.mlp.harness.MachineFingerprint;
import io.github.mundanej.mlp.harness.RunOutcome;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Command-line entrypoint for the LP benchmark harness. */
public final class MlpBenchMain {
    private MlpBenchMain() {
    }

    /** Runs the tiny harness smoke command. */
    public static void main(final String[] args) {
        if (args.length > 0 && "--help".equals(args[0])) {
            printHelp();
            return;
        }
        try {
            runTinySmoke();
        } catch (IOException exception) {
            throw new IllegalStateException("could not run tiny harness smoke", exception);
        }
    }

    private static void printHelp() {
        System.out.println("Usage: mlpbench [--help]");
        System.out.println("Runs the built-in tiny harness smoke suite by default.");
    }

    private static void runTinySmoke() throws IOException {
        CanonicalLpFixture fixture = CanonicalLpFixtures.singleBoundedVariable();
        BenchmarkInstance instance = new BenchmarkInstance(
                fixture.problem().name(),
                fixture.problem(),
                fixture.matrix(),
                expected(fixture));
        List<RunRecord> records = new HarnessRunner().run(
                new BenchmarkSuite("tiny-smoke", List.of(instance)),
                List.of(new TinyAdapter(fixture.evidence().objectiveValue().orElseThrow())),
                new HarnessRunConfig(
                        Files.createTempDirectory("mlpbench-"),
                        SolverOptions.defaults(),
                        ToleranceProfile.STANDARD));
        long accepted = records.stream().filter(record -> record.outcome() == RunOutcome.SUCCESS).count();
        MachineFingerprint fingerprint = MachineFingerprint.capture();
        System.out.println("mlpbench G4 smoke");
        System.out.println("java=" + fingerprint.javaVersion());
        System.out.println("records=" + records.size());
        System.out.println("accepted=" + accepted);
    }

    private static ExpectedValidationResult expected(final CanonicalLpFixture fixture) {
        return new ExpectedValidationResult(
                Optional.of(statusFor(fixture.evidence().resultKind())),
                fixture.evidence().objectiveValue());
    }

    private static ValidationStatus statusFor(final ExpectedResultKind resultKind) {
        return switch (resultKind) {
            case OPTIMAL -> ValidationStatus.OPTIMAL;
            case INFEASIBLE -> ValidationStatus.INFEASIBLE;
            case UNBOUNDED -> ValidationStatus.UNBOUNDED;
        };
    }

    private record TinyAdapter(double objectiveValue) implements LpSolverAdapter {
        @Override
        public SolverId id() {
            return new SolverId("tiny", "built-in");
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
                    new double[0],
                    0.0d,
                    "built-in tiny smoke adapter");
        }
    }
}
