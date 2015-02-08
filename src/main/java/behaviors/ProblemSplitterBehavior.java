package behaviors;

import jade.core.behaviours.Behaviour;
import misc.Problem;

/**
 * Created by anon on 08.02.2015.
 */
public class ProblemSplitterBehavior extends Behaviour {


  private enum State {
    WAITING_FOR_ANSWER, READY, PARTIALLY_SOLVED, SOLVED
  }

  private State state = State.READY;
  private final Problem problem;
  private ProblemDelegatorBehavior problemDelegatorBehavior;
  private final String problemDescription;

  public ProblemSplitterBehavior(Problem problem) {
    this.problem = problem;
    problemDescription = problem.toString();
  }

  @Override
  public void action() {

    switch (state) {
      case READY:
        Problem subProblem = problem.getSubproblem();
        System.out.println("Requesting solving of subproblem: " + subProblem);
        problemDelegatorBehavior = new ProblemDelegatorBehavior(subProblem);
        myAgent.addBehaviour(problemDelegatorBehavior);

        state = State.WAITING_FOR_ANSWER;
        break;
      case WAITING_FOR_ANSWER:
        if (problemDelegatorBehavior.done()) {
          System.out.println("Partial problem solved ...");
          state = State.PARTIALLY_SOLVED;
        }
        break;
      case PARTIALLY_SOLVED:
        if (problem.isSolved()) {
          System.out.println("Problem " + problemDescription + " successfully solved! The answer was " + problem);
          state = State.SOLVED;
        } else {
          state = State.READY;
//          myAgent.addBehaviour(new ProblemSolverBehavior(problem));

        }
    }
  }

  @Override
  public boolean done() {
    return state == State.SOLVED;
  }
}

