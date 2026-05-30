# G3-008 solver comparison smoke

## Status

proposed

## Requirement IDs

- REQ-V1-SOLVER-COMPARISON-SMOKE

## References

- `docs/verification/solver-comparison.md`
- `docs/verification/verification-strategy.md`
- `docs/roadmap/tasks/g3-001-harness-execution-model.md`

## Target module

- `examples/solver-comparison-smoke`
- `modules/lp-harness-cli`

## Allowed files

- `examples/solver-comparison-smoke/`
- `modules/lp-harness-cli/`
- `modules/lp-harness-report/`
- Solver comparison docs.

## Forbidden files

- Adapter internals except registration or smoke wiring.
- In-project solver implementation.

## Required behavior

- Make `solverComparisonSmoke` run the tiny comparison suite across available
  third-party adapters.
- Report unavailable solvers explicitly instead of failing or omitting them.
- Validate outputs independently before reporting acceptance.

## Required tests

- Smoke tests for all-unavailable and at-least-one-available scenarios using
  controllable test doubles where practical.
- Acceptance lane must pass on machines without external solvers installed.

## Required docs

- Update solver comparison and verification strategy docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
