# G5-005 v1 release readiness

## Status

proposed

## Requirement IDs

- REQ-V1-RELEASE-READINESS

## References

- `README.md`
- `docs/charter.md`
- `docs/verification/verification-strategy.md`
- `docs/roadmap/roadmap-index.md`

## Target module

- Repository release metadata and documentation.

## Allowed files

- `README.md`
- `CHANGELOG.md` if introduced.
- Root Gradle version metadata.
- `docs/`
- `.github/workflows/`

## Forbidden files

- New solver features.
- New adapter features.
- Public API expansion.

## Required behavior

- Verify version metadata, artifact list, known limitations, and acceptance
  commands for the 1.0.0 release.
- Document supported solvers, supported LP/MPS subset, and benchmark claim
  limits.
- Ensure no release doc claims exceed recorded validation or benchmark evidence.

## Required tests

- Run all release acceptance commands from documentation.
- Confirm published artifact coordinates are correct.
- Confirm generated reports and docs describe known limitations.

## Required docs

- Update release-facing README, roadmap status, verification docs, and any
  changelog or release notes introduced by the project.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke nativeSmoke printPublishedArtifacts --console=plain
```
