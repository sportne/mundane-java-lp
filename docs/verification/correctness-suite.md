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

The MPS round-trip subset covers Tier 1 minimization fixtures that do not use
canonical ranged rows. Maximization and ranged-row fixtures remain canonical
model fixtures, but they are outside the 0.1.0 MPS subset until objective-sense
and ranged-row encoding are designed.

## Tier 2: generated LPs with known evidence

Generators should eventually create primal/dual certificates so correctness can
be checked without trusting another solver as oracle.

## Tier 3: public benchmark instances

Public instance suites are added only after file-ingestion and validation logic
is stable.

## Tier 4: massive structured LPs

Parametric generators model wide, tall, network-like, block-angular,
ill-conditioned, and highly degenerate shapes.
