# Solver adapter contract

A solver adapter takes a solver input envelope, solver options, and a work
directory. It returns a normalized result. It must not assume that
solver-reported status is accepted without validation.

## Solver Input Envelope

The 0.1.0 SPI input contains:

- canonical `LpProblem` metadata;
- CSR row-by-column coefficient matrix;
- row names in matrix row order;
- column names in variable order;
- objective row name for file formats that need one.

Adapters may reject unsupported model features with `UNSUPPORTED`, but they must
not silently reinterpret the model. CLI adapters export the input envelope to a
solver-owned file in their per-run work directory.

## Adapter Responsibilities

- Record solver ID and version when available.
- Record command line or invocation mode in diagnostics.
- Apply timeout and thread options where supported.
- Capture stdout/stderr logs in a bounded diagnostic string.
- Normalize status into the shared SPI status enum.
- Return objective and primal values when available.
- Report unsupported features and unavailable binaries explicitly.

## Result Evidence

The 0.1.0 `SolverRunResult` is solver evidence, not a validation verdict. It
contains:

- solver ID;
- normalized status;
- optional objective value;
- primal variable values in solver-input column order when available;
- elapsed solve time in seconds;
- bounded diagnostic message.

Adapters must use an empty primal vector when no complete primal assignment is
available. They must not fill missing values with zeros. Non-finite primal
values are invalid result evidence. Adapter parser and library-integration code
should convert non-finite raw objective outputs to `ERROR`; if malformed
objective evidence reaches validation, validation reports `NON_FINITE_OBJECTIVE`.

## CLI Adapter Rule

Only CLI adapter modules may execute external processes. CLI adapters must write
only below the supplied work directory. They must not use process-wide temporary
files for model export or solver outputs.

## Java Library Adapter Rule

Java library adapters call in-process Java APIs and must not execute external
solver binaries. Third-party dependencies remain isolated to the owning adapter
module; foundation modules, the harness, and other adapters must not depend on
OR-Tools or ojAlgo.

Java adapters translate the solver input envelope directly into continuous
variables, linear constraints, objective sense, objective constant, objective
coefficients, and row bounds. They return normalized status, objective evidence,
and a complete primal vector for accepted optimal or feasible results when the
library exposes one.

OR-Tools native runtime loading is adapter-owned. If the OR-Tools Java artifact
is present but the native runtime cannot be loaded on the current machine, the
adapter returns `UNSUPPORTED` with a deterministic diagnostic instead of failing
class loading or the harness process. ojAlgo is pure Java for the 0.1.0 adapter
scope and reports unsupported model features through the same result path.

## In-Project Solver Rule

In-project solver adapters use the same solver input and result evidence
contract as third-party adapters. They must not invoke external processes or
depend on third-party solver libraries.

The G9 simple solver is the first in-project solver. G9-002 wires only a small
SPI smoke path; broad algorithm behavior belongs to the later correctness task.

## MPS Export

The 0.1.0 CLI adapters use the supported MPS subset as their interchange format.
Unsupported MPS features return `UNSUPPORTED` with a deterministic message.
Adapters must preserve row and column names from the solver input envelope when
writing MPS.

## Status Normalization

Adapters normalize solver-specific status text to:

- `OPTIMAL`
- `FEASIBLE`
- `INFEASIBLE`
- `UNBOUNDED`
- `INFEASIBLE_OR_UNBOUNDED`
- `TIME_LIMIT`
- `MEMORY_LIMIT`
- `NUMERICAL_FAILURE`
- `ERROR`
- `UNSUPPORTED`
- `UNKNOWN`

Unknown status text must not be treated as accepted output.

## Solution Parsing Limits

0.1.0 CLI adapters parse only bounded text solution artifacts produced by the
target CLI in the run work directory. They may return objective evidence without
primal values when the solver output exposes objective status clearly but the
variable assignment format is unsupported.

Parsing rules:

- missing objective values are allowed only for non-optimal, unavailable, or
  error statuses;
- malformed numeric values, `NaN`, and infinities produce `ERROR`;
- duplicate variable values produce `ERROR`;
- unknown variable names produce `ERROR`;
- missing variable values leave primal evidence unavailable rather than filling
  zeros;
- unsupported solution file formats produce `UNSUPPORTED`;
- parsed text is bounded before inspection so large logs or solution files cannot
  exhaust memory.

The harness validation layer decides whether available evidence is accepted.
Adapters only normalize and report what they can parse deterministically.

## Timeout, Logs, And Availability

Timeouts are enforced around the external process. If a binary is not present on
`PATH`, adapters return `UNSUPPORTED` with an unavailable-binary diagnostic.
Captured logs are bounded and deterministic; truncation must be explicit in the
message.
