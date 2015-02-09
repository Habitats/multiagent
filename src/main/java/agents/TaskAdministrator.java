package agents;

import java.util.ArrayDeque;
import java.util.Deque;

import behaviors.ProblemSplitterBehavior;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import misc.Log;
import misc.Problem;

/**
 * Created by anon on 04.02.2015.
 */
public class TaskAdministrator extends Agent {

  public static final String SERVICE_PREFIX = "math-solver";

  Deque<Problem> problems = new ArrayDeque<>();

  @Override
  protected void setup() {
    // Printout a welcome message
    Log.v(getTag(), "Hello! Buyer-agent " + getAID().getName() + " is ready.");

    addSampleProblems();
    enableQueryRefProcessing();
  }

  private void addSampleProblems() {
    // if no args, create dummy problems
    Object[] args = getArguments();
    if (args == null || args.length == 0) {
      Log.v(getTag(), "Adding some dummy problems ...");

      for (int i = 0; i < 3; i++) {
        problems.add(new Problem("- * / 15 - 7 + 1 1 3 + 2 + 1 1"));
      }
    }

    problems.forEach(p -> {
      p = problems.removeFirst();
      Log.v(getTag(), "New problem: " + p);
      addBehaviour(new ProblemSplitterBehavior(p));
    });
  }

  private void enableQueryRefProcessing() {
    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage query = myAgent.receive();
        if (query == null) {
          return;
        }
        if (query.getPerformative() == ACLMessage.QUERY_REF) {
          addBehaviour(new ProblemSplitterBehavior(new Problem(query.getContent())));
        } else {
          // didn't want this message, passing it forward!
          send(query);
        }

        block();
      }
    });
  }


  @Override
  protected void takeDown() {
    super.takeDown();
  }


  private String getTag() {
    return getLocalName();
  }
}
