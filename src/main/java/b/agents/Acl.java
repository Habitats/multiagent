package b.agents;


import java.util.Arrays;

import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 24.02.2015.
 */
public enum Acl {
  PROPOSE(ACLMessage.PROPOSE), //
  REFUSE(ACLMessage.REFUSE), //
  REQUEST(ACLMessage.REQUEST), //
  INFORM(ACLMessage.INFORM), //
  CONFIRM(ACLMessage.CONFIRM),//
  ACCEPT_PROPOSAL(ACLMessage.ACCEPT_PROPOSAL),//
  QUERY_REF(ACLMessage.QUERY_REF), //
  CFP(ACLMessage.CFP);

  private final int inform;

  Acl(int inform) {
    this.inform = inform;
  }

  public static Acl get(int performative) {
    return Arrays.asList(Acl.values()).stream().filter(v -> v.inform == performative).findFirst().get();
  }
}
