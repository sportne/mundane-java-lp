# G7-004 performance benchmark suite baseline

## Status

proposed

## Requirement IDs

- REQ-0-1-PERFORMANCE-BENCHMARK-SUITE

## References

- `docs/roadmap/tasks/g7-001-generated-fixture-baseline.md`
- `docs/roadmap/tasks/g7-002-public-benchmark-curation.md`
- `docs/roadmap/tasks/g7-003-benchmark-evidence-baseline.md`

## Target module

- `modules/lp-harness-cli`
- `modules/lp-generators`
- Benchmark documentation.

## Allowed files

- `modules/lp-harness-cli/`
- `modules/lp-generators/`
- `modules/lp-testkit/`
- `docs/verification/`
- Benchmark examples.

## Forbidden files

- Solver implementation changes.
- Third-party adapter internals.

## Required behavior

- Wire generated and curated public instances into a repeatable benchmark suite.
- Keep the default benchmark set small enough for local development.
- Preserve separate reporting for parse, export, solve, validation, and total
  time where data is available.

## Required tests

- Benchmark smoke tests that run without external solver binaries.
- Tests for missing public benchmark files reporting cleanly.

## Required docs

- Update benchmark suite, generated instance, public benchmark, and harness docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
