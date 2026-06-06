# Public benchmark sources

Public benchmark instances are not vendored in 0.1.0. Local copies live under
`instances/public/`, which is git-ignored. Only curation metadata and manifest
examples are committed.

## 0.1.0 Candidate Set

The initial public benchmark candidate set is a small Netlib LP subset:

- `netlib-afiro`
- `netlib-adlittle`
- `netlib-scorpion`

These are intentionally small enough for local smoke work and CI evidence
collection. The benchmark lanes read the manifest, check each local path, and
report a deterministic missing-input record when a candidate has not been
downloaded. When a local MPS file is present, the lane parses it through the
supported MPS reader and records the instance as loaded without requiring an
external benchmark solver.

## Metadata Policy

Every public benchmark manifest entry records:

- stable instance ID;
- family or suite name;
- upstream source URL;
- license or redistribution terms;
- upstream content verification date;
- SHA-256 checksum;
- source format;
- local path under `instances/public/`;
- normalization notes;
- curation status.

Downloaded benchmark files stay out of git until a future ADR explicitly
approves vendoring.

## Manifest Verification

`instances/public/manifest.example.json` documents the deterministic manifest
shape. Run:

```bash
tools/fetch-public-benchmarks.sh
```

The script verifies manifest schema, local-path policy, and checksums for any
present local files. To acquire the curated files for CI or explicit evidence
runs:

```bash
tools/fetch-public-benchmarks.sh --download
```

Downloaded benchmark files remain untracked under `instances/public/`.
