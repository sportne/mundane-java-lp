# G9-014 performance iteration 2 algorithmic bottleneck

## Status

proposed

## Requirement IDs

- REQ-0-1-PERFORMANCE-ITERATION-2

## References

- `docs/roadmap/tasks/g9-013-iteration-1-correctness-and-simplification.md`
- `docs/roadmap/tasks/g9-011-performance-solver-profiling-baseline.md`
- Relevant solver algorithm ADR.

## Target module

- Performance solver module.

## Allowed files

- Performance solver module.
- Solver tests and docs.
- Literature or ADR docs if the selected bottleneck changes design.

## Forbidden files

- Third-party adapter behavior.
- Public performance claims without evidence.

## Required behavior

- Improve the highest-impact algorithmic bottleneck selected from recorded
  evidence.
- Keep the implementation within the approved performance solver design.
- Document why the change is worth its complexity.

## Required tests

- Correctness tests for affected solver paths.
- Benchmark smoke with recorded comparison evidence.

## Required docs

- Update performance solver notes, literature notes, or ADRs as needed.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
