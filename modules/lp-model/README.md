# lp-model

Canonical LP domain model module.

`lp-model` owns the solver-neutral LP metadata used across the project:

- optimization direction and objective constant/coefficient vector;
- variable bounds, including free and fixed variables;
- row activity bounds, including equality, ranged, one-sided, and free rows;
- problem shape statistics and a diagnostic problem name.

The module intentionally does not own sparse coefficient storage, parser state,
solver status, validation results, row or variable names, or harness behavior.
Matrix coefficients and optional row/variable names are associated by position
through sparse storage or I/O-specific envelope types.

Production model constructors reject inconsistent shape data and invalid bounds.
Invalid LP data belongs in boundary-specific negative tests, not shared valid
fixture catalogs.

See the repository root `README.md` and `docs/architecture/module-boundaries.md`.
