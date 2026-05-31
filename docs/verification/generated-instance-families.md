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
