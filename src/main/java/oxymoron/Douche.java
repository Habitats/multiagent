package oxymoron;

import java.util.Collections;

import behaviors.Spam;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Created by anon on 04.02.2015.
 */
public class Douche extends Agent {

  @Override
  protected void setup() {
    Collections.nCopies(10, 1).stream().forEach(i -> System.out.println(getAID().getLocalName() + " representin'!"));

    addBehaviour(new Spam());

    addBehaviour(new WakerBehaviour(this, 500) {
      @Override
      protected void handleElapsedTimeout() {
        System.out.println("timeout!");
        addBehaviour(new TickerBehaviour(Douche.this, 2000) {
          @Override
          protected void onTick() {
            System.out.println("tock");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("oxy", AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setOntology("duck-dock-go");
            msg.setContent("howdy sailor");
            send(msg);
          }
        });
      }
    });

    addBehaviour(new TickerBehaviour(this, 2000) {
      @Override
      protected void onTick() {
        System.out.println("tick");
      }
    });


  }
}
