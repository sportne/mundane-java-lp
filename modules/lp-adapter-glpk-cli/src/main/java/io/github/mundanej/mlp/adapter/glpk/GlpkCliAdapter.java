package io.github.mundanej.mlp.adapter.glpk;

import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.OptionalDouble;

/** G0 placeholder adapter for GlpkCli. */
public final class GlpkCliAdapter implements LpSolverAdapter {
    private static final SolverId ID = new SolverId("glpk", "placeholder");

    /** {@inheritDoc} */
    @Override
    public SolverId id() {
        return ID;
    }

    /** {@inheritDoc} */
    @Override
    public SolverRunResult solve(
            final SolverInput input,
            final SolverOptions options,
            final SolverWorkDirectory workDirectory) {
        return new SolverRunResult(ID, SolverStatus.UNSUPPORTED, OptionalDouble.empty(), 0.0d,
                "GlpkCliAdapter is a G0 placeholder.");
    }
}
