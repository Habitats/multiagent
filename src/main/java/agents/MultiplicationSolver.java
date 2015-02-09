package agents;

import misc.Log;
import misc.Problem;

/**
 * A simple agent for executing multiplication
 */
public class MultiplicationSolver extends AbstractSolverAgent {

  @Override
  protected int getExecutionEstimate(String content) {
    return super.getExecutionEstimate(content) + 1000;
  }

  @Override
  protected void problemReceived(Problem problem) {
    problem.solve();
    Log.v(getTag(), problem + " solved!");
  }

  @Override
  protected String getServiceName() {
    return Operator.MULTIPLICATION.get();
  }


}
