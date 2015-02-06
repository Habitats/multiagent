package behaviors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import misc.Problem;

/**
 * Created by Patrick on 06.02.2015.
 */
public class ProblemSolverBehavior extends Behaviour {

  private boolean isDone = false;

  private enum State {
    ANNOUNCE, RECEIVE_PROPOSAL, WAITING_FOR_SOLUTION, DONE,
  }

  private State currentState = State.ANNOUNCE;
  private Problem problem;

  public ProblemSolverBehavior(Problem problem) {
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
      case DONE:
        isDone = true;
        break;
    }
    block();
  }

  private void announceProblem() {
    List<AID> sellerAgents = getAgentIds();

    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    sellerAgents.forEach(agent -> cfp.addReceiver(agent));

    cfp.setContent(problem.toString());
    cfp.setConversationId("math-problem");

    myAgent.send(cfp);

    currentState = State.RECEIVE_PROPOSAL;
  }

  private void evaluateProposal() {
    ACLMessage reply = myAgent.receive();
    if (reply != null && reply.getPerformative() == ACLMessage.PROPOSE) {
      AID bestChoice = reply.getSender();
      currentState = State.WAITING_FOR_SOLUTION;

      ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      accept.addReceiver(bestChoice);
      accept.setContent(problem.toString());
      myAgent.send(accept);
    }
  }


  private void processSolution() {
    ACLMessage reply = myAgent.receive();
    if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
      problem.solve(reply.getContent());
      currentState = State.DONE;
    }
  }

  @Override
  public boolean done() {
    return isDone;
  }

  private List<AID> getAgentIds() {
    ServiceDescription sd = new ServiceDescription();
    sd.setType("math-solver");
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
