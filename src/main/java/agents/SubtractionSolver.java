package agents;

import jade.lang.acl.ACLMessage;
import misc.Problem;

/**
 * Created by anon on 04.02.2015.
 */
public class SubtractionSolver extends SimpleAgent {

  @Override
  protected int getExecutionEstimate(String content) {
    return 2;
  }

  @Override
  protected void problemReceived(Problem problem) {

  }

  @Override
  protected String getServiceName() {
    return "sub";
  }



}
