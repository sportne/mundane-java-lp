# G7-002 public benchmark curation

## Status

proposed

## Requirement IDs

- REQ-0-1-PUBLIC-BENCHMARK-CURATION

## References

- `docs/verification/public-benchmark-sources.md`
- `docs/verification/benchmark-suite.md`
- `docs/roadmap/tasks/g7-001-generated-fixture-baseline.md`

## Target module

- Benchmark documentation and tooling.

## Allowed files

- `docs/verification/public-benchmark-sources.md`
- `docs/verification/benchmark-suite.md`
- `tools/`
- `instances/README.md`

## Forbidden files

- Vendored public benchmark instances.
- Solver implementation modules.

## Required behavior

- Choose a small public benchmark set for 0.1.0 comparison evidence.
- Record upstream URL, license or redistribution terms, download date,
  checksum, and normalization notes.
- Keep downloaded benchmark files out of git.

## Required tests

- Verify metadata schema or manifest examples are deterministic.
- Confirm `.gitignore` keeps public benchmark downloads untracked.

## Required docs

- Update public benchmark source docs, benchmark suite docs, and instance docs.

## Acceptance command

```bash
./gradlew validateDesignControlPack qualityGate --console=plain
```
