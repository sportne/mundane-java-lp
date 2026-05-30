# G5-003 CLP CLI adapter

## Status

proposed

## Requirement IDs

- REQ-0-1-CLP-CLI-ADAPTER

## References

- `docs/architecture/solver-adapter-contract.md`
- `docs/verification/solver-comparison.md`
- `docs/roadmap/tasks/g5-001-cli-adapter-design-completion.md`

## Target module

- `modules/lp-adapter-clp-cli`

## Allowed files

- `modules/lp-adapter-clp-cli/`
- `modules/lp-solver-spi/`
- Adapter docs and README.

## Forbidden files

- Other adapter modules.
- In-project solver modules.
- Core model behavior unless required by the adapter contract.

## Required behavior

- Implement CLP CLI invocation through the solver SPI.
- Export supported problems, apply timeout and thread options where supported,
  capture bounded logs, normalize status, and report unavailable binaries.
- Do not fail unrelated quality gates when CLP is not installed.

## Required tests

- Unit tests for command construction and status parsing.
- Tests for unavailable binary behavior.
- Integration smoke test guarded so it skips cleanly when CLP is absent.

## Required docs

- Update CLP adapter README and solver comparison docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
