package agents;

import misc.Problem;

/**
 * Created by anon on 04.02.2015.
 */
public class DivisionSolver extends SimpleAgent {

  @Override
  protected int getExecutionEstimate(String content) {
    return 2;
  }

  @Override
  protected void problemReceived(Problem problem) {
    problem.solve();
    System.out.println(id() + problem + " solved!");
  }

  @Override
  protected String getServiceName() {
    return Operator.DIVISION.get();
  }

}
