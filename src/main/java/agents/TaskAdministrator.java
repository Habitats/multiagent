package agents;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import behaviors.ProblemSplitterBehavior;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import misc.Log;
import misc.Problem;

/**
 * The TaskAdministrator class is the brain of the problem solving process. It decomposes problems into subproblems,
 * that can easily be solved by simpler agents.
 */
public class TaskAdministrator extends Agent {

  public static final String SERVICE_PREFIX = "math-solver";

  private Deque<Problem> problems = new ArrayDeque<>();
  private List<ProblemSplitterBehavior> jobs = new ArrayList<>();

  @Override
  protected void setup() {
    // Printout a welcome message
    Log.v(getTag(), "Hello! Buyer-agent " + getAID().getName() + " is ready.");

    addSampleProblems();
    enableQueryRefProcessing();
  }

  private void addSampleProblems() {
    // create some dummy problems for easy testing
    Log.v(getTag(), "Adding some dummy problems ...");

    for (int i = 0; i < 1; i++) {
      problems.add(new Problem("- + - 15 - 7 1 3 + 1 1"));
//      problems.add(new Problem("+ + + 15 3 + 2 1 1"));
//      problems.add(new Problem("+ 7 + 1 2 + 1"));
//      problems.add(new Problem("+ + + 1 1 + 2 + 4 1 1"));
    }

    problems.forEach(p -> {
      p = problems.removeFirst();
      Log.v(getTag(), "New problem: " + p);
      ProblemSplitterBehavior b = new ProblemSplitterBehavior(p);
      addBehaviour(b);
      jobs.add(b);
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
          ProblemSplitterBehavior b = new ProblemSplitterBehavior(new Problem(query.getContent()));
          addBehaviour(b);
          jobs.add(b);
        } else {
          // didn't want this message, passing it forward!
          jobs.stream().forEach(b -> b.newMessage(query));
          System.out.println("lol");
        }

        block();
      }
    });
  }


  @Override
  protected void takeDown() {
    Log.v(getTag(), "Shutting down!");
  }


  private String getTag() {
    return getLocalName();
  }
}
