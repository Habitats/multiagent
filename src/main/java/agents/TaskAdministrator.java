package agents;

import java.util.ArrayDeque;
import java.util.Deque;

import behaviors.ProblemSplitterBehavior;
import jade.core.Agent;
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
    System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");

    // if no args, create dummy problems
    Object[] args = getArguments();
    if (args == null || args.length == 0) {
      System.out.println("Adding some dummy problems ...");
//      problems.add(new Problem("+ - 3 9 * 5 1"));
//      problems.add(new Problem("+ * 5 2 - 7 2"));
      problems.add(new Problem("- * / 15 - 7 + 1 1 3 + 2 + 1 1"));
    }

    problems.forEach(p -> {
      p = problems.removeFirst();
      System.out.println("Announcing problem: " + p);
      addBehaviour(new ProblemSplitterBehavior(p));
    });

  }


  @Override
  protected void takeDown() {
    super.takeDown();
  }


}
