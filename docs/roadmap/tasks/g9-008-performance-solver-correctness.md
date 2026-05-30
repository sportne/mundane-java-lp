# G9-008 performance solver correctness

## Status

proposed

## Requirement IDs

- REQ-0-1-PERFORMANCE-SOLVER-CORRECTNESS

## References

- `docs/roadmap/tasks/g9-007-performance-solver-core.md`
- `docs/verification/correctness-suite.md`
- `docs/architecture/result-validation.md`

## Target module

- Performance solver module.
- `modules/lp-validation`

## Allowed files

- Performance solver module.
- `modules/lp-testkit/`
- `modules/lp-validation/src/test/`
- Correctness docs and solver README.

## Forbidden files

- Third-party adapter behavior.
- Benchmark claim text.

## Required behavior

- Validate the performance solver against 0.1.0 correctness fixtures.
- Record accepted, rejected, infeasible, unbounded, and unsupported outcomes
  consistently with validation policy.
- Fix correctness defects before expanding benchmark coverage.

## Required tests

- Correctness tests for all 0.1.0 fixtures supported by the performance solver.
- Regression tests for numerical edge cases documented in literature notes.

## Required docs

- Update correctness suite and performance solver README with supported cases.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
