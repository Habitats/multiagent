package b.agents;

import java.util.ArrayList;
import java.util.List;

import b.misc.Item;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * This is an abstract solver agent which inherits most of the functionality for the simple solver agents.
 */
public class NegotiatingAgent extends Agent {

  private List<Item> inventory = new ArrayList<>();
  private List<Item> wanted = new ArrayList<>();
  private int money = 0;

  private static final String SERVICE_PREFIX = "NEGOTIATION";
  private static final String SERVICE_NAME = String.format("%3", (int) (Math.random() * 100));

  @Override
  protected void setup() {
//    inventory = ItemManager.getInstance().aquireItems();
//    wanted = ItemManager.getInstance().wantedItems();

    System.out.println("Inventory:");
    inventory.forEach(i -> System.out.println(i));
    System.out.println("Wanted:");
    wanted.forEach(i -> System.out.println(i));

    registerWithYellowPages();

    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          block();
          return;
        }

        block();
      }
    });
  }

  private void generateItems() {

  }


  public String getTag() {
    return getLocalName();
  }


  private void registerWithYellowPages() {
    addBehaviour(new OneShotBehaviour() {
      @Override
      public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_PREFIX + SERVICE_NAME);
        sd.setName(SERVICE_NAME);
        dfd.addServices(sd);

        Log.v(getTag(), "Registered with YellowPages!");

        try {
          DFService.register(NegotiatingAgent.this, dfd);
        } catch (FIPAException fe) {
          fe.printStackTrace();
        }
      }
    });
  }


  @Override
  protected void takeDown() {
    // Deregister from the yellow pages
    try {
      DFService.deregister(this);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }

    Log.v(getTag(), "Going down!");
  }

}
