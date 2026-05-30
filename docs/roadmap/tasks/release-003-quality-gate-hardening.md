# release-003 quality gate hardening

## Status

proposed

## Requirement IDs

- REQ-0-1-QUALITY-GATES

## References

- `docs/verification/verification-strategy.md`
- `AGENT.md`
- `README.md`

## Target module

- Root build and verification lanes.

## Allowed files

- Root Gradle build files.
- `build-logic/`
- `.github/workflows/`
- Verification docs and README files.

## Forbidden files

- Solver algorithm changes.
- Adapter behavior changes except task wiring needed for verification lanes.

## Required behavior

- Ensure default, solver comparison, benchmark, and native lanes are documented
  and pass or skip unavailable external tooling cleanly.
- Keep local quality gate suitable for normal development.
- Avoid adding slow mandatory checks to the default lane.

## Required tests

- Run default quality gate.
- Run solver comparison smoke.
- Run benchmark smoke.
- Run native smoke and verify clean skip behavior when native tooling is absent.

## Required docs

- Update verification strategy, README, and AGENT acceptance command guidance.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke nativeSmoke --console=plain
```
