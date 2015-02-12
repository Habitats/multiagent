package skjennum.agents;

import skjennum.misc.Log;
import skjennum.misc.Problem;

/**
 * A simple agent for executing multiplication
 */
public class MultiplicationSolver extends AbstractSolverAgent {

  @Override
  protected int getExecutionEstimate(String content) {
    return super.getExecutionEstimate(content) + EXECUTION_CONSTANT;
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
