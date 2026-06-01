# G9-016 performance iteration 3 robustness and scaling

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-ITERATION-3

## References

- `docs/roadmap/tasks/g9-015-numerical-robustness-stress-suite.md`
- `docs/roadmap/tasks/g7-004-performance-benchmark-suite-baseline.md`
- `docs/literature/numerical-stability.md`

## Target module

- Performance solver module.

## Allowed files

- Performance solver module.
- Solver tests.
- Performance and numerical docs.

## Forbidden files

- Third-party adapter behavior.
- New broad solver architecture without an ADR.

## Required behavior

- Improve robustness and scaling on generated and small public benchmark cases.
- Preserve correctness on all supported fixtures.
- Record remaining limitations explicitly.

## Required tests

- Correctness and numerical stress tests.
- Benchmark smoke with generated and curated small public cases where available.

## Required docs

- Update performance solver docs and numerical stability notes.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
