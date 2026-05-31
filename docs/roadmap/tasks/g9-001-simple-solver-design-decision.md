# G9-001 simple solver design decision

## Status

complete

## Requirement IDs

- REQ-0-1-SIMPLE-SOLVER-DESIGN

## References

- `docs/charter.md`
- `docs/verification/correctness-suite.md`
- `docs/literature/revised-simplex.md`
- `docs/literature/first-order-methods.md`

## Target module

- Future simple solver module.

## Allowed files

- `docs/adr/`
- `docs/literature/`
- `docs/architecture/`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Gradle module registration.

## Required behavior

- Choose the simple correctness-first in-project solver approach for 0.1.0.
- Document why the approach is boring, small, and suitable for Tier 1
  correctness fixtures.
- Explicitly state performance non-goals for this solver.

## Required tests

- No production tests required; this is a design decision task.
- Confirm later simple solver tasks reference the decision.

## Required docs

- Add or update an ADR for the simple solver choice.
- Update relevant literature notes.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
