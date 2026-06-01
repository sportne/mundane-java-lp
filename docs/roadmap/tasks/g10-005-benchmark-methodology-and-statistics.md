# G10-005 benchmark methodology and statistics

## Status

proposed

## Requirement IDs

- REQ-0-1-BENCHMARK-METHODOLOGY

## References

- `docs/literature/benchmark-methodology.md`
- `docs/roadmap/tasks/g9-010-performance-measurement-instrumentation.md`
- `docs/roadmap/tasks/g10-004-expanded-generated-and-public-benchmarks.md`

## Target module

- Harness report modules.
- Benchmark documentation.

## Allowed files

- `modules/lp-harness-api/`
- `modules/lp-harness-cli/`
- `modules/lp-harness-report/`
- `docs/verification/`
- `docs/literature/`

## Forbidden files

- Solver implementation changes.
- Claims that are not derived from the recorded methodology.

## Required behavior

- Define benchmark warmup, repetition, timeout, ordering, and isolation policy.
- Add report fields for repetition count, warmup count, min/median/max, and
  failure/unavailable counts.
- Keep parse/load, export/canonicalization, solve, validation, total time, and
  memory fields visible.
- Require validation acceptance before a timing sample can contribute to
  comparative evidence.
- Mark any unavailable metric as `not-measured` rather than omitting it.

## Required tests

- Deterministic report rendering tests for repeated samples.
- Tests that invalid or unvalidated solver results are excluded from timing
  summaries.
- Tests for timeout and failure aggregation.

## Required docs

- Update benchmark methodology, benchmark suite, and report documentation.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate expandedBenchmarkSuite --console=plain
```
