# G6-004 Java adapter comparison smoke

## Status

complete

## Requirement IDs

- REQ-0-1-JAVA-ADAPTER-COMPARISON-SMOKE

## References

- `docs/roadmap/tasks/g6-002-ortools-java-adapter.md`
- `docs/roadmap/tasks/g6-003-ojalgo-adapter.md`
- `docs/verification/solver-comparison.md`

## Target module

- `examples/solver-comparison-smoke`
- `modules/lp-harness-cli`

## Allowed files

- `examples/solver-comparison-smoke/`
- `modules/lp-harness-cli/`
- `modules/lp-harness-report/`
- Solver comparison docs.

## Forbidden files

- CLI adapter internals.
- In-project solver implementation.

## Required behavior

- Include OR-Tools and ojAlgo in solver comparison smoke when available.
- Report unavailable Java adapters cleanly and consistently with CLI adapters.
- Validate Java adapter outputs independently before reporting acceptance.

## Required tests

- Smoke tests for unavailable Java adapters.
- Comparison smoke tests using controllable adapter doubles where practical.

## Required docs

- Update solver comparison and verification strategy docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
