package agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
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

  public enum Operator {
    ADDITION("+"), SUBTRACTION("-"), DIVISION("/"), MULTIPLICATION("*");
    private final String s;

    Operator(String s) {
      this.s = s;
    }

    public String get() {
      return s;
    }
  }

  private enum State {
    READY, WAITING_FOR_CONFIRMATION, BUSY
  }

  private String conversationId;
  private State state = State.READY;

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

        switch (state) {
          case READY:
            if (msg.getPerformative() == ACLMessage.CFP) {
              System.out.println(id() + "I can solve this! Problem: " + msg.getContent());
              ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
              reply.setContent(String.valueOf(getExecutionEstimate(msg.getContent())));
              reply.addReceiver(msg.getSender());
              myAgent.send(reply);
              state = State.WAITING_FOR_CONFIRMATION;
            }
            break;
          case WAITING_FOR_CONFIRMATION:
            if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
              state = State.BUSY;
              Problem problem = new Problem(msg.getContent());
              System.out.println(id() + "My proposal was accepted -- attempting to solve: " + problem);
              problemReceived(problem);

              addBehaviour(new WakerBehaviour(SimpleAgent.this, getExecutionEstimate(msg.getContent())) {
                @Override
                public void handleElapsedTimeout() {
                  ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                  reply.setContent(problem.getValue());
                  reply.addReceiver(msg.getSender());
                  myAgent.send(reply);
                  state = State.READY;
                }

              });
            }
            // didn't win the bid, moving on!
            else {
              state = State.READY;
            }
            break;
          case BUSY:
            System.out.println(id() + "I'm busy!");
            break;
        }

        block();
      }

      @Override
      public boolean done() {
        return false;
      }
    });

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
        sd.setType(TaskAdministrator.SERVICE_PREFIX + getServiceName());
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

  protected void setState(State state) {
    this.state = state;
  }

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
