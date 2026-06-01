package io.github.mundanej.mlp.solver.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.mundanej.mlp.solver.performance.RevisedSimplexCore.SimplexStatus;
import io.github.mundanej.mlp.solver.performance.RevisedSimplexCore.Tableau;
import io.github.mundanej.mlp.solver.performance.RevisedSimplexCore.TableauBuilder;
import org.junit.jupiter.api.Test;

final class RevisedSimplexCoreTest {
  @Test
  void initializesSlackBasisForLessThanRows() {
    TableauBuilder builder = new TableauBuilder(2, 1, 1);
    builder.addLe(new double[] {1.0d, 1.0d}, 4.0d);

    assertEquals(2, builder.build().basisColumn(0));
  }

  @Test
  void choosesFirstNegativeReducedCostAsEnteringColumn() {
    TableauBuilder builder = new TableauBuilder(2, 1, 1);
    builder.addLe(new double[] {1.0d, 1.0d}, 4.0d);
    Tableau tableau = builder.build();

    tableau.optimize(new double[] {3.0d, 0.0d});

    assertEquals(0, tableau.basisColumn(0));
  }

  @Test
  void ratioTestChoosesTightestLeavingRow() {
    TableauBuilder builder = new TableauBuilder(1, 2, 2);
    builder.addLe(new double[] {1.0d}, 4.0d);
    builder.addLe(new double[] {1.0d}, 2.0d);

    assertEquals(1, builder.build().leavingRow(0));
  }

  @Test
  void reconstructsObjectiveAndPrimalAfterOptimization() {
    TableauBuilder builder = new TableauBuilder(2, 2, 2);
    builder.addLe(new double[] {1.0d, 1.0d}, 4.0d);
    builder.addLe(new double[] {1.0d, 0.0d}, 2.0d);
    Tableau tableau = builder.build();

    assertEquals(SimplexStatus.OPTIMAL, tableau.optimize(new double[] {3.0d, 2.0d}));

    double[] solution = tableau.originalSolution(2);
    assertEquals(2.0d, solution[0]);
    assertEquals(2.0d, solution[1]);
    assertEquals(10.0d, tableau.objectiveValue());
  }

  @Test
  void phaseOneHandlesEqualityRows() {
    TableauBuilder builder = new TableauBuilder(2, 1, 1);
    builder.addEq(new double[] {1.0d, 1.0d}, 5.0d);
    Tableau tableau = builder.build();

    assertEquals(SimplexStatus.OPTIMAL, tableau.optimize(tableau.phaseOneObjective()));

    assertEquals(0.0d, tableau.objectiveValue());
  }

  @Test
  void tableauRowsRemainStableWhenSourceBufferIsReused() {
    TableauBuilder builder = new TableauBuilder(2, 2, 2);
    double[] rowBuffer = {1.0d, 0.0d};
    builder.addLe(rowBuffer, 2.0d);
    rowBuffer[0] = 0.0d;
    rowBuffer[1] = 1.0d;
    builder.addLe(rowBuffer, 3.0d);
    Tableau tableau = builder.build();

    assertEquals(SimplexStatus.OPTIMAL, tableau.optimize(new double[] {1.0d, 1.0d}));
    assertEquals(5.0d, tableau.objectiveValue());
  }
}
