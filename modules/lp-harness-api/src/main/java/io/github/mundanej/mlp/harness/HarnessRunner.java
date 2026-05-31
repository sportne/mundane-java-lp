package io.github.mundanej.mlp.harness;

import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import io.github.mundanej.mlp.validation.LpSolutionValidator;
import io.github.mundanej.mlp.validation.ValidationEvidence;
import io.github.mundanej.mlp.validation.ValidationFinding;
import io.github.mundanej.mlp.validation.ValidationReport;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/** Executes benchmark suites against explicit solver adapters. */
public final class HarnessRunner {
    private final LpSolutionValidator validator = new LpSolutionValidator();

    /**
     * Runs each instance against each adapter in deterministic order.
     *
     * @param suite benchmark suite
     * @param adapters explicit solver adapter list
     * @param config harness run configuration
     */
    public List<RunRecord> run(
            final BenchmarkSuite suite,
            final List<LpSolverAdapter> adapters,
            final HarnessRunConfig config) {
        Objects.requireNonNull(suite, "suite");
        Objects.requireNonNull(adapters, "adapters");
        Objects.requireNonNull(config, "config");
        try {
            Files.createDirectories(config.workRoot());
        } catch (IOException exception) {
            throw new IllegalStateException("could not create harness work root", exception);
        }
        List<RunRecord> records = new ArrayList<>();
        int sequence = 0;
        for (BenchmarkInstance instance : suite.instances()) {
            for (LpSolverAdapter adapter : adapters) {
                records.add(runOne(suite, instance, adapter, config, sequence));
                sequence++;
            }
        }
        return List.copyOf(records);
    }

    private RunRecord runOne(
            final BenchmarkSuite suite,
            final BenchmarkInstance instance,
            final LpSolverAdapter adapter,
            final HarnessRunConfig config,
            final int sequence) {
        SolverId solverId = new SolverId("unknown", "adapter-id-failed");
        Path workDirectory = config.workRoot().resolve(directoryName(suite, instance, solverId, sequence));
        try {
            solverId = adapter.id();
            workDirectory = config.workRoot().resolve(directoryName(suite, instance, solverId, sequence));
            Files.createDirectories(workDirectory);
            SolverRunResult result = adapter.solve(
                    instance.problem(),
                    config.solverOptions(),
                    new SolverWorkDirectory(workDirectory));
            ValidationReport report = validator.validate(
                    instance.problem(),
                    instance.matrix(),
                    instance.expectedResult(),
                    evidence(result),
                    config.toleranceProfile());
            RunOutcome outcome = outcome(result, report);
            return new RunRecord(
                    suite.id(),
                    instance.id(),
                    result,
                    report,
                    outcome,
                    failureMessage(outcome, result.message()),
                    "not-measured",
                    config.solverOptions(),
                    MachineFingerprint.capture(),
                    0.0d,
                    0.0d,
                    0.0d,
                    result.elapsedSeconds());
        } catch (RuntimeException | IOException exception) {
            SolverRunResult result = new SolverRunResult(
                    solverId,
                    SolverStatus.ERROR,
                    OptionalDouble.empty(),
                    0.0d,
                    exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            ValidationReport report = new ValidationReport(
                    config.toleranceProfile(),
                    List.of(new ValidationFinding("ADAPTER_ERROR", "adapter execution failed", 1.0d)));
            return new RunRecord(
                    suite.id(),
                    instance.id(),
                    result,
                    report,
                    RunOutcome.ADAPTER_ERROR,
                    result.message(),
                    "not-measured",
                    config.solverOptions(),
                    MachineFingerprint.capture(),
                    0.0d,
                    0.0d,
                    0.0d,
                    result.elapsedSeconds());
        }
    }

    private static ValidationEvidence evidence(final SolverRunResult result) {
        return new ValidationEvidence(
                Optional.of(status(result.status())),
                result.objectiveValue(),
                new double[0]);
    }

    private static ValidationStatus status(final SolverStatus status) {
        return ValidationStatus.valueOf(status.name());
    }

    private static RunOutcome outcome(final SolverRunResult result, final ValidationReport report) {
        if (result.status() == SolverStatus.UNSUPPORTED) {
            return RunOutcome.SOLVER_UNAVAILABLE;
        }
        if (result.status() == SolverStatus.ERROR) {
            return RunOutcome.ADAPTER_ERROR;
        }
        return report.accepted() ? RunOutcome.SUCCESS : RunOutcome.VALIDATION_FAILED;
    }

    private static String failureMessage(final RunOutcome outcome, final String message) {
        if (outcome != RunOutcome.ADAPTER_ERROR) {
            return "";
        }
        if (message == null || message.isBlank()) {
            return "adapter reported error";
        }
        return message;
    }

    private static String directoryName(
            final BenchmarkSuite suite,
            final BenchmarkInstance instance,
            final SolverId solverId,
            final int sequence) {
        return String.format("%04d-%s-%s-%s",
                sequence,
                sanitize(suite.id()),
                sanitize(instance.id()),
                sanitize(solverId.name()));
    }

    private static String sanitize(final String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
