# G10-001 solver toolchain provisioning

## Status

complete

## Requirement IDs

- REQ-0-1-SOLVER-TOOLCHAIN-PROVISIONING

## References

- `docs/roadmap/tasks/g5-005-cli-solver-comparison-smoke.md`
- `docs/roadmap/tasks/g6-004-java-adapter-comparison-smoke.md`
- `docs/verification/solver-comparison.md`
- `docs/verification/performance-evidence-report.md`

## Target module

- CI configuration.
- Developer environment documentation.
- Solver adapter documentation.

## Allowed files

- `.github/workflows/`
- `docs/`
- `modules/lp-adapter-*/README.md`
- Build scripts required only for repeatable solver binary discovery.

## Forbidden files

- Solver algorithm changes.
- Benchmark result claims.
- Vendored solver binaries.

## Required behavior

- Define the supported 0.1.0 solver toolchain for HiGHS, CLP, GLPK, OR-Tools,
  ojAlgo, the simple solver, and the performance solver.
- Provide repeatable CI installation or setup for required external CLI solvers.
- Keep Java library solver dependencies pinned through Gradle.
- Document local developer setup for the same solver versions used by CI.
- Make missing optional local binaries diagnosable without hiding the stricter
  CI requirement.

## Required tests

- Add CI or build verification that reports discovered solver versions.
- Confirm each required solver can be invoked in the benchmark environment.
- Existing smoke lanes must keep passing.

## Required docs

- Update solver comparison docs with required solver versions and install paths.
- Update adapter READMEs with environment variables or discovery rules.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke --console=plain
```
