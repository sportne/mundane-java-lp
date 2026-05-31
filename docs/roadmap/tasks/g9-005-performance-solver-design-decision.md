# G9-005 performance solver design decision

## Status

complete

## Requirement IDs

- REQ-0-1-PERFORMANCE-SOLVER-DESIGN

## References

- `docs/charter.md`
- `docs/literature/interior-point-methods.md`
- `docs/literature/first-order-methods.md`
- `docs/literature/sparse-linear-algebra.md`
- `docs/literature/numerical-stability.md`

## Target module

- Future performance solver module.

## Allowed files

- `docs/adr/`
- `docs/literature/`
- `docs/architecture/`
- `docs/roadmap/tasks/`

## Forbidden files

- Production Java source.
- Gradle module registration.

## Required behavior

- Choose the performance-oriented in-project solver approach for 0.1.0.
- Document sparse data flow, numerical risks, expected limits, and benchmark
  evidence needed before performance claims.
- Keep the first implementation narrow enough to remain maintainable.

## Required tests

- No production tests required; this is a design decision task.
- Confirm later performance solver tasks reference the decision.

## Required docs

- Add or update an ADR for the performance solver choice.
- Update relevant literature notes.
- Confirm G9-006 and later performance-solver tasks reference the accepted
  decision.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
