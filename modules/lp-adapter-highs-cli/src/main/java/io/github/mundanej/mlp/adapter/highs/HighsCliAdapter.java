package io.github.mundanej.mlp.adapter.highs;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.OptionalDouble;

/** G0 placeholder adapter for HighsCli. */
public final class HighsCliAdapter implements LpSolverAdapter {
    private static final SolverId ID = new SolverId("highs", "placeholder");

    @Override
    public SolverId id() {
        return ID;
    }

    @Override
    public SolverRunResult solve(
            final LpProblem problem,
            final SolverOptions options,
            final SolverWorkDirectory workDirectory) {
        return new SolverRunResult(ID, SolverStatus.UNSUPPORTED, OptionalDouble.empty(), 0.0d,
                "HighsCliAdapter is a G0 placeholder.");
    }
}
