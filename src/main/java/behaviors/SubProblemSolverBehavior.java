package behaviors;

import jade.core.behaviours.Behaviour;
import misc.Problem;

/**
 * Created by anon on 08.02.2015.
 */
public class SubProblemSolverBehavior extends Behaviour {


  private enum State {
    WAITING_FOR_ANSWER, READY, PARTIALLY_SOLVED, SOLVED
  }

  private State state = State.READY;
  private final Problem problem;
  private ProblemSolverBehavior problemSolverBehavior;
  private final String problemDescription;

  public SubProblemSolverBehavior(Problem problem) {
    this.problem = problem;
    problemDescription = problem.toString();
  }

  @Override
  public void action() {

    switch (state) {
      case READY:
        Problem subProblem = problem.getSubproblem();
        System.out.println("Requesting solving of subproblem: " + subProblem);
        problemSolverBehavior = new ProblemSolverBehavior(subProblem);
        myAgent.addBehaviour(problemSolverBehavior);

        state = State.WAITING_FOR_ANSWER;
        break;
      case WAITING_FOR_ANSWER:
        if (problemSolverBehavior.done()) {
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

