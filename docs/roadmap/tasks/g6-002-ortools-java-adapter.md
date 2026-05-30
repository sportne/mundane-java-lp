# G6-002 OR-Tools Java adapter

## Status

proposed

## Requirement IDs

- REQ-0-1-ORTOOLS-JAVA-ADAPTER

## References

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- `docs/roadmap/tasks/g6-001-java-adapter-design-completion.md`

## Target module

- `modules/lp-adapter-ortools-java`

## Allowed files

- `modules/lp-adapter-ortools-java/`
- `gradle/libs.versions.toml`
- Adapter docs and README.

## Forbidden files

- Other adapter modules.
- In-project solver modules.
- Foundation modules except for contract-driven tests.

## Required behavior

- Implement OR-Tools Java adapter through the solver SPI.
- Keep the OR-Tools dependency isolated to its adapter module.
- Normalize status, objective, primal values, timing, and unsupported-feature
  behavior.

## Required tests

- Unit tests for status normalization and unsupported-feature paths.
- Integration smoke test guarded so it skips cleanly when OR-Tools dependency
  or native runtime is unavailable.

## Required docs

- Update OR-Tools adapter README and solver comparison docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
