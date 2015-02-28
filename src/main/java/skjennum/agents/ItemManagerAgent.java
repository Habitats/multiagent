package skjennum.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import skjennum.misc.Inventory;
import skjennum.misc.Item;
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
 * Created by Patrick on 19.02.2015.
 */
public class ItemManagerAgent extends Agent {

  public static final String SERVICE_NAME = "ITEM_MANAGER";
  private final int NUMBER_OF_AGENTS = 3;
  private final int inventorySize = NUMBER_OF_AGENTS * 10;
  private final List<Item> inventory = new ArrayList<>();
  private final List<List<Item>> inventorySubLists = new ArrayList<>();
  private final List<List<Item>> wantedSubLists = new ArrayList<>();

  private String
      nouns =
      "apple\n" + "arm\n" + "banana\n" + "bike\n" + "bird\n" + "book\n" + "chin\n" + "clam\n" + "class\n" + "clover\n"
      + "club\n" + "corn\n" + "crayon\n" + "crow\n" + "crown\n" + "crowd\n" + "crib\n" + "desk\n" + "dime\n" + "dirt\n"
      + "dress\n" + "fang \n" + "field\n" + "flag\n" + "flower\n" + "fog\n" + "game\n" + "heat\n" + "hill\n" + "home\n"
      + "horn\n" + "hose\n" + "joke\n" + "juice\n" + "kite\n" + "lake\n" + "maid\n" + "mask\n" + "mice\n" + "milk\n"
      + "mint\n" + "meal\n" + "meat\n" + "moon\n" + "mother\n" + "morning\n" + "name\n" + "nest\n" + "nose\n" + "pear\n"
      + "pen\n" + "pencil\n" + "plant\n" + "rain\n" + "river\n" + "road\n" + "rock\n" + "room\n" + "rose\n" + "seed\n"
      + "shape\n" + "shoe\n" + "shop\n" + "show\n" + "sink\n" + "snail\n" + "snake\n" + "snow\n" + "soda\n" + "sofa\n"
      + "star\n" + "step\n" + "stew\n" + "stove\n" + "straw\n" + "string\n" + "summer\n" + "swing\n" + "table\n"
      + "tank\n" + "team\n" + "tent\n" + "test\n" + "toes\n" + "tree\n" + "vest\n" + "water\n" + "wing\n" + "winter\n"
      + "woman" + "ball\n" + "bat\n" + "bed\n" + "book\n" + "boy\n" + "bun\n" + "can\n" + "cake\n" + "cap\n" + "car\n"
      + "cat\n" + "cow\n" + "cub\n" + "cup\n" + "dad\n" + "day\n" + "dog\n" + "doll\n" + "dust\n" + "fan\n" + "feet\n"
      + "girl\n" + "gun\n" + "hall\n" + "hat\n" + "hen\n" + "jar\n" + "kite\n" + "man\n" + "map\n" + "men\n" + "mom\n"
      + "pan\n" + "pet\n" + "pie\n" + "pig\n" + "pot\n" + "rat\n" + "son\n" + "sun\n" + "toe\n" + "tub\n" + "van";

  private static int id = 0;
  private List<String> nounsList;


  @Override
  protected void setup() {
    registerWithYellowPages();
    generateItems();
    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          block();
          return;
        }
        if (msg.getPerformative() == ACLMessage.REQUEST) {
          ACLMessage res = new ACLMessage(ACLMessage.INFORM);
          Inventory inv = Inventory.create(acquireInventoryItems(), acquireWantedItems());
          res.setContent(Inventory.toJson(inv));
          res.addReceiver(msg.getSender());
          send(res);
        }
      }
    });
  }


  public void generateItems() {
    nounsList = Arrays.asList(nouns.split("\\s")).stream().collect(Collectors.toList());
    Collections.shuffle(nounsList);

    Collections.nCopies(inventorySize, null).forEach(i -> inventory.add(Item.create(nounsList.remove(0))));
    int start = 0;
    int end = inventory.size() / NUMBER_OF_AGENTS;
    for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
      inventorySubLists.add(inventory.subList(start, end).stream().collect(Collectors.toList()));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
    start = 0;
    end = inventory.size() / NUMBER_OF_AGENTS;
    Collections.shuffle(inventory);
    for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
      wantedSubLists.add(inventory.subList(start, end).stream().collect(Collectors.toList()));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
  }

  public List<Item> acquireInventoryItems() {
    return inventorySubLists.remove(0);
  }

  public List<Item> acquireWantedItems() {
    return wantedSubLists.remove(0).stream().map(Item::wantedCopy).collect(Collectors.toList());
  }

  public static int generateUniqueId() {
    return id++;
  }

  private void registerWithYellowPages() {
    addBehaviour(new OneShotBehaviour() {
      @Override
      public void action() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_NAME);
        sd.setName(SERVICE_NAME);
        dfd.addServices(sd);

        Log.v(getTag(), "Registered with YellowPages!");

        try {
          DFService.register(ItemManagerAgent.this, dfd);
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
