# Generated instance families

Roadmap problem-shape families:

- tiny known-answer LPs;
- random feasible bounded LPs;
- primal/dual certificate LPs;
- network-flow-like LPs;
- transportation LPs;
- block-angular LPs;
- scenario-tree LPs;
- degenerate LPs;
- ill-conditioned LPs;
- wide sparse LPs;
- tall sparse LPs.

Every generated instance records the generator name, seed, size parameters,
scaling parameters when present, and expected evidence when known.

## 0.1.0 network-flow baseline

The 0.1.0 generated baseline is `network-flow-3-node`, a deterministic
three-node, three-arc LP family. Each instance has variables for
`source_middle`, `middle_sink`, and `source_sink`; a relay-balance equality row;
bounded arc capacities derived from the seed; and a max-flow-like objective that
maximizes relay flow plus direct flow to the sink.

The generator records:

- generator name: `network-flow-3-node`;
- seed;
- size parameters: nodes, arcs, and derived arc capacities;
- canonical `LpProblem`;
- CSR coefficient matrix;
- row and column names;
- expected optimal objective and primal evidence.

The known optimum sends `min(source_middle_capacity, middle_sink_capacity)`
through the relay path and `source_sink_capacity` through the direct path.

## 0.1.0 numerical stress baseline

The 0.1.0 numerical stress baseline is `numerical-stress-v1`, a deterministic
small fixture family used for robustness checks, not benchmark claims. The
suite contains:

- `stress-scaling`: two nonnegative variables with coefficients separated by
  `1.0e6`, expected optimal objective `1.0`;
- `stress-degeneracy`: duplicate active rows and a zero objective, expected
  accepted optimum;
- `stress-tight-bounds`: a one-variable case with width `1.0e-9`, expected
  accepted optimum at the tight bound;
- `stress-ill-conditioned-ranged`: a nearly parallel ranged-row case recorded
  with feasible optimal evidence and intentionally unsupported by the current
  performance solver.

Each generated instance records generator name, seed, size/scaling parameters,
canonical model data, CSR coefficients, names, and expected evidence.
