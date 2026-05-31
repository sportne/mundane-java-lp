package io.github.mundanej.mlp.solver.spi;

import java.nio.file.Path;

/**
 * Work directory made available to solver adapters.
 *
 * @param path work directory path
 */
public record SolverWorkDirectory(Path path) {
  /**
   * Creates a work directory.
   *
   * @param path work directory path
   */
  public SolverWorkDirectory {
    if (path == null) {
      throw new IllegalArgumentException("path must not be null");
    }
  }
}
