package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import misc.Problem;

/**
 * Created by anon on 04.02.2015.
 */
public abstract class SimpleAgent extends Agent {

  @Override
  protected void setup() {
    registerWithYellowPages();

    System.out.println(id() + "representin'!");
    addBehaviour(new Behaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          return;
        }

        if (msg.getPerformative() == ACLMessage.CFP) {
          System.out.println(id() + "I can solve this! Problem: " + msg.getContent());
          ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
          reply.setContent(String.valueOf(getExecutionEstimate(msg.getContent())));
          reply.addReceiver(msg.getSender());
          myAgent.send(reply);
        } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
          Problem problem = new Problem(msg.getContent());
          System.out.println(id() + "My proposal was accepted -- attempting to solve: " + problem);
          problemReceived(problem);

          ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
          reply.setContent(problem.getValue());
          reply.addReceiver(msg.getSender());
          myAgent.send(reply);
        }

        block();
      }

      @Override
      public boolean done() {
        return false;
      }
    });

//    addBehaviour(new OneShotBehaviour() {
//      @Override
//      public void action() {
//        broadcastMessage("go go go");
//      }
//    });
  }

  protected String id() {
    return getAID().getLocalName() + ": ";
  }

  protected abstract int getExecutionEstimate(String content);

  private void registerWithYellowPages() {
    addBehaviour(new OneShotBehaviour() {
      @Override
      public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("math-solver");
        sd.setName(getServiceName());
        dfd.addServices(sd);

        System.out.println(id() + "registred with YellowPages!");

        try {
          DFService.register(SimpleAgent.this, dfd);
        } catch (FIPAException fe) {
          fe.printStackTrace();
        }
      }
    });
  }

  protected abstract void problemReceived(Problem problem);


  protected abstract String getServiceName();

  private void broadcastMessage(String content) {
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.addReceiver(new AID("oxy", AID.ISLOCALNAME));
    msg.setLanguage("English");
    msg.setOntology("duck-dock-go");
    msg.setContent(content);
    send(msg);
    System.out.println(id() + "broadcasting message!");
  }

  abstract void broadcastReceived(ACLMessage msg);


  @Override
  protected void takeDown() {
    // Deregister from the yellow pages
    try {
      DFService.deregister(this);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }

    System.out.println(id() + "going down!");
  }
}
