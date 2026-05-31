package io.github.mundanej.mlp.solver.spi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;

final class SolverRunResultTest {
    @Test
    void defensivelyCopiesPrimalValues() {
        double[] primal = {1.0d, 2.0d};
        SolverRunResult result = new SolverRunResult(
                new SolverId("solver", "test"),
                SolverStatus.OPTIMAL,
                OptionalDouble.of(3.0d),
                primal,
                0.1d,
                "ok");

        primal[0] = 99.0d;
        double[] returned = result.primalValues();
        returned[1] = 99.0d;

        assertArrayEquals(new double[] {1.0d, 2.0d}, result.primalValues());
    }

    @Test
    void rejectsNonFinitePrimalValues() {
        assertThrows(IllegalArgumentException.class, () -> new SolverRunResult(
                new SolverId("solver", "test"),
                SolverStatus.OPTIMAL,
                OptionalDouble.of(3.0d),
                new double[] {Double.NaN},
                0.1d,
                "bad"));
    }
}
