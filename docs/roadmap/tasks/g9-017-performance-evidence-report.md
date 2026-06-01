# G9-017 performance evidence report

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-EVIDENCE-REPORT

## References

- `docs/roadmap/tasks/g7-003-benchmark-evidence-baseline.md`
- `docs/roadmap/tasks/g9-016-performance-iteration-3-robustness-and-scaling.md`
- `docs/verification/benchmark-suite.md`

## Target module

- Benchmark reports and performance documentation.

## Allowed files

- `docs/verification/`
- Performance solver docs.
- Harness report module.
- Benchmark examples.

## Forbidden files

- Solver optimization changes.
- Unbacked performance claims.

## Required behavior

- Produce documented benchmark evidence comparing the simple solver,
  performance solver, and available third-party solvers.
- Include correctness validation, machine metadata, versions, options, timing,
  and failure records.
- State limitations plainly.

## Required tests

- Report output tests for required evidence fields.
- Benchmark smoke command recorded in docs.

## Required docs

- Update benchmark suite, solver comparison, and performance solver docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke --console=plain
```
