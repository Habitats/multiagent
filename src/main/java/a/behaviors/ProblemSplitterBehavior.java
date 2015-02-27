package a.behaviors;

import a.misc.Problem;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * This behavior decomposes a more complex problem into smaller parts. After having split up the problem, it sends each
 * of the sub problems to the delegator. It then proceeds to keep track of the overall solving process, and finally
 * reports back the final solution.
 */
public class ProblemSplitterBehavior extends Behaviour {

  private enum State {
    WAITING_FOR_ANSWER, READY, PARTIALLY_SOLVED, SOLVED
  }

  private State state = State.READY;
  private ProblemDelegatorBehavior problemDelegatorBehavior;
  private final Problem problem;
  private final String problemDescription;

  public ProblemSplitterBehavior(Problem problem) {
    this.problem = problem;
    this.problemDescription = problem.toString();
  }

  @Override
  public void action() {
    switch (state) {
      case READY:
        assignSubProblem();
        break;
      case WAITING_FOR_ANSWER:
        checkProgress();
        break;
      case PARTIALLY_SOLVED:
        evaluateSolution();
        break;
    }
  }

  public void newMessage(ACLMessage query) {
    problemDelegatorBehavior.newMessage(query);
  }

  private void assignSubProblem() {
    Problem subProblem = problem.getSubproblem();
    Log.v(myAgent.getLocalName(), "Current state of full problem: " + problem);
    Log.v(myAgent.getLocalName(), "Requesting solving of subproblem: " + subProblem);
    problemDelegatorBehavior = new ProblemDelegatorBehavior(subProblem);
    myAgent.addBehaviour(problemDelegatorBehavior);

    state = State.WAITING_FOR_ANSWER;
  }

  private void checkProgress() {
    if (problemDelegatorBehavior.done()) {
      Log.v(myAgent.getLocalName(), "Partial problem solved ...");
      state = State.PARTIALLY_SOLVED;
    }
  }

  private void evaluateSolution() {
    if (problem.isSolved()) {
      Log.v(myAgent.getLocalName(),
            "Problem " + problemDescription + " successfully solved! The answer was " + problem);
      state = State.SOLVED;
    } else {
      state = State.READY;
    }
  }

  @Override
  public boolean done() {
    return state == State.SOLVED;
  }
}

