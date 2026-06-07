# Changelog

## 0.1.0

Initial release of the mundane Java LP design-control baseline.

### Added

- Java 21 / Gradle multi-module LP harness with design-control documentation,
  architecture tests, quality gates, and release metadata.
- Canonical continuous LP model, primitive sparse matrix storage, validation
  engine, supported MPS read/write subset, and deterministic fixtures.
- Solver adapter SPI with CLI adapters for HiGHS, CLP, and GLPK; Java-library
  adapters for OR-Tools and ojAlgo; and in-project simple and
  performance-oriented experimental solvers.
- Deterministic Markdown, JSON, and CSV reporting for solver comparison and
  benchmark lanes, including solver identity, options, validation, timing, and
  machine metadata fields.
- Explicit verification lanes for local quality, solver comparison smoke,
  benchmark smoke, native executable smoke, strict solver comparison, expanded
  benchmark evidence, and profiling.

### Release Boundaries

- Supported solver comparison targets are HiGHS, CLP, GLPK, OR-Tools, ojAlgo,
  the in-project simple solver, and the in-project performance-oriented solver.
- The supported file interchange path is the documented 0.1.0 MPS subset.
  LP text I/O remains outside the release surface.
- Native support covers GraalVM Native Build Tools executable smoke behavior.
  A shared-library C ABI is outside the release surface.
- Public Netlib benchmark candidates are curated and checksum-verified, but
  they do not support 0.1.0 runtime comparisons because the current MPS reader
  cannot load those full files.
- Benchmark reports are evidence collection only. This release makes no solver
  speed, robustness, scalability, or commercial-solver replacement claim.

### Published Artifacts

All artifacts use group `io.github.mundanej` and version `0.1.0`:

- `lp-bom`
- `lp-model`
- `lp-sparse`
- `lp-io-mps`
- `lp-io-lp`
- `lp-validation`
- `lp-generators`
- `lp-solver-spi`
- `lp-solver-simple`
- `lp-solver-performance`
- `lp-harness-api`
- `lp-harness-cli`
- `lp-harness-report`
- `lp-adapter-highs-cli`
- `lp-adapter-clp-cli`
- `lp-adapter-glpk-cli`
- `lp-adapter-ortools-java`
- `lp-adapter-ojalgo`
- `lp-native-api`
- `lp-testkit`

### Verification

The local release-readiness command is:

```bash
./gradlew validateDesignControlPack qualityGate solverComparisonSmoke benchmarkSmoke nativeSmoke printPublishedArtifacts --console=plain
```
