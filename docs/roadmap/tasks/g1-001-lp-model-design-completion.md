# G1-001 LP model design completion

## Status

proposed

## Requirement IDs

- REQ-0-1-LP-MODEL-DESIGN

## References

- `docs/architecture/lp-model-contract.md`
- `docs/standards-baseline.md`
- `docs/verification/correctness-suite.md`

## Target module

- `modules/lp-model`

## Allowed files

- `docs/architecture/lp-model-contract.md`
- `docs/standards-baseline.md`
- `modules/lp-model/README.md`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Solver and adapter modules.

## Required behavior

- Finalize the canonical LP model contract for 0.1.0.
- Define objective sense, constants, variable bounds, row bounds, matrix shape,
  names, and invalid fixture policy.
- Keep the model small and independent of solver, harness, and I/O behavior.

## Required tests

- No production tests required; this is a design task.
- Confirm documentation describes each invariant currently enforced or planned
  before implementation.

## Required docs

- Update LP model contract and module README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
