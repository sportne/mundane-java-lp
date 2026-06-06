# G10-007 real performance evidence report

## Status

complete

## Requirement IDs

- REQ-0-1-REAL-PERFORMANCE-EVIDENCE

## References

- `docs/roadmap/tasks/g10-001-solver-toolchain-provisioning.md`
- `docs/roadmap/tasks/g10-002-strict-solver-availability-gate.md`
- `docs/roadmap/tasks/g10-003-expanded-correctness-comparison-suite.md`
- `docs/roadmap/tasks/g10-004-expanded-generated-and-public-benchmarks.md`
- `docs/roadmap/tasks/g10-005-benchmark-methodology-and-statistics.md`
- `docs/roadmap/tasks/g10-006-profiler-tooling-and-hotspot-workflow.md`
- `docs/verification/performance-evidence-report.md`

## Target module

- Performance evidence documentation.
- Benchmark report artifacts.

## Allowed files

- `docs/verification/`
- Benchmark report fixtures or golden outputs.
- Roadmap status files.

## Forbidden files

- Solver implementation changes.
- New benchmark families.
- Unvalidated performance claims.

## Required behavior

- Replace the current smoke-level performance evidence report with a real
  evidence report from the strict solver and expanded benchmark lanes.
- Include all required solvers: HiGHS, CLP, GLPK, OR-Tools, ojAlgo, simple
  solver, and performance solver.
- Include solver versions, binary paths or dependency versions, solver options,
  machine metadata, validation results, timing statistics, unavailable/failure
  records, and limitations.
- Include profiler artifact references or explicit `not-profiled` diagnostics
  for runs where profiler evidence is unavailable.
- Clearly separate observed evidence from conclusions.
- If any required solver or benchmark lane is unavailable, record the release
  evidence as incomplete instead of treating smoke evidence as sufficient.

## Required tests

- Report tests proving required evidence fields are present.
- Verification that strict solver and expanded benchmark reports exist before
  readiness docs can mark evidence complete.

## Required docs

- Update performance evidence, solver comparison, benchmark suite, and
  performance readiness docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate strictSolverComparison expandedBenchmarkSuite --console=plain
```
