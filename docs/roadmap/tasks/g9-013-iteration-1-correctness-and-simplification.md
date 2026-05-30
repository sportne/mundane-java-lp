# G9-013 iteration 1 correctness and simplification

## Status

proposed

## Requirement IDs

- REQ-0-1-ITERATION-1-CORRECTNESS-SIMPLIFICATION

## References

- `docs/roadmap/tasks/g9-012-performance-iteration-1-sparse-hot-path.md`
- `docs/verification/correctness-suite.md`
- `docs/charter.md`

## Target module

- Performance solver module.
- Solver tests.

## Allowed files

- Performance solver module.
- `modules/lp-testkit/`
- Solver docs and tests.

## Forbidden files

- Third-party adapter behavior.
- New benchmark families.

## Required behavior

- Re-run correctness after iteration 1 and fix regressions before continuing.
- Remove optimization scaffolding or helper code that did not prove useful.
- Prefer net simplification where possible.

## Required tests

- Full performance solver correctness tests.
- Benchmark smoke to confirm no obvious regression.

## Required docs

- Update performance solver notes with correctness and simplification outcomes.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
