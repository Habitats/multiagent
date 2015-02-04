package agents;

import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public class MultiplicationSolver extends SimpleAgent {

  @Override
  protected void setup() {
    super.setup();
  }

  @Override
  protected void problemReceived(ACLMessage msg) {

  }

  @Override
  protected String getServiceName() {
    return "multi";
  }

  @Override
  void broadcastReceived(ACLMessage msg) {

  }

  @Override
  protected void takeDown() {
    super.takeDown();
  }
}
