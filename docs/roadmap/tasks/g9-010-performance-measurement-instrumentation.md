# G9-010 performance measurement instrumentation

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-MEASUREMENT

## References

- `docs/roadmap/tasks/g7-003-benchmark-evidence-baseline.md`
- `docs/verification/benchmark-suite.md`

## Target module

- `modules/lp-harness-api`
- `modules/lp-harness-report`
- Performance solver module.

## Allowed files

- Harness modules.
- Performance solver module.
- Benchmark docs.

## Forbidden files

- Solver algorithm optimization.
- Third-party adapter internals.

## Required behavior

- Record parse, export, solve, validation, allocation or memory, and total
  timing where practical.
- Keep instrumentation lightweight and deterministic.
- Avoid heavyweight profiling dependencies in normal quality gates.

## Required tests

- Unit tests for reported timing fields and missing measurement behavior.
- Report tests proving measurement fields are emitted consistently.

## Required docs

- Update benchmark suite and report docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
