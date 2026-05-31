# tiny-lp

Example project for tiny, hand-checkable LP instances and the explicit simple
solver MPS smoke path.

The application writes the supported `single-bounded-variable` Tier 1 fixture to
MPS, reads it back through `lp-io-mps`, runs `lp-solver-simple` through the
standard harness API, and prints the normalized solver status plus harness
outcome. This path is explicit; it does not perform solver discovery.
