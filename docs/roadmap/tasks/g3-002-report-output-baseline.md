# G3-002 report output baseline

## Status

proposed

## Requirement IDs

- REQ-V1-REPORT-OUTPUT

## References

- `docs/verification/benchmark-suite.md`
- `docs/roadmap/tasks/g1-005-harness-design-completion.md`

## Target module

- `modules/lp-harness-report`

## Allowed files

- `modules/lp-harness-report/`
- `modules/lp-harness-api/`
- `docs/verification/benchmark-suite.md`
- Relevant README files.

## Forbidden files

- Solver and adapter modules.
- Benchmark claim text.

## Required behavior

- Implement Markdown, JSON, and CSV report outputs for v1.0 run records.
- Include solver ID, status, objective, validation result, timing, options, and
  machine metadata required by the benchmark suite design.
- Keep report formatting deterministic.

## Required tests

- Golden or structural tests for Markdown, JSON, and CSV outputs.
- Tests for empty, accepted, rejected, errored, and unavailable run records.

## Required docs

- Update benchmark suite docs and report module README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
