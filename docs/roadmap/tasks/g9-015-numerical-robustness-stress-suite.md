# G9-015 numerical robustness stress suite

## Status

complete

## Requirement IDs

- REQ-0-1-NUMERICAL-ROBUSTNESS-STRESS

## References

- `docs/literature/numerical-stability.md`
- `docs/verification/correctness-suite.md`
- `docs/verification/generated-instance-families.md`

## Target module

- `modules/lp-generators`
- `modules/lp-validation`
- Performance solver module.

## Allowed files

- `modules/lp-generators/`
- `modules/lp-validation/`
- Performance solver tests.
- Numerical stability and verification docs.

## Forbidden files

- Third-party adapter behavior.
- Benchmark claims.

## Required behavior

- Add generated numerical stress fixtures for scaling, degeneracy, tight
  bounds, and ill-conditioning.
- Define validation expectations and tolerated unsupported cases.
- Keep stress fixtures deterministic and bounded for local runs.

## Required tests

- Generator determinism tests.
- Validation and solver tests for accepted, rejected, and unsupported stress
  cases.

## Required docs

- Update numerical stability, generated instance, and correctness docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke --console=plain
```
