# G2-002 sparse matrix test coverage

## Status

proposed

## Requirement IDs

- REQ-0-1-SPARSE-TEST-COVERAGE

## References

- `docs/architecture/sparse-matrix-contract.md`
- `docs/roadmap/tasks/g2-001-sparse-storage-design-completion.md`

## Target module

- `modules/lp-sparse`

## Allowed files

- `modules/lp-sparse/src/main/`
- `modules/lp-sparse/src/test/`
- `modules/lp-sparse/README.md`

## Forbidden files

- Solver and adapter modules.
- Harness modules.

## Required behavior

- Complete correctness coverage for CSR and CSC construction, validation, and
  matrix-vector multiplication.
- Add only operations required by 0.1.0 fixtures, validation, or solvers.
- Preserve primitive-array storage.

## Required tests

- Unit tests for empty, single-entry, rectangular, invalid pointer, invalid
  index, and vector length mismatch cases.
- Architecture tests for primitive storage if not already covered.

## Required docs

- Update sparse matrix contract and module README if behavior changes.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
