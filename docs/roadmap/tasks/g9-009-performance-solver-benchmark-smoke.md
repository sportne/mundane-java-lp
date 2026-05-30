# G9-009 performance solver benchmark smoke

## Status

proposed

## Requirement IDs

- REQ-0-1-PERFORMANCE-SOLVER-BENCHMARK-SMOKE

## References

- `docs/verification/benchmark-suite.md`
- `docs/verification/solver-comparison.md`
- `docs/roadmap/tasks/g9-008-performance-solver-correctness.md`

## Target module

- Performance solver module.
- `modules/lp-harness-cli`
- `examples/solver-comparison-smoke`

## Allowed files

- Performance solver module.
- `modules/lp-harness-cli/`
- `examples/solver-comparison-smoke/`
- Benchmark and solver comparison docs.

## Forbidden files

- Third-party adapter internals except registration or smoke wiring.
- Public performance claims.

## Required behavior

- Add small benchmark smoke comparisons for the performance solver.
- Report timing and validation evidence without claiming superiority.
- Keep benchmark fixtures small enough for normal local development.

## Required tests

- Benchmark smoke test that runs without external solvers.
- Solver comparison smoke test that includes the performance solver when
  available.

## Required docs

- Update benchmark suite, solver comparison, and verification strategy docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke --console=plain
```
