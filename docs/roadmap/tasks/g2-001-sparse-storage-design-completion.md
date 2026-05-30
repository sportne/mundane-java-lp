# G2-001 sparse storage design completion

## Status

complete

## Requirement IDs

- REQ-0-1-SPARSE-DESIGN

## References

- `docs/architecture/sparse-matrix-contract.md`
- `docs/literature/sparse-linear-algebra.md`
- `docs/architecture/architecture-rule-catalog.md`

## Target module

- `modules/lp-sparse`

## Allowed files

- `docs/architecture/sparse-matrix-contract.md`
- `docs/literature/sparse-linear-algebra.md`
- `modules/lp-sparse/README.md`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Solver and adapter modules.

## Required behavior

- Finalize CSR, CSC, and builder policy for 0.1.0.
- Define primitive-array invariants, copy policy, index ordering policy, and
  expected matrix-vector operations.
- Keep performance posture simple: no object-heavy sparse entries in hot paths.

## Required tests

- No production tests required; this is a design task.
- Confirm the architecture rule catalog names the enforcement target.

## Required docs

- Update sparse matrix contract, literature note, and module README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
