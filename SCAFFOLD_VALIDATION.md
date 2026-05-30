# Scaffold validation

This document records what this initial scaffold is expected to provide.

## Expected root files

- `README.md`
- `AGENT.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `LICENSE`
- `NOTICE.md`
- `settings.gradle`
- `build.gradle`
- `gradle.properties`
- `SCAFFOLD_VALIDATION.md`

## Expected directories

- `.github/workflows/`
- `build-logic/`
- `config/`
- `docs/`
- `examples/`
- `modules/`
- `tools/`

## Expected root tasks

- `validateDesignControlPack`
- `checkAll`
- `qualityGate`
- `nativeSmoke`
- `solverComparisonSmoke`
- `benchmarkSmoke`
- `printPublishedArtifacts`

## G0 assertion

G0 provides the repository shape and test-harness skeleton only. It does not
implement a production LP solver.
