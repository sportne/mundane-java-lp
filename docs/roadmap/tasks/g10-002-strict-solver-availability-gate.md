# G10-002 strict solver availability gate

## Status

complete

## Requirement IDs

- REQ-0-1-STRICT-SOLVER-AVAILABILITY

## References

- `docs/roadmap/tasks/g10-001-solver-toolchain-provisioning.md`
- `docs/verification/solver-comparison.md`
- `docs/architecture/harness-architecture.md`

## Target module

- `modules/lp-harness-cli`
- Solver comparison example.
- CI configuration.

## Allowed files

- `modules/lp-harness-cli/`
- `examples/solver-comparison-smoke/`
- `.github/workflows/`
- `docs/verification/`

## Forbidden files

- Solver implementation changes.
- Public performance claims.

## Required behavior

- Add a strict solver-comparison lane that fails when any required comparison
  solver is unavailable.
- Keep the existing smoke lane tolerant of unavailable optional local solvers.
- Make strict-vs-smoke behavior explicit in task names, console output, and
  report metadata.
- Record solver versions and binary paths in strict-lane reports.
- Ensure unavailable solvers remain explicit failure records instead of silent
  skips.

## Required tests

- Tests for all-required-solvers-available behavior using doubles.
- Tests that strict mode fails when a required solver is unavailable.
- Tests that smoke mode still passes when external solvers are unavailable.

## Required docs

- Update solver comparison and verification strategy docs with the strict lane.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke strictSolverComparison --console=plain
```
