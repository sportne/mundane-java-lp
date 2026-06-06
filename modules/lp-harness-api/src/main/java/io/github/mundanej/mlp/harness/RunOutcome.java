package io.github.mundanej.mlp.harness;

/** Deterministic harness outcome for one solver-instance request. */
public enum RunOutcome {
  /** Solver ran and validation accepted available evidence. */
  SUCCESS,
  /** Solver ran but validation rejected available evidence. */
  VALIDATION_FAILED,
  /** Solver or adapter reported unavailable/unsupported. */
  SOLVER_UNAVAILABLE,
  /** Solver is available but does not support the requested model shape. */
  UNSUPPORTED,
  /** Adapter failed before producing a usable solver result. */
  ADAPTER_ERROR
}
