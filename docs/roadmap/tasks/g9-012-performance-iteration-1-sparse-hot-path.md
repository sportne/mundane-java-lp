# G9-012 performance iteration 1 sparse hot path

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-ITERATION-1

## References

- `docs/roadmap/tasks/g9-011-performance-solver-profiling-baseline.md`
- `docs/architecture/sparse-matrix-contract.md`
- `docs/literature/sparse-linear-algebra.md`

## Target module

- Performance solver module.
- `modules/lp-sparse`

## Allowed files

- Performance solver module.
- `modules/lp-sparse/`
- Related tests and docs.

## Forbidden files

- Third-party adapter behavior.
- Unmeasured broad rewrites.

## Required behavior

- Improve the sparse matrix or solver hot path selected by profiling evidence.
- Preserve correctness and primitive-array storage.
- Keep changes narrow and measured.

## Required tests

- Sparse and solver correctness tests.
- Benchmark smoke comparing before and after evidence where practical.

## Required docs

- Update profiling or performance notes with measured impact and tradeoffs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
