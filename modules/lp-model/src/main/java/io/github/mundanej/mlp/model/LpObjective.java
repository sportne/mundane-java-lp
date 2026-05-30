package io.github.mundanej.mlp.model;

import java.util.Arrays;
import java.util.Objects;

/** Dense objective vector used by the initial canonical LP scaffold. */
public final class LpObjective {
    private final ObjectiveSense sense;
    private final double constant;
    private final double[] coefficients;

    /** Creates an objective. */
    public LpObjective(final ObjectiveSense sense, final double constant, final double[] coefficients) {
        this.sense = Objects.requireNonNull(sense, "sense");
        this.constant = constant;
        this.coefficients = Objects.requireNonNull(coefficients, "coefficients").clone();
    }

    /** Returns the objective sense. */
    public ObjectiveSense sense() {
        return sense;
    }

    /** Returns the objective constant. */
    public double constant() {
        return constant;
    }

    /** Returns the number of objective coefficients. */
    public int size() {
        return coefficients.length;
    }

    /** Returns a defensive copy of the coefficients. */
    public double[] coefficients() {
        return coefficients.clone();
    }

    /** Evaluates the objective for a primal vector. */
    public double evaluate(final double[] primal) {
        Objects.requireNonNull(primal, "primal");
        if (primal.length != coefficients.length) {
            throw new IllegalArgumentException("primal length does not match objective length");
        }
        double value = constant;
        for (int index = 0; index < coefficients.length; index++) {
            value += coefficients[index] * primal[index];
        }
        return value;
    }

    @Override
    public String toString() {
        return "LpObjective[sense=" + sense + ", constant=" + constant
                + ", coefficients=" + Arrays.toString(coefficients) + ']';
    }
}
