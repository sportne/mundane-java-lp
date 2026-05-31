package io.github.mundanej.mlp.solver.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

final class SolverOptionsTest {
  @Test
  void defaultUsesOneThread() {
    assertEquals(1, SolverOptions.defaults().threads());
    assertEquals(Duration.ofSeconds(60), SolverOptions.defaults().timeLimit());
  }

  @Test
  void rejectsZeroThreads() {
    assertThrows(IllegalArgumentException.class, () -> new SolverOptions(Duration.ofSeconds(1), 0));
  }

  @Test
  void rejectsNullTimeLimitAndNegativeThreads() {
    assertThrows(IllegalArgumentException.class, () -> new SolverOptions(null, 1));
    assertThrows(
        IllegalArgumentException.class, () -> new SolverOptions(Duration.ofSeconds(1), -1));
  }
}
