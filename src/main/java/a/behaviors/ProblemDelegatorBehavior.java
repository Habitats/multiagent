package a.behaviors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import a.agents.TaskAdministrator;
import a.misc.Problem;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * This behavior delegates and sends the trivial sub problems to agents for solving. It also handles auctions and
 * bidding, and will issue the next sub problem to the agent with the lowest bid, which is interpreted as the time it
 * will take to solve a given problem.
 */
public class ProblemDelegatorBehavior extends Behaviour {

  private static final int AUCTION_TIMEOUT = 1000;
  private int bestTime = Integer.MAX_VALUE;
  private AID bestChoice;
  private String conversationId;


  private enum State {
    ANNOUNCE, RECEIVE_PROPOSAL, WAITING_FOR_SOLUTION, DONE,
  }

  private State currentState = State.ANNOUNCE;
  private Problem problem;

  public ProblemDelegatorBehavior(Problem problem) {
    this.problem = problem;
  }

  @Override
  public void action() {
    switch (currentState) {
      case ANNOUNCE:
        announceProblem();
        break;
    }
    block();
  }

  public void newMessage(ACLMessage msg) {
    switch (currentState) {
      case RECEIVE_PROPOSAL:
        evaluateProposal(msg);
        break;
      case WAITING_FOR_SOLUTION:
        processSolution(msg);
        break;
    }
  }

  private void announceProblem() {
    List<AID> sellerAgents = getAgentIds(problem);

    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    sellerAgents.forEach(agent -> cfp.addReceiver(agent));

    cfp.setContent(problem.toString());
    conversationId = generateConversationId();
    cfp.setConversationId(conversationId);

    currentState = State.RECEIVE_PROPOSAL;
    myAgent.send(cfp);
  }

  private void evaluateProposal(ACLMessage msg) {

    if (isValidPropose(msg)) {
      int finishTime = Integer.parseInt(msg.getContent());

      if (finishTime >= bestTime) {
        return;
      }

      // this is the best bid
      bestTime = finishTime;
      bestChoice = msg.getSender();

      myAgent.addBehaviour(new WakerBehaviour(myAgent, AUCTION_TIMEOUT) {
        @Override
        protected void handleElapsedTimeout() {
          currentState = State.WAITING_FOR_SOLUTION;
          Log.v(myAgent.getLocalName(),
                bestChoice.getLocalName() + " is chosen as the winner! Requesting a solution ...");
          ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
          accept.addReceiver(bestChoice);
          accept.setContent(problem.toString());
          accept.setConversationId(conversationId);
          myAgent.send(accept);
        }
      });
    }
  }

  private void processSolution(ACLMessage msg) {
    if (isValidSolution(msg)) {
      currentState = State.DONE;
      problem.solve(msg.getContent());
    }
  }

  private boolean isValidPropose(ACLMessage msg) {
    return msg.getPerformative() == ACLMessage.PROPOSE && msg.getConversationId().equalsIgnoreCase(conversationId);
  }

  private boolean isValidSolution(ACLMessage msg) {
    return msg.getPerformative() == ACLMessage.INFORM && msg.getSender().getLocalName()
        .equalsIgnoreCase(bestChoice.getLocalName()) && msg.getConversationId().equalsIgnoreCase(conversationId);
  }

  private String generateConversationId() {
    return TaskAdministrator.SERVICE_PREFIX + System.currentTimeMillis() + Math.random();
  }

  private List<AID> getAgentIds(Problem problem) {
    ServiceDescription sd = new ServiceDescription();
    sd.setType(TaskAdministrator.SERVICE_PREFIX + problem.getType());
    DFAgentDescription template = new DFAgentDescription();
    template.addServices(sd);
    List<DFAgentDescription> result;
    try {
      result = Arrays.asList(DFService.search(myAgent, template));
    } catch (FIPAException e) {
      result = new ArrayList<>();
    }
    return result.stream().map(res -> res.getName()).collect(Collectors.toList());
  }

  @Override
  public boolean done() {
    return currentState == State.DONE;
  }

}
