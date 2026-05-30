# G4-003 simple solver MPS path

## Status

proposed

## Requirement IDs

- REQ-V1-SIMPLE-SOLVER-MPS-PATH

## References

- `docs/roadmap/tasks/g4-002-simple-solver-correctness.md`
- `docs/roadmap/tasks/g2-003-mps-round-trip-fixtures.md`
- `docs/architecture/harness-architecture.md`

## Target module

- Simple solver module.
- `modules/lp-harness-cli`
- `modules/lp-io-mps`

## Allowed files

- Simple solver module.
- `modules/lp-harness-cli/`
- `modules/lp-io-mps/`
- `examples/tiny-lp/`
- Relevant docs and READMEs.

## Forbidden files

- Performance solver module.
- Third-party adapter internals.

## Required behavior

- Run the simple solver through the MPS import/export and harness path.
- Demonstrate a tiny MPS fixture producing a validated normalized result.
- Keep the path explicit; no automatic solver discovery.

## Required tests

- End-to-end test from MPS fixture to harness run record.
- CLI smoke test for the tiny MPS path.

## Required docs

- Update harness, MPS, and simple solver docs with the supported path.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
