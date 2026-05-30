# G2-004 MPS round-trip fixtures

## Status

complete

## Requirement IDs

- REQ-0-1-MPS-ROUND-TRIP

## References

- `docs/adr/ADR-0003-mps-as-initial-interchange-format.md`
- `docs/roadmap/tasks/g2-003-mps-format-design-completion.md`
- `docs/verification/correctness-suite.md`

## Target module

- `modules/lp-io-mps`
- `modules/lp-testkit`

## Allowed files

- `modules/lp-io-mps/`
- `modules/lp-testkit/`
- `docs/verification/correctness-suite.md`
- Relevant module READMEs.

## Forbidden files

- Solver and adapter modules.
- Harness CLI behavior.

## Required behavior

- Implement MPS read/write round trips for the supported 0.1.0 subset.
- Use hand-checkable fixtures before public benchmark instances.
- Produce deterministic parse errors for unsupported or malformed files.

## Required tests

- Round-trip tests for Tier 1 fixtures that fit the supported MPS subset.
- Negative parser tests for missing required sections and unsupported features.

## Required docs

- Update MPS module README and correctness suite.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
