# G1-004 validation design completion

## Status

proposed

## Requirement IDs

- REQ-V1-VALIDATION-DESIGN

## References

- `docs/adr/ADR-0004-result-validation-before-performance-claims.md`
- `docs/architecture/result-validation.md`
- `docs/verification/correctness-suite.md`

## Target module

- `modules/lp-validation`

## Allowed files

- `docs/architecture/result-validation.md`
- `docs/verification/correctness-suite.md`
- `modules/lp-validation/README.md`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Solver and adapter modules.

## Required behavior

- Define validation evidence for objective, variable bounds, row activity,
  status claims, dual residuals, and gaps.
- Define tolerance profiles and how findings are reported.
- Keep validation independent from solver-specific status text.

## Required tests

- No production tests required; this is a design task.
- Confirm later validation fixture tasks map to every v1.0 validation dimension.

## Required docs

- Update validation architecture, correctness suite, and module README.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
