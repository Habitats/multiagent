package oxymoron;


import java.util.Collections;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public class Oxymoron extends Agent {

  @Override
  protected void setup() {
    AID id = new AID(Oxymoron.class.getSimpleName(), AID.ISLOCALNAME);

    Collections.nCopies(10, 1).stream().forEach(i -> System.out.println(getAID().getLocalName() + " representin'!"));

    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          return;
        }

        System.out.println("Received message: " + msg.getContent());


      }
    });
  }

  @Override
  protected void takeDown() {
    super.takeDown();
  }

}
