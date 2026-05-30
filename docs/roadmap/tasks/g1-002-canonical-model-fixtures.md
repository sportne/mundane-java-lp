# G1-002 canonical model fixtures

## Status

proposed

## Requirement IDs

- REQ-0-1-CANONICAL-FIXTURES

## References

- `docs/architecture/lp-model-contract.md`
- `docs/verification/correctness-suite.md`
- `docs/roadmap/tasks/g1-001-lp-model-design-completion.md`

## Target module

- `modules/lp-generators`
- `modules/lp-testkit`
- `modules/lp-model`

## Allowed files

- `modules/lp-generators/`
- `modules/lp-testkit/`
- `modules/lp-model/src/test/`
- Relevant module READMEs.

## Forbidden files

- Solver and adapter modules.
- MPS parser implementation.

## Required behavior

- Implement hand-checkable canonical LP fixtures for Tier 1 correctness cases.
- Include expected objective values, primal vectors, and known status evidence
  where the design requires it.
- Keep fixture construction direct and small.

## Required tests

- Unit tests for every Tier 1 fixture shape.
- Model invariant tests for invalid fixture rejection.

## Required docs

- Update correctness suite and fixture module READMEs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
