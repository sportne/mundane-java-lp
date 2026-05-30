# G6-003 ojAlgo adapter

## Status

proposed

## Requirement IDs

- REQ-0-1-OJALGO-ADAPTER

## References

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- `docs/roadmap/tasks/g6-001-java-adapter-design-completion.md`

## Target module

- `modules/lp-adapter-ojalgo`

## Allowed files

- `modules/lp-adapter-ojalgo/`
- `gradle/libs.versions.toml`
- Adapter docs and README.

## Forbidden files

- Other adapter modules.
- In-project solver modules.
- Foundation modules except for contract-driven tests.

## Required behavior

- Implement ojAlgo adapter through the solver SPI.
- Keep the ojAlgo dependency isolated to its adapter module.
- Normalize status, objective, primal values, timing, and unsupported-feature
  behavior.

## Required tests

- Unit tests for status normalization and unsupported-feature paths.
- Integration smoke test guarded so it skips cleanly when the dependency cannot
  solve a supported fixture.

## Required docs

- Update ojAlgo adapter README and solver comparison docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
