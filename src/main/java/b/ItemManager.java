package b;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import b.misc.Item;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import util.Log;

/**
 * Created by Patrick on 19.02.2015.
 */
public class ItemManager extends Agent {

  private final int NUMBER_OF_AGENTS = 5;
  private final List<Item> inventory = new ArrayList<>();
  private final List<List<Item>> inventorySubLists = new ArrayList<>();
  private final List<List<Item>> wantedSubLists = new ArrayList<>();

  private int id = 0;
  private String SERVICE_PREFIX = "ITEM_MANAGER";
  private String SERVICE_NAME = "Harold";


  @Override
  protected void setup() {
    generateItems();
  }

  public void generateItems() {

    Collections.nCopies(100, null).forEach(i -> inventory.add(Item.create()));
    int start = 0;
    int end = inventory.size() / NUMBER_OF_AGENTS;
    for (int i = 0; i < NUMBER_OF_AGENTS; i += inventory.size() / NUMBER_OF_AGENTS) {
      inventorySubLists.add(inventory.subList(start, end));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
    Collections.shuffle(inventory);
    for (int i = 0; i < NUMBER_OF_AGENTS; i += inventory.size() / NUMBER_OF_AGENTS) {
      wantedSubLists.add(inventory.subList(start, end));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
  }

  public List<Item> aquireItems() {
    return inventorySubLists.remove(0);
  }

  public List<Item> wantedItems() {
    return wantedSubLists.remove(0);
  }

  public int getUniqueId() {
    return id++;
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
          DFService.register(ItemManager.this, dfd);
        } catch (FIPAException fe) {
          fe.printStackTrace();
        }
      }
    });
  }

  private String getTag() {
    return getLocalName();
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
