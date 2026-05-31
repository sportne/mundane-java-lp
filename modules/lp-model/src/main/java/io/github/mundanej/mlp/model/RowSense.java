package io.github.mundanej.mlp.model;

/** Logical row sense used by parser diagnostics and generated fixtures. */
public enum RowSense {
  /** Equality row. */
  EQUAL,
  /** Less-than-or-equal row. */
  LESS_THAN_OR_EQUAL,
  /** Greater-than-or-equal row. */
  GREATER_THAN_OR_EQUAL,
  /** Ranged row with lower and upper activity bounds. */
  RANGED,
  /** Free row with no activity bounds. */
  FREE
}
