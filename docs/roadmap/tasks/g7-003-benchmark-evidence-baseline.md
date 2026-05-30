# G7-003 benchmark evidence baseline

## Status

proposed

## Requirement IDs

- REQ-0-1-BENCHMARK-EVIDENCE

## References

- `docs/verification/benchmark-suite.md`
- `docs/verification/solver-comparison.md`
- `docs/adr/ADR-0004-result-validation-before-performance-claims.md`

## Target module

- Benchmark and report documentation.

## Allowed files

- `docs/verification/benchmark-suite.md`
- `docs/verification/solver-comparison.md`
- `docs/architecture/result-validation.md`
- Harness report docs.

## Forbidden files

- Solver implementation modules.
- Performance claim text not backed by evidence.

## Required behavior

- Define generated and small-public benchmark evidence required before any
  performance claim.
- Define required report fields for correctness, timing, machine metadata,
  solver versions, options, and failure records.
- Keep the evidence baseline modest enough for local smoke runs.

## Required tests

- No production tests required; this is a documentation baseline task.
- Confirm later benchmark and performance tasks reference this evidence policy.

## Required docs

- Update benchmark suite, solver comparison, and validation docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
