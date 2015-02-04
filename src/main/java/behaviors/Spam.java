package behaviors;

import jade.core.behaviours.Behaviour;

/**
 * Created by anon on 04.02.2015.
 */
public class Spam extends Behaviour {

  private int step = 0;
  @Override
  public void action() {
    switch (step){
      case 0:
        step++;
        System.out.println("action!");
        break;
      case 1:
        step++;
        System.out.println("faction!");
        break;
      case 2:
        step++;
        System.out.println("reaction!");
        break;
    }
  }

  @Override
  public boolean done() {
    return step == 4;
  }
}
