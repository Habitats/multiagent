package a.agents;

import java.util.HashMap;
import java.util.Map;

import a.misc.Problem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * This is an abstract solver agent which inherits most of the functionality for the simple solver agents.
 */
public abstract class AbstractSolverAgent extends Agent {

  private int runningJobs = 0;
  private int finishedJobs = 0;
  protected final int EXECUTION_CONSTANT = 3000;
  private Map<String, Problem> problems = new HashMap<>();
  private Map<String, Integer> proposals = new HashMap<>();

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

  @Override
  protected void setup() {
    registerWithYellowPages();

    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          block();
          return;
        }

        String conversationId = msg.getConversationId();
        if (msg.getPerformative() == ACLMessage.CFP) {
          processCfp(msg, conversationId);
        } else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL && proposals.containsKey(conversationId)) {
          initiateProblemSolving(msg, conversationId);
        }

        block();
      }
    });
  }

  private void initiateProblemSolving(ACLMessage msg, String conversationId) {
    Log.v(getTag(), "My proposal was accepted -- attempting to solve: " + problems.get(conversationId));
    problemReceived(problems.get(conversationId));
    addBehaviour(new ProblemSolverBehavior(this, proposals.get(conversationId), conversationId, msg.getSender()));
  }

  private void processCfp(ACLMessage msg, String conversationId) {
    int executionEstimate = getExecutionEstimate(msg.getContent());
    Log.v(getTag(), "I can solve this! Problem: " + msg.getContent() + " -- Bidding: " + executionEstimate);
    ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);
    proposals.put(conversationId, executionEstimate);
    problems.put(conversationId, new Problem(msg.getContent()));
    reply.setContent(String.valueOf(executionEstimate));
    reply.setConversationId(conversationId);
    reply.addReceiver(msg.getSender());
    send(reply);
  }

  public String getTag() {
    return getLocalName();
  }

  protected int getExecutionEstimate(String content) {
    return runningJobs * EXECUTION_CONSTANT + finishedJobs * EXECUTION_CONSTANT / 100;
  }

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

        Log.v(getTag(), "Registered with YellowPages!");

        try {
          DFService.register(AbstractSolverAgent.this, dfd);
        } catch (FIPAException fe) {
          fe.printStackTrace();
        }
      }
    });
  }

  protected abstract void problemReceived(Problem problem);

  protected abstract String getServiceName();

  @Override
  protected void takeDown() {
    // Deregister from the yellow pages
    try {
      DFService.deregister(this);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }

    Log.v(getTag(), "Going down!");
  }

  /**
   * An internal class for a behavior for actually solving a sub problem. An agent can have multiple instances of this
   * behavior at the same time, for several sub problems it is solving. Thus, an object with a state is required.
   */
  private class ProblemSolverBehavior extends WakerBehaviour {

    private final String conversationId;
    private AID sender;

    public ProblemSolverBehavior(Agent myAgent, int executionEstimate, String conversationId, AID sender) {
      super(myAgent, executionEstimate);
      this.conversationId = conversationId;
      this.sender = sender;
      runningJobs++;
      finishedJobs++;
    }

    @Override
    public void handleElapsedTimeout() {
      ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
      Problem currentProblem = problems.get(conversationId);
      reply.setContent(currentProblem.getValue());
      reply.addReceiver(sender);
      reply.setConversationId(conversationId);
      myAgent.send(reply);
      runningJobs--;
    }
  }
}
