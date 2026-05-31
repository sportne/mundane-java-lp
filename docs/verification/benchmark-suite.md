# Benchmark suite

Benchmark runs must report:

- instance ID and source;
- generator parameters where applicable;
- solver ID and version;
- solver options;
- tolerance profile;
- parse time;
- canonicalization/export time;
- solve time;
- validation time;
- total wall time;
- peak memory where measured;
- objective and residuals;
- status and termination reason;
- machine metadata.

Do not combine parsing/export time with solve time without also reporting them
separately.

## Harness Input Policy

Suites use explicit instance lists. Each instance must have a stable ID,
canonical model metadata, sparse coefficients, names needed by file formats, and
expected validation evidence. Solver lists are explicit and ordered; the harness
does not discover solvers automatically.

## Public Benchmark Curation

Public benchmark suites use metadata manifests instead of vendored instance
files. The 0.1.0 curated candidate set is documented in
`docs/verification/public-benchmark-sources.md` and represented by
`instances/public/manifest.example.json`.

Before a public instance can support performance evidence, its manifest entry
must record upstream URL, redistribution terms, download date, SHA-256 checksum,
format, local path, normalization notes, and curation status. Missing local files
are reported by benchmark smoke tooling as unavailable or missing input, not as
solver failures.

## Evidence Baseline

No benchmark output may be described as a performance claim unless every
solver-instance record includes the evidence below, or records a deterministic
reason that the evidence is unavailable:

- instance provenance: generated family metadata or public benchmark manifest
  entry with source URL, checksum, and local path;
- canonical model shape: rows, columns, nonzeros, objective sense, and bound
  categories;
- solver identity: solver ID, adapter mode, solver version when available, and
  unavailable-binary or unavailable-runtime diagnostics when absent;
- solver options: timeout, threads, and adapter-specific options used for the
  run;
- validation result: accepted/rejected status, finding codes, tolerance profile,
  expected status, expected objective when known, reported objective, and primal
  evidence availability;
- timing buckets: parse/load, canonicalization or export, solve, validation, and
  total wall time, using `not-measured` where a bucket is not implemented yet;
- machine metadata: operating system, architecture, Java version, and available
  processor count;
- failure records: adapter errors, validation failures, missing input,
  unsupported features, and unavailable solvers must remain in reports.

Generated fixtures may support correctness and smoke evidence as soon as they
carry deterministic expected evidence. Public benchmark entries require checksum
and local download provenance before they can support any comparative runtime
statement.

## Required Run Outcomes

Every solver-instance pair yields a run record:

- successful solver runs include solver status, objective when available,
  validation status, timings, options, and diagnostics;
- solver version, tolerance profile, residual summary fields, parse timing, and
  export timing are report fields even when the value is `not-measured`;
- unavailable solvers are recorded with an unavailable outcome and are not
  silently skipped;
- adapter exceptions and harness errors are recorded deterministically;
- reports preserve suite order and solver order.

0.1.0 report outputs are Markdown, JSON, and CSV. The formats are deterministic:
records are rendered in harness run order, unknown solver versions use
`not-measured`, and unavailable objective values render as blank CSV/Markdown
cells or JSON `null`.

## Default Lanes

The default local lane is the quality gate. Solver comparison, benchmark, and
native lanes are separate tasks so optional external tools do not affect normal
development validation.
