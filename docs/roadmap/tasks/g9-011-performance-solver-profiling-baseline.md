# G9-011 performance solver profiling baseline

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-PROFILING-BASELINE

## References

- `docs/roadmap/tasks/g9-010-performance-measurement-instrumentation.md`
- `docs/roadmap/tasks/g7-004-performance-benchmark-suite-baseline.md`

## Target module

- Performance solver module.
- Benchmark documentation.

## Allowed files

- Performance solver docs.
- Benchmark docs.
- Profiling notes under `docs/`.

## Forbidden files

- Solver optimization changes.
- Benchmark fixture expansion.

## Required behavior

- Profile the first performance solver before optimizing it.
- Document top bottlenecks, measurement setup, benchmark cases, and evidence.
- Select the first optimization target from recorded data.

## Required tests

- Run benchmark smoke and record the command used for profiling.
- Confirm profiling notes reference generated or curated benchmark cases.

## Required docs

- Add profiling baseline notes and update performance solver docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
