package agents;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public class TaskAdministrator extends Agent {

  Deque<Problem> problems = new ArrayDeque<>();

  @Override
  protected void setup() {
    // Printout a welcome message
    System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");

    // if no args, create dummy problems
    Object[] args = getArguments();
    if (args == null || args.length == 0) {
      System.out.println("Adding some dummy problems ...");
      problems.add(new Problem("+ - 3 9 * 5 1"));
      problems.add(new Problem("+ * 5 2 - 7 2"));
    }

    problems.forEach(problem -> addBehaviour(new TickerBehaviour(this, 10000) {
      protected void onTick() {

        List<AID> sellerAgents = getAgentIds();
        Problem problem = problems.removeFirst();
        while (!problem.isTerminal()) {
          Problem subProblem = problem.getSubproblem();
          addBehaviour(composeProblemMessage(sellerAgents, subProblem));
        }
      }

    }));

  }

  private OneShotBehaviour composeProblemMessage(final List<AID> sellerAgents, final Problem problem) {
    return new OneShotBehaviour() {
      @Override
      public void action() {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        sellerAgents.forEach(agent -> cfp.addReceiver(agent));

        cfp.setContent(problem.toString());
        cfp.setConversationId("math-problem");

        TaskAdministrator.this.send(cfp);
      }
    };
  }

  private List<AID> getAgentIds() {
    ServiceDescription sd = new ServiceDescription();
    sd.setType("math-solver");
    DFAgentDescription template = new DFAgentDescription();
    template.addServices(sd);
    List<DFAgentDescription> result = null;
    try {
      result = Arrays.asList(DFService.search(this, template));
    } catch (FIPAException e) {
      result = new ArrayList<>();
    }
    return result.stream().map(res -> res.getName()).collect(Collectors.toList());
  }

  @Override
  protected void takeDown() {
    super.takeDown();
  }
}
