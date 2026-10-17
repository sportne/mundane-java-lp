# October 2026 maintenance

## Status

Owner-authorized maintenance in progress; verification results are recorded in the pull request.

## Requirement IDs

- REQ-0-1-QUALITY-GATES

## References

- Existing `release-003` quality-gate contract and `verification-strategy.md`.

## Target module

- Root CI and build conventions; no solver implementation changes.

## Allowed files

- `.github/workflows/ci.yml`, `build-logic/build.gradle`, and these verification documents.

## Forbidden files

- Solver algorithms, model/validation semantics, public API, benchmark source data and hashes.

## Required behavior

- Run the existing default quality gate independently of network-dependent solver evidence.
- Keep the strict solver/benchmark job failing on unavailable prerequisites.
- Refresh compatible build-plugin and test-runner patches.

## Required tests and documentation

- Existing default and optional smoke lanes; update the verification strategy to match CI.

## Acceptance command

`./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke nativeSmoke --console=plain`
