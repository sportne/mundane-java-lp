# Numerical stability

## Design notes

- Validation must be independent of solver logs. A status is credible only when
  supported by residuals, objective evidence, tolerances, and clear handling of
  infeasible or unbounded cases.
- Floating-point LP results can be useful and still fail strict checks. Reports
  should distinguish accepted, rejected, unsupported, and numerically uncertain
  outcomes without collapsing them into pass/fail text.
- Iterative refinement is a separate design option for high-accuracy paths, but the
  early project should first make residual computation and tolerance profiles
  boring and reproducible.
- Java/GraalVM code should avoid hidden global numeric state and should make
  precision, tolerances, and scaling choices explicit in APIs and reports.
- ADR-0007 intentionally accepts a narrow revised-simplex-style implementation
  with documented numerical risk. Degeneracy, scaling, tight bounds, and
  ill-conditioning must become deterministic validation, unsupported, or
  limitation records before the project treats timing evidence as meaningful.
- G9-015 adds the first bounded numerical stress suite. Scaling, degeneracy,
  and tight-bound cases must validate with `ToleranceProfile.STANDARD`; the
  current ill-conditioned ranged-row case has feasible evidence but is an
  explicit performance-solver unsupported outcome because ranged-row
  normalization is outside the 0.1.0 performance solver subset.
- G9-016 adds a narrow tableau row-scaling step for non-unit row constraints:
  when the largest absolute original row coefficient exceeds one, generated row
  coefficients and right-hand sides are divided by that coefficient scale before
  slack/surplus/artificial columns are appended. The final primal is checked
  against the original unscaled model before reporting `OPTIMAL`. This improves
  large-coefficient robustness without changing the supported subset. It is not
  presolve, equilibration, iterative refinement, or a claim of broad numerical
  stability.

## Roadmap references

- `g3-001-validation-design-completion`
- `g3-002-validation-engine-fixtures`
- `g7-003-benchmark-evidence-baseline`
- `g9-005-performance-solver-design-decision`
- `g9-015-numerical-robustness-stress-suite`

## References

- Higham, *Accuracy and Stability of Numerical Algorithms*, SIAM, second
  edition, 2002, <https://doi.org/10.1137/1.9780898718027>.
- Gleixner, Steffy, and Wolter, "Iterative Refinement for Linear Programming,"
  INFORMS Journal on Computing, 2016, <https://doi.org/10.1287/ijoc.2016.0692>.
