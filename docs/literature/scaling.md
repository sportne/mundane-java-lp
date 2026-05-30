# Scaling

## Design notes

- Scaling is a numerical transformation that can improve solver behavior while
  making raw residuals harder to compare. The validation design must define
  whether tolerances are checked in original or scaled space.
- The project should record row and column scaling choices in harness outputs
  once an in-project solver applies them. Until then, adapters should preserve
  enough solver logs/options to explain scaled statuses from third-party tools.
- Automatic scaling is a performance-solver concern, not a simple-solver
  requirement. The simple solver should prefer unscaled, hand-checkable fixtures
  so early correctness failures are easy to interpret.
- Benchmark evidence should include coefficient ranges before scaling. Otherwise
  performance changes can be numerical-conditioning changes rather than solver
  improvements.

## Roadmap references

- `g3-001-validation-design-completion`
- `g7-003-benchmark-evidence-baseline`
- `g9-015-numerical-robustness-stress-suite`
- `g9-016-performance-iteration-3-robustness-and-scaling`

## References

- GNU GLPK documentation notes; the reference manual is distributed with the
  official GLPK source package, <https://www.gnu.org/software/glpk/>.
- lp_solve scaling notes, <https://lpsolve.sourceforge.net/5.5/scaling.htm>.
- Ploskas and Samaras, "A computational comparison of scaling techniques for
  linear optimization problems on a graphical processing unit," International
  Journal of Computer Mathematics, 2015,
  <https://doi.org/10.1080/00207160.2014.890716>.
