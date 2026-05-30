# G1-003 MPS format design completion

## Status

proposed

## Requirement IDs

- REQ-V1-MPS-DESIGN

## References

- `docs/adr/ADR-0003-mps-as-initial-interchange-format.md`
- `docs/standards-baseline.md`
- `docs/architecture/lp-model-contract.md`

## Target module

- `modules/lp-io-mps`

## Allowed files

- `docs/standards-baseline.md`
- `docs/architecture/lp-model-contract.md`
- `modules/lp-io-mps/README.md`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Solver and adapter modules.

## Required behavior

- Define the v1.0 supported MPS subset and explicitly unsupported features.
- Define deterministic diagnostics for malformed or unsupported input.
- Define round-trip expectations between MPS and canonical LP fixtures.

## Required tests

- No production tests required; this is a design task.
- Confirm later MPS fixture tasks reference the supported subset.

## Required docs

- Update standards baseline and MPS module README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
