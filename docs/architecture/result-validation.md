# Result validation

Result validation decides whether solver output is consistent with independent
model evidence. Solver-reported status is an input claim, not the validation
result. Validation reports accepted/rejected findings with stable finding codes.

## Inputs

The 0.1.0 validator consumes:

- canonical `LpProblem` metadata;
- a CSR row-by-column coefficient matrix for row activity checks;
- optional primal values in column order;
- optional objective value reported by the solver;
- optional solver status claim normalized to validation status names;
- expected fixture evidence for objective value and terminal outcome;
- a named tolerance profile.

Validation remains independent from solver-specific status text. Adapters and
harness code normalize solver text before calling validation.

## Status Claims

Validation status names are:

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

Status validation checks the normalized claim against expected fixture evidence
when expected evidence exists. Missing status claims are allowed only when the
caller is validating primal/objective evidence directly.

## Validation Dimensions

The 0.1.0 implementation validates these dimensions:

- objective value: reported objective and objective evaluated from primal values;
- variable bounds: each primal value must satisfy its column bounds;
- row activity: `A x` must satisfy each row bound;
- status claim: normalized solver status must match expected outcome evidence;
- objective gap: absolute and relative gap against expected objective evidence.

Dual residuals, complementarity, and certificate validation are named dimensions
but remain reserved evidence types until dual/certificate fixtures exist. They must
not be reported as accepted merely because no evidence was supplied.

## Reserved Evidence Types

Dual residual evidence, complementarity evidence, infeasibility certificates,
and unbounded ray certificates are reserved for a separate fixture generation pass.
The 0.1.0 Java API does not need public input fields or finding codes for these
reserved evidence types. Until fixtures carry that evidence, validators must
exclude those dimensions instead of emitting accepted or rejected findings.

## Available-Evidence Policy

Validation checks only dimensions for which it has enough evidence:

- no primal means no variable-bound, row-activity, or evaluated-objective check;
- no reported objective means no reported-objective check;
- no expected objective means no expected-objective or objective-gap check;
- no expected terminal outcome means no status-claim check.

Unavailable dimensions are omitted from findings. Rejected findings are emitted
only for dimensions that were actually checked.

## Benchmark Evidence Policy

Benchmark reports must include validation output before any timing data is used
for comparison. A solver run is eligible for performance evidence only when its
validation report is accepted for the dimensions available to the instance. If
expected objective, primal evidence, or terminal status evidence is absent, the
report must state the missing dimension; missing validation evidence cannot be
treated as acceptance.

Adapter errors, unavailable solvers, unsupported features, missing public input,
and validation failures are benchmark evidence and must remain visible in
Markdown, JSON, and CSV outputs. They are not successful timings.

## Tolerance Profiles

Initial named profiles:

- `LOOSE`: exploratory first-order or massive instances.
- `STANDARD`: normal correctness runs.
- `STRICT`: small instances and regression tests.

A violation is accepted when its magnitude is less than or equal to the active
tolerance. Values strictly greater than tolerance produce findings.

The same scalar tolerance is used for variable feasibility, row feasibility, and
absolute objective gaps in 0.1.0. Relative objective gap uses:

```text
abs(actual - expected) / max(1.0, abs(expected))
```

## Finding Codes

Finding codes are stable API strings. 0.1.0 codes are:

- `VARIABLE_LOWER_BOUND`
- `VARIABLE_UPPER_BOUND`
- `ROW_LOWER_BOUND`
- `ROW_UPPER_BOUND`
- `OBJECTIVE_REPORTED_MISMATCH`
- `OBJECTIVE_PRIMAL_MISMATCH`
- `OBJECTIVE_ABSOLUTE_GAP`
- `OBJECTIVE_RELATIVE_GAP`
- `STATUS_MISSING`
- `STATUS_MISMATCH`
- `NON_FINITE_PRIMAL`
- `NON_FINITE_OBJECTIVE`

Messages are diagnostic text and may change. Code, magnitude, and acceptance
semantics are the contract.
