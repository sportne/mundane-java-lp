package io.github.mundanej.mlp.solver.spi;

import io.github.mundanej.mlp.model.LpProblem;

/** Adapter contract for LP solvers. */
public interface LpSolverAdapter {
    /** Returns solver identifier. */
    SolverId id();

    /** Solves the supplied problem or returns an unsupported/error result. */
    SolverRunResult solve(LpProblem problem, SolverOptions options, SolverWorkDirectory workDirectory);
}
