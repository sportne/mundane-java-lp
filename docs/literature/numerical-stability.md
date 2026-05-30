# Numerical stability

## Design notes

- Validation must be independent of solver logs. A status is credible only when
  supported by residuals, objective evidence, tolerances, and clear handling of
  infeasible or unbounded cases.
- Floating-point LP results can be useful and still fail strict checks. Reports
  should distinguish accepted, rejected, unsupported, and numerically uncertain
  outcomes without collapsing them into pass/fail text.
- Iterative refinement is a later design option for high-accuracy paths, but the
  early project should first make residual computation and tolerance profiles
  boring and reproducible.
- Java/GraalVM code should avoid hidden global numeric state and should make
  precision, tolerances, and scaling choices explicit in APIs and reports.

## Roadmap references

- `g3-001-validation-design-completion`
- `g3-002-validation-engine-fixtures`
- `g7-003-benchmark-evidence-baseline`
- `g9-015-numerical-robustness-stress-suite`

## References

- Higham, *Accuracy and Stability of Numerical Algorithms*, SIAM, second
  edition, 2002, <https://doi.org/10.1137/1.9780898718027>.
- Gleixner, Steffy, and Wolter, "Iterative Refinement for Linear Programming,"
  INFORMS Journal on Computing, 2016, <https://doi.org/10.1287/ijoc.2016.0692>.
