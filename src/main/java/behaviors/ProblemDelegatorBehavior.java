package behaviors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import agents.TaskAdministrator;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import misc.Log;
import misc.Problem;

/**
 * Created by Patrick on 06.02.2015.
 */
public class ProblemDelegatorBehavior extends Behaviour {

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
      case RECEIVE_PROPOSAL:
        evaluateProposal();
        break;
      case WAITING_FOR_SOLUTION:
        processSolution();
        break;
    }
    block();
  }

  private void announceProblem() {
    List<AID> sellerAgents = getAgentIds(problem);

    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    sellerAgents.forEach(agent -> cfp.addReceiver(agent));

    cfp.setContent(problem.toString());
    conversationId = "math-problem_" + System.currentTimeMillis() + Math.random();
    cfp.setConversationId(conversationId);

    myAgent.send(cfp);

    currentState = State.RECEIVE_PROPOSAL;
  }

  private void evaluateProposal() {
    ACLMessage msg = myAgent.receive();
    if (msg == null) {
      return;
    }

    if (isValidPropose(msg)) {
      int finishTime = Integer.parseInt(msg.getContent());

      if (finishTime >= bestTime) {
        return;
      }

      // this is the best bid
      bestTime = finishTime;
      bestChoice = msg.getSender();

      myAgent.addBehaviour(new WakerBehaviour(myAgent, 1000) {
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
    } else {
      myAgent.send(msg);
    }
  }

  private boolean isValidPropose(ACLMessage msg) {
    return msg.getPerformative() == ACLMessage.PROPOSE && msg.getConversationId().equalsIgnoreCase(conversationId);
  }

  private boolean isValidSolution(ACLMessage msg) {
    return msg.getPerformative() == ACLMessage.INFORM && msg.getSender().getLocalName()
        .equalsIgnoreCase(bestChoice.getLocalName()) && msg.getConversationId().equalsIgnoreCase(conversationId);
  }

  private void processSolution() {
    ACLMessage msg = myAgent.receive();
    if (msg == null) {
      return;
    }
    if (isValidSolution(msg)) {
      currentState = State.DONE;
      problem.solve(msg.getContent());
    } else {
      myAgent.send(msg);
    }
  }

  @Override
  public boolean done() {
    return currentState == State.DONE;
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
}
