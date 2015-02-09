package agents;

import misc.Log;
import misc.Problem;

/**
 * A simple agent for executing division
 */
public class DivisionSolver extends AbstractSolverAgent {

  @Override
  protected int getExecutionEstimate(String content) {
    return super.getExecutionEstimate(content) + 1000;
  }

  @Override
  protected void problemReceived(Problem problem) {
    problem.solve();
    Log.v(getTag(), "Returning answer: " + problem);
  }

  @Override
  protected String getServiceName() {
    return Operator.DIVISION.get();
  }

}
