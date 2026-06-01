# G10-007 evidence readiness review

## Status

proposed

## Requirement IDs

- REQ-0-1-EVIDENCE-READINESS

## References

- `docs/roadmap/tasks/g10-006-real-performance-evidence-report.md`
- `docs/roadmap/tasks/g9-019-performance-readiness-review.md`
- `docs/charter.md`

## Target module

- Verification and release documentation.

## Allowed files

- `docs/verification/`
- `docs/roadmap/roadmap-index.md`
- Release docs.

## Forbidden files

- Solver implementation changes.
- New benchmark families.
- New claims not backed by G10 evidence.

## Required behavior

- Decide whether the project has enough solver-comparison and benchmark
  evidence to enter release hardening.
- Distinguish implementation readiness from benchmark-evidence readiness.
- Record any missing solver, benchmark, validation, methodology, or reporting
  evidence as a release blocker.
- Update the roadmap status only after strict solver and expanded benchmark
  lanes pass.

## Required tests

- Run strict solver comparison and expanded benchmark lanes.
- Confirm readiness docs match generated reports.

## Required docs

- Update performance readiness, verification strategy, and roadmap status.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate strictSolverComparison expandedBenchmarkSuite --console=plain
```
