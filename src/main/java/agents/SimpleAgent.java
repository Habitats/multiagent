package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public abstract class SimpleAgent extends Agent {

  @Override
  protected void setup() {
    addBehaviour(new OneShotBehaviour() {
      @Override
      public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("math-solver");
        sd.setName(getServiceName());
        dfd.addServices(sd);

        try {
          DFService.register(SimpleAgent.this, dfd);
        } catch (FIPAException fe) {
          fe.printStackTrace();
        }
      }
    });
    System.out.println(getAID().getLocalName() + " representin'!");
    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          return;
        }

        if (msg.getConversationId() != null && msg.getConversationId().equalsIgnoreCase("math-problem")) {
          System.out.println("I can solve this! Problem: " + msg.getContent());
          problemReceived(msg);
        } else {
          System.out.println(getAID().getLocalName() + " received a message!");
          broadcastReceived(msg);
        }

        block();
      }
    });

    addBehaviour(new OneShotBehaviour() {
      @Override
      public void action() {
        broadcastMessage("go go go");
      }
    });
  }

  protected abstract void problemReceived(ACLMessage msg);


  protected abstract String getServiceName();

  private void broadcastMessage(String content) {
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    msg.addReceiver(new AID("oxy", AID.ISLOCALNAME));
    msg.setLanguage("English");
    msg.setOntology("duck-dock-go");
    msg.setContent(content);
    send(msg);
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

    System.out.println(getAID().getLocalName() + " going down!");
  }
}
