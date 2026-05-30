# G7-001 generated fixture baseline

## Status

proposed

## Requirement IDs

- REQ-0-1-GENERATED-FIXTURES

## References

- `docs/verification/generated-instance-families.md`
- `docs/roadmap/tasks/g1-002-canonical-model-fixtures.md`

## Target module

- `modules/lp-generators`
- `examples/generated-network-flow`

## Allowed files

- `modules/lp-generators/`
- `examples/generated-network-flow/`
- `docs/verification/generated-instance-families.md`
- Relevant README files.

## Forbidden files

- Solver and adapter modules.
- Public benchmark ingestion.

## Required behavior

- Implement small deterministic generated LP families needed for 0.1.0 smoke
  tests.
- Record generator name, seed, size parameters, and expected evidence when
  known.
- Keep generated fixture APIs minimal.

## Required tests

- Unit tests for deterministic output by seed and parameters.
- Smoke test for at least one generated network-flow-like instance if included
  by the design.

## Required docs

- Update generated instance family docs and example README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
