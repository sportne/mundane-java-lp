# G4-004 testkit simplification pass

## Status

proposed

## Requirement IDs

- REQ-0-1-TESTKIT-SIMPLIFICATION

## References

- `docs/charter.md`
- `docs/verification/correctness-suite.md`

## Target module

- `modules/lp-testkit`
- Test sources across modules.

## Allowed files

- `modules/lp-testkit/`
- `modules/*/src/test/`
- `examples/*/src/test/`
- Relevant module READMEs.

## Forbidden files

- Production behavior changes outside `lp-testkit`.
- Solver algorithm implementation.

## Required behavior

- Consolidate duplicated fixture and assertion helpers.
- Prefer fewer lines and clearer test setup over helper abstraction.
- Remove unused fixture helpers and stale README claims.

## Required tests

- Existing test suites must keep passing.
- Review diff for net simplification in test setup where practical.

## Required docs

- Update `lp-testkit` README and any affected test documentation.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
