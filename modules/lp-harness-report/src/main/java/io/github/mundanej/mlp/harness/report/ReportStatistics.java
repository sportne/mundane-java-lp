package io.github.mundanej.mlp.harness.report;

import io.github.mundanej.mlp.harness.RunOutcome;
import io.github.mundanej.mlp.harness.RunRecord;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ReportStatistics {
  private static final int WARMUP_COUNT = 0;

  private ReportStatistics() {}

  static Map<Key, Summary> summarize(final List<RunRecord> records) {
    Map<Key, List<RunRecord>> groups = new HashMap<>();
    for (RunRecord record : records) {
      groups.computeIfAbsent(Key.from(record), ignored -> new ArrayList<>()).add(record);
    }
    Map<Key, Summary> summaries = new HashMap<>();
    for (Map.Entry<Key, List<RunRecord>> entry : groups.entrySet()) {
      summaries.put(entry.getKey(), Summary.from(entry.getValue()));
    }
    return Map.copyOf(summaries);
  }

  record Key(String mode, String suite, String instance, String solver) {
    static Key from(final RunRecord record) {
      return new Key(
          record.runMode(),
          record.suiteId(),
          record.instanceId(),
          record.solverResult().solverId().name());
    }
  }

  record Summary(
      int warmupCount,
      int repetitionCount,
      String solveMinSeconds,
      String solveMedianSeconds,
      String solveMaxSeconds,
      int failureCount,
      int unavailableCount) {
    static Summary from(final List<RunRecord> records) {
      List<Double> acceptedSolveSeconds =
          records.stream()
              .filter(record -> record.outcome() == RunOutcome.SUCCESS)
              .filter(record -> record.validationReport().accepted())
              .filter(record -> isComparativeTimingStatus(record.solverResult().status()))
              .map(record -> record.solverResult().elapsedSeconds())
              .filter(Double::isFinite)
              .sorted(Comparator.naturalOrder())
              .toList();
      int failureCount =
          (int)
              records.stream()
                  .filter(record -> record.outcome() != RunOutcome.UNSUPPORTED)
                  .filter(record -> record.outcome() != RunOutcome.SOLVER_UNAVAILABLE)
                  .filter(
                      record ->
                          record.outcome() == RunOutcome.ADAPTER_ERROR
                              || record.outcome() == RunOutcome.VALIDATION_FAILED
                              || isTerminalFailureStatus(record.solverResult().status()))
                  .count();
      int unavailableCount =
          (int)
              records.stream()
                  .filter(
                      record ->
                          record.outcome() == RunOutcome.SOLVER_UNAVAILABLE
                              || record.outcome() == RunOutcome.UNSUPPORTED)
                  .count();
      return new Summary(
          WARMUP_COUNT,
          records.size(),
          timing(acceptedSolveSeconds, 0),
          median(acceptedSolveSeconds),
          timing(acceptedSolveSeconds, acceptedSolveSeconds.size() - 1),
          failureCount,
          unavailableCount);
    }

    private static String timing(final List<Double> values, final int index) {
      return values.isEmpty() ? "not-measured" : Double.toString(values.get(index));
    }

    private static String median(final List<Double> values) {
      if (values.isEmpty()) {
        return "not-measured";
      }
      int middle = values.size() / 2;
      if (values.size() % 2 == 1) {
        return Double.toString(values.get(middle));
      }
      return Double.toString((values.get(middle - 1) + values.get(middle)) / 2.0d);
    }

    private static boolean isComparativeTimingStatus(final SolverStatus status) {
      return switch (status) {
        case OPTIMAL, FEASIBLE, INFEASIBLE, UNBOUNDED, INFEASIBLE_OR_UNBOUNDED -> true;
        case TIME_LIMIT, MEMORY_LIMIT, NUMERICAL_FAILURE, ERROR, UNSUPPORTED, UNKNOWN -> false;
      };
    }

    private static boolean isTerminalFailureStatus(final SolverStatus status) {
      return switch (status) {
        case TIME_LIMIT, MEMORY_LIMIT, NUMERICAL_FAILURE, ERROR, UNKNOWN -> true;
        case OPTIMAL, FEASIBLE, INFEASIBLE, UNBOUNDED, INFEASIBLE_OR_UNBOUNDED, UNSUPPORTED ->
            false;
      };
    }
  }
}
