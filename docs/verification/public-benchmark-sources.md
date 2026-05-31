# Public benchmark sources

Public benchmark instances are not vendored in 0.1.0. Local copies live under
`instances/public/`, which is git-ignored. Only curation metadata and manifest
examples are committed.

## 0.1.0 Candidate Set

The initial public benchmark candidate set is a small Netlib LP subset:

- `netlib-afiro`
- `netlib-adlittle`
- `netlib-scorpion`

These are intentionally small enough for local smoke work once public-instance
loading is wired into the benchmark lane. Their upstream source is the Netlib LP
collection at `https://www.netlib.org/lp/`.

## Metadata Policy

Every public benchmark manifest entry records:

- stable instance ID;
- family or suite name;
- upstream source URL;
- license or redistribution terms;
- download date, or `not-downloaded` for candidate-only entries;
- SHA-256 checksum, or `pending-local-download` before download;
- source format;
- local path under `instances/public/`;
- normalization notes;
- curation status.

Downloaded benchmark files stay out of git until a future ADR explicitly
approves vendoring. Checksum values must be updated from local downloads before
using the instances for any performance evidence.

## Manifest Verification

`instances/public/manifest.example.json` documents the deterministic manifest
shape. Run:

```bash
tools/fetch-public-benchmarks.sh
```

The script verifies manifest schema and local-path policy. It does not download
benchmark files in 0.1.0.
