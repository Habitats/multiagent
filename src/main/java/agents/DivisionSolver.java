package agents;

import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public class DivisionSolver extends SimpleAgent {


  @Override
  protected void problemReceived(ACLMessage msg) {

  }

  @Override
  protected String getServiceName() {
    return "div";
  }

  @Override
  void broadcastReceived(ACLMessage msg) {

  }
}
