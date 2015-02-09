package agents;

import misc.Log;
import misc.Problem;

/**
 * Created by anon on 04.02.2015.
 */
public class MultiplicationSolver extends SimpleAgent {

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
