package io.github.mundanej.mlp.adapter.ojalgo;

import io.github.mundanej.mlp.model.LpProblem;
import io.github.mundanej.mlp.model.LpRowBounds;
import io.github.mundanej.mlp.model.LpVariableBounds;
import io.github.mundanej.mlp.model.ObjectiveSense;
import io.github.mundanej.mlp.solver.spi.LpSolverAdapter;
import io.github.mundanej.mlp.solver.spi.SolverId;
import io.github.mundanej.mlp.solver.spi.SolverInput;
import io.github.mundanej.mlp.solver.spi.SolverOptions;
import io.github.mundanej.mlp.solver.spi.SolverRunResult;
import io.github.mundanej.mlp.solver.spi.SolverStatus;
import io.github.mundanej.mlp.solver.spi.SolverWorkDirectory;
import java.util.Objects;
import java.util.OptionalDouble;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

/** Java library adapter for ojAlgo. */
public final class OjAlgoAdapter implements LpSolverAdapter {
    private static final SolverId ID = new SolverId("ojalgo", "java");

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
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(workDirectory, "workDirectory");
        long startNanos = System.nanoTime();
        try {
            ExpressionsBasedModel model = buildModel(input);
            model.options.time_abort = options.timeLimit().toMillis();
            Optimisation.Result result = input.problem().objective().sense() == ObjectiveSense.MAXIMIZE
                    ? model.maximise()
                    : model.minimise();
            SolverStatus status = normalize(result.getState());
            if (status == SolverStatus.OPTIMAL || status == SolverStatus.FEASIBLE) {
                OptionalDouble objective = objectiveValue(input.problem(), result);
                double[] primal = primalValues(result, input.problem().stats().columns());
                if (objective.isEmpty() || primal.length != input.problem().stats().columns()) {
                    return result(SolverStatus.ERROR, OptionalDouble.empty(), new double[0], startNanos,
                            "ojAlgo returned non-finite solution evidence.");
                }
                return result(status, objective, primal, startNanos, "ojAlgo state: " + result.getState());
            }
            return result(status, OptionalDouble.empty(), new double[0], startNanos, "ojAlgo state: " + result.getState());
        } catch (IllegalArgumentException exception) {
            return result(SolverStatus.UNSUPPORTED, OptionalDouble.empty(), new double[0], startNanos,
                    exception.getMessage());
        } catch (RuntimeException exception) {
            return result(SolverStatus.ERROR, OptionalDouble.empty(), new double[0], startNanos, message(exception));
        }
    }

    private static ExpressionsBasedModel buildModel(final SolverInput input) {
        ExpressionsBasedModel model = new ExpressionsBasedModel();
        LpProblem problem = input.problem();
        Variable[] variables = variables(problem, input, model);
        constraints(problem, input, variables, model);
        return model;
    }

    private static Variable[] variables(
            final LpProblem problem,
            final SolverInput input,
            final ExpressionsBasedModel model) {
        double[] objective = problem.objective().coefficients();
        Variable[] variables = new Variable[input.columnNames().size()];
        for (int index = 0; index < variables.length; index++) {
            LpVariableBounds bounds = problem.variableBounds().get(index);
            Variable variable = model.addVariable(input.columnNames().get(index))
                    .weight(finite(objective[index], "objective coefficient"));
            if (Double.isFinite(bounds.lower())) {
                variable.lower(bounds.lower());
            }
            if (Double.isFinite(bounds.upper())) {
                variable.upper(bounds.upper());
            }
            rejectBounds(bounds.lower(), bounds.upper());
            variables[index] = variable;
        }
        finite(problem.objective().constant(), "objective constant");
        return variables;
    }

    private static void constraints(
            final LpProblem problem,
            final SolverInput input,
            final Variable[] variables,
            final ExpressionsBasedModel model) {
        double[] values = input.matrix().values();
        int[] columnIndices = input.matrix().columnIndices();
        int[] rowPointers = input.matrix().rowPointers();
        for (int row = 0; row < input.matrix().rows(); row++) {
            LpRowBounds bounds = problem.rowBounds().get(row);
            rejectBounds(bounds.lower(), bounds.upper());
            if (!Double.isFinite(bounds.lower()) && !Double.isFinite(bounds.upper())) {
                continue;
            }
            Expression expression = model.addExpression(input.rowNames().get(row));
            if (Double.isFinite(bounds.lower())) {
                expression.lower(bounds.lower());
            }
            if (Double.isFinite(bounds.upper())) {
                expression.upper(bounds.upper());
            }
            for (int offset = rowPointers[row]; offset < rowPointers[row + 1]; offset++) {
                expression.set(variables[columnIndices[offset]], finite(values[offset], "matrix value"));
            }
        }
    }

    static SolverStatus normalize(final Optimisation.State state) {
        return switch (state) {
            case OPTIMAL, DISTINCT -> SolverStatus.OPTIMAL;
            case FEASIBLE, APPROXIMATE -> SolverStatus.FEASIBLE;
            case INFEASIBLE -> SolverStatus.INFEASIBLE;
            case UNBOUNDED -> SolverStatus.UNBOUNDED;
            case FAILED -> SolverStatus.NUMERICAL_FAILURE;
            case INVALID -> SolverStatus.ERROR;
            case UNEXPLORED, VALID -> SolverStatus.UNKNOWN;
        };
    }

    private static OptionalDouble objectiveValue(final LpProblem problem, final Optimisation.Result result) {
        double value = result.getValue() + problem.objective().constant();
        if (!Double.isFinite(value)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value);
    }

    private static double[] primalValues(final Optimisation.Result result, final int expectedLength) {
        if (result.size() != expectedLength) {
            return new double[0];
        }
        double[] primal = new double[expectedLength];
        for (int index = 0; index < primal.length; index++) {
            double value = result.doubleValue(index);
            if (!Double.isFinite(value)) {
                return new double[0];
            }
            primal[index] = value;
        }
        return primal;
    }

    private static double finite(final double value, final String label) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(label + " must be finite");
        }
        return value;
    }

    private static void rejectBounds(final double lower, final double upper) {
        if (Double.isNaN(lower) || Double.isNaN(upper)) {
            throw new IllegalArgumentException("bound must not be NaN");
        }
        if (lower == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("lower bound must not be positive infinity");
        }
        if (upper == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("upper bound must not be negative infinity");
        }
    }

    private static SolverRunResult result(
            final SolverStatus status,
            final OptionalDouble objective,
            final double[] primal,
            final long startNanos,
            final String message) {
        double elapsedSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0d;
        return new SolverRunResult(ID, status, objective, primal, elapsedSeconds, message);
    }

    static String message(final Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message;
    }
}
