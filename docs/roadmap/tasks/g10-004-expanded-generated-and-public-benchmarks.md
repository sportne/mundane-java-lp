# G10-004 expanded generated and public benchmarks

## Status

proposed

## Requirement IDs

- REQ-0-1-EXPANDED-BENCHMARK-SUITE

## References

- `docs/roadmap/tasks/g7-001-generated-fixture-baseline.md`
- `docs/roadmap/tasks/g7-002-public-benchmark-curation.md`
- `docs/roadmap/tasks/g7-004-performance-benchmark-suite-baseline.md`
- `docs/verification/benchmark-suite.md`

## Target module

- `modules/lp-generators`
- `modules/lp-harness-cli`
- Benchmark manifests and verification docs.

## Allowed files

- `modules/lp-generators/`
- `modules/lp-harness-cli/`
- `modules/lp-testkit/`
- Benchmark manifests.
- `docs/verification/`

## Forbidden files

- Vendored public benchmark files.
- Solver implementation changes.
- Performance conclusions without accepted validation evidence.

## Required behavior

- Add generated benchmark families that cover small dense, sparse, network-like,
  equality-heavy, degenerate, and scaled cases.
- Make generated fixtures deterministic and carry provenance, size parameters,
  seed, expected status, and validation evidence where available.
- Add a public benchmark acquisition/verification workflow for the curated
  Netlib candidates without committing public instance files.
- Distinguish missing local public files from failed public benchmark runs.
- Keep local defaults bounded while allowing a fuller benchmark profile in CI or
  explicit developer runs.

## Required tests

- Generator determinism tests.
- Manifest checksum and missing-input tests.
- Harness tests for mixed generated and public benchmark records.

## Required docs

- Update generated instance, public benchmark, and benchmark suite docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate benchmarkSmoke expandedBenchmarkSuite --console=plain
```
