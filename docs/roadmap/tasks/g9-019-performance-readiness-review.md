# G9-019 performance readiness review

## Status

proposed

## Requirement IDs

- REQ-0-1-PERFORMANCE-READINESS

## References

- `docs/roadmap/tasks/g9-017-performance-evidence-report.md`
- `docs/roadmap/tasks/g9-018-performance-code-simplification-pass.md`
- `docs/charter.md`

## Target module

- Performance solver module and release documentation.

## Allowed files

- Performance solver docs.
- `docs/verification/`
- `docs/roadmap/roadmap-index.md`
- Release docs.

## Forbidden files

- New solver features.
- New benchmark families.

## Required behavior

- Decide whether the performance solver meets the 0.1.0 credible-but-modest
  bar.
- Record supported cases, unsupported cases, evidence, and known limitations.
- Defer unmet ambitions to future roadmap tasks instead of expanding 0.1.0.

## Required tests

- Run correctness, solver comparison, and benchmark smoke lanes.
- Confirm readiness docs match recorded evidence.

## Required docs

- Update performance solver docs, verification docs, and roadmap status.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke --console=plain
```
