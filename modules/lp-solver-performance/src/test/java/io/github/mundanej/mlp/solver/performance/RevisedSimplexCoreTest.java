package io.github.mundanej.mlp.solver.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.mundanej.mlp.solver.performance.RevisedSimplexCore.LinearConstraint;
import io.github.mundanej.mlp.solver.performance.RevisedSimplexCore.SimplexStatus;
import io.github.mundanej.mlp.solver.performance.RevisedSimplexCore.Tableau;
import java.util.List;
import org.junit.jupiter.api.Test;

final class RevisedSimplexCoreTest {
  @Test
  void initializesSlackBasisForLessThanRows() {
    Tableau tableau =
        Tableau.from(2, List.of(LinearConstraint.le(new double[] {1.0d, 1.0d}, 4.0d)));

    assertEquals(2, tableau.basisColumn(0));
  }

  @Test
  void choosesFirstNegativeReducedCostAsEnteringColumn() {
    Tableau tableau =
        Tableau.from(2, List.of(LinearConstraint.le(new double[] {1.0d, 1.0d}, 4.0d)));

    tableau.optimize(new double[] {3.0d, 0.0d});

    assertEquals(0, tableau.basisColumn(0));
  }

  @Test
  void ratioTestChoosesTightestLeavingRow() {
    Tableau tableau =
        Tableau.from(
            1,
            List.of(
                LinearConstraint.le(new double[] {1.0d}, 4.0d),
                LinearConstraint.le(new double[] {1.0d}, 2.0d)));

    assertEquals(1, tableau.leavingRow(0));
  }

  @Test
  void reconstructsObjectiveAndPrimalAfterOptimization() {
    Tableau tableau =
        Tableau.from(
            2,
            List.of(
                LinearConstraint.le(new double[] {1.0d, 1.0d}, 4.0d),
                LinearConstraint.le(new double[] {1.0d, 0.0d}, 2.0d)));

    assertEquals(SimplexStatus.OPTIMAL, tableau.optimize(new double[] {3.0d, 2.0d}));

    double[] solution = tableau.originalSolution(2);
    assertEquals(2.0d, solution[0]);
    assertEquals(2.0d, solution[1]);
    assertEquals(10.0d, tableau.objectiveValue());
  }

  @Test
  void phaseOneHandlesEqualityRows() {
    Tableau tableau =
        Tableau.from(2, List.of(LinearConstraint.eq(new double[] {1.0d, 1.0d}, 5.0d)));

    assertEquals(SimplexStatus.OPTIMAL, tableau.optimize(tableau.phaseOneObjective()));

    assertEquals(0.0d, tableau.objectiveValue());
  }
}
