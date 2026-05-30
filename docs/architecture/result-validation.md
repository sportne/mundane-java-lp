# Result validation

Solver status is an input to validation, not the validation result.

## Validation dimensions

- Objective value.
- Variable bound violations.
- Row lower/upper violations.
- Primal feasibility residuals.
- Dual feasibility residuals.
- Complementarity and gap when dual information exists.
- Certificates for infeasible or unbounded statuses when available.

## Tolerance profiles

Initial named profiles:

- `LOOSE`: exploratory first-order or massive instances.
- `STANDARD`: normal correctness runs.
- `STRICT`: small instances and regression tests.
