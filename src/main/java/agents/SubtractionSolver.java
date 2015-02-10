package agents;

import misc.Log;
import misc.Problem;

/**
 * A simple agent for executing subtraction
 */
public class SubtractionSolver extends AbstractSolverAgent {

  @Override
  protected int getExecutionEstimate(String content) {
    return super.getExecutionEstimate(content) + EXECUTION_CONSTANT;
  }

  @Override
  protected void problemReceived(Problem problem) {
    problem.solve();
    Log.v(getTag(), "Returning answer: " + problem);
  }

  @Override
  protected String getServiceName() {
    return Operator.SUBTRACTION.get();
  }

}
