# G10-003 expanded correctness comparison suite

## Status

complete

## Requirement IDs

- REQ-0-1-EXPANDED-CORRECTNESS-COMPARISON

## References

- `docs/roadmap/tasks/g1-002-canonical-model-fixtures.md`
- `docs/roadmap/tasks/g3-002-validation-engine-fixtures.md`
- `docs/roadmap/tasks/g10-002-strict-solver-availability-gate.md`
- `docs/verification/correctness-suite.md`

## Target module

- `modules/lp-testkit`
- `modules/lp-validation`
- Harness comparison suites.

## Allowed files

- `modules/lp-testkit/`
- `modules/lp-validation/`
- `modules/lp-harness-cli/`
- Solver comparison examples.
- `docs/verification/`

## Forbidden files

- Solver-specific correctness shortcuts.
- Solver implementation changes except compile-only API adjustments.

## Required behavior

- Expand the comparison suite beyond the single smoke fixture.
- Run all Tier 1 canonical fixtures through every solver that supports the
  required model shape.
- Record deterministic unsupported outcomes for solvers whose documented subset
  does not cover a fixture.
- Add MPS round-trip comparison cases for fixtures in the supported MPS subset.
- Include objective, primal, row-activity, and status validation for every
  solved fixture.

## Required tests

- Harness tests for multi-instance, multi-solver comparison ordering.
- Validation tests for mixed optimal, infeasible, unbounded, unsupported, and
  unavailable records.
- Regression tests proving unsupported solver subsets do not count as accepted
  solves.

## Required docs

- Update correctness-suite and solver-comparison docs with the expanded matrix.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate strictSolverComparison --console=plain
```
