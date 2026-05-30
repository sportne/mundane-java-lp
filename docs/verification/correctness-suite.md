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

## Tier 2: generated LPs with known evidence

Generators should eventually create primal/dual certificates so correctness can
be checked without trusting another solver as oracle.

## Tier 3: public benchmark instances

Public instance suites are added only after file-ingestion and validation logic
is stable.

## Tier 4: massive structured LPs

Parametric generators model wide, tall, network-like, block-angular,
ill-conditioned, and highly degenerate shapes.
