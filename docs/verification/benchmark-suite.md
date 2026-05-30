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

## Default Lanes

The default local lane is the quality gate. Solver comparison, benchmark, and
native lanes are separate tasks so optional external tools do not affect normal
development validation.
