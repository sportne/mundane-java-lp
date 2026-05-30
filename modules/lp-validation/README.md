# lp-validation

Solution, status, and residual validation module.

`lp-validation` owns evidence-based checks for solver output. It validates
against canonical model metadata, sparse row coefficients, optional solver
primal/objective evidence, normalized status claims, and expected fixture
evidence. It does not parse solver-native status text and does not depend on
solver adapters.

The 0.1.0 validation dimensions are variable bounds, row activity, objective
value, normalized status, and expected-objective gap checks. Dual residuals,
complementarity, and certificate checks are reserved until corresponding
evidence is available.

Primary API types:

- `ValidationEvidence`: optional solver status, objective, and primal values.
- `ExpectedValidationResult`: independent fixture expectation.
- `LpSolutionValidator`: checks available evidence and emits stable findings.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
