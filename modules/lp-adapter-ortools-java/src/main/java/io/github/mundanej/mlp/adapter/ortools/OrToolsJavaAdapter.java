package io.github.mundanej.mlp.adapter.ortools;

import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.OptionalDouble;

/** G0 placeholder adapter for OrToolsJava. */
public final class OrToolsJavaAdapter implements LpSolverAdapter {
    private static final SolverId ID = new SolverId("ortools", "placeholder");

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
        return new SolverRunResult(ID, SolverStatus.UNSUPPORTED, OptionalDouble.empty(), new double[0], 0.0d,
                "OrToolsJavaAdapter is a G0 placeholder.");
    }
}
