package behaviors;

import jade.core.behaviours.Behaviour;
import misc.Log;
import misc.Problem;

/**
 * Created by anon on 08.02.2015.
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

  private void assignSubProblem() {
    Problem subProblem = problem.getSubproblem();
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

