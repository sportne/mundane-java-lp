# G0-006 Javadoc quality baseline

## Status

proposed

## Requirement IDs

- REQ-V1-JAVADOC-BASELINE

## References

- `docs/architecture/architecture-rule-catalog.md`
- `docs/verification/verification-strategy.md`

## Target module

- Build conventions and public Java API documentation.

## Allowed files

- `build-logic/`
- `modules/`
- `examples/`
- `docs/architecture/architecture-rule-catalog.md`

## Forbidden files

- Solver algorithm implementation.
- Adapter behavior changes.

## Required behavior

- Define the project Javadoc baseline for public classes, methods, fields,
  parameters, records, and enum constants.
- Add the smallest practical enforcement that keeps Javadocs useful without
  making implementation noisy.
- Fix existing public API Javadoc gaps found by the baseline.

## Required tests

- Run all module Javadoc tasks through `qualityGate`.
- Add an architecture or source-shape check only if it stays simple and stable.

## Required docs

- Document the Javadoc baseline in architecture or contributor docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
