# Correctness suite

Correctness tests are layered.

## Tier 1: hand-checkable LPs

- Single variable bound optimum.
- Two-variable feasible optimum.
- Infeasible problem.
- Unbounded problem.
- Redundant row.
- Fixed variable.
- Free variable.
- Equality row.
- Ranged row.
- Degenerate optimum.

Tier 1 fixtures are implemented as direct canonical model instances with sparse
matrix coefficients and hand-checked evidence. Optimal fixtures include a
primal vector and objective value. Infeasible and unbounded fixtures carry the
expected outcome without primal evidence.

Tier 1 validation maps to every 0.1.0 implemented validation dimension:

- optimal fixtures validate status, primal variable bounds, row activity,
  objective-from-primal, reported objective, and objective gap;
- infeasible and unbounded fixtures validate normalized terminal status claims;
- tolerance boundary tests use small deterministic perturbations around Tier 1
  evidence instead of generated random instances;
- shape mismatch tests use deliberately invalid local fixtures and are not part
  of the canonical fixture catalog.

`lp-testkit` exposes the canonical fixture evidence as validation expected
results and solver-like validation evidence. Validation tests consume that
adapter instead of duplicating fixture math.

`lp-solver-simple` solves the Tier 1 fixture shapes that have zero, one, or two
variables. Its correctness tests send solver output back through
`lp-validation`; unsupported larger shapes are checked separately and are not
part of the Tier 1 fixture catalog.

The MPS round-trip subset covers Tier 1 minimization fixtures that do not use
canonical ranged rows. Maximization and ranged-row fixtures remain canonical
model fixtures, but they are outside the 0.1.0 MPS subset until objective-sense
and ranged-row encoding are designed.

`examples/tiny-lp` exercises one supported MPS fixture through the simple solver
and harness path. It is a smoke path for integration wiring, not a replacement
for the full Tier 1 simple-solver validation tests.

## Tier 2: generated LPs with known evidence

Generators should eventually create primal/dual certificates so correctness can
be checked without trusting another solver as oracle.

## Tier 3: public benchmark instances

Public instance suites are added only after file-ingestion and validation logic
is stable.

## Tier 4: massive structured LPs

Parametric generators model wide, tall, network-like, block-angular,
ill-conditioned, and highly degenerate shapes.
