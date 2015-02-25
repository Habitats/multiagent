package b.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import b.ItemManager;
import b.misc.Item;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
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

  private State currentState;
  private final String delimiter = "asdasdasd";

  private enum State {
    SEARCHING_ITEMS

  }

  private List<Item> inventory = new ArrayList<>();
  private List<Item> wanted = new ArrayList<>();
  private int money = 200;

  private static final String SERVICE_PREFIX = "NEGOTIATION";
  private static final String SERVICE_NAME = "yoloswag";

  @Override
  protected void setup() {

    inventory = ItemManager.getInstance().aquireItems();
    wanted = ItemManager.getInstance().wantedItems();

    Log.v(getTag(), inventory.stream().map(v -> v.toString()).collect(Collectors.joining("\n", "\nInventory:\n", "")));
    Log.v(getTag(), wanted.stream().map(v -> v.toString()).collect(Collectors.joining("\n", "\nWant:\n", "")));

    registerWithYellowPages();

    addBehaviour(new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          block();
          return;
        }
        newMessage(msg);

        block();
      }
    });

    addBehaviour(new WakerBehaviour(this, (long) (1000 + 3000 * Math.random())) {
      @Override
      protected void onWake() {
        requestItems();
      }
    });
  }

  private void newMessage(ACLMessage msg) {
    Acl acl = Acl.get(msg.getPerformative());
    switch (acl) {
      case CFP:
        announceItems(msg);
        break;
      case PROPOSE:
        break;
      case REFUSE:
        break;
      case REQUEST:
        break;
      case INFORM:
        announceWantedItems(msg);
        break;
      case CONFIRM:
        break;
      case ACCEPT_PROPOSAL:
        break;
      case QUERY_REF:
        requestItems();
        break;
    }
  }

  private void announceWantedItems(ACLMessage msg) {
    Log.v(getTag(), Arrays.asList(msg.getContent().split(delimiter)).stream().map(v -> Item.fromJson(v))
        .filter(v -> wanted.contains(v)).map(v -> v.toString())
        .collect(Collectors.joining("\n", "\nI want:\n", "\nFrom: " + msg.getSender().getLocalName())));
  }

  private void announceItems(ACLMessage msg) {
    ACLMessage informAboutItems = new ACLMessage(ACLMessage.INFORM);
    informAboutItems.addReceiver(msg.getSender());
    informAboutItems.setContent(inventory.stream().map(v -> Item.toJson(v)).collect(Collectors.joining(delimiter)));
    send(informAboutItems);
  }

  private void generateItems() {

  }

  private void requestItems() {
    List<AID> sellerAgents = getAgentIds();

    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    sellerAgents.forEach(agent -> cfp.addReceiver(agent));

    cfp.setContent("CAN I HAS ITEMS?");
    String conversationId = generateConversationId();
    cfp.setConversationId(conversationId);

    send(cfp);
  }

  private String generateConversationId() {
    return NegotiatingAgent.SERVICE_PREFIX + System.currentTimeMillis() + Math.random();
  }

  private List<AID> getAgentIds() {
    ServiceDescription sd = new ServiceDescription();
    sd.setType(SERVICE_PREFIX + SERVICE_NAME);
    DFAgentDescription template = new DFAgentDescription();
    template.addServices(sd);
    List<DFAgentDescription> result;
    try {
      result = Arrays.asList(DFService.search(this, template));
    } catch (FIPAException e) {
      result = new ArrayList<>();
    }
    return result.stream().map(res -> res.getName()).collect(Collectors.toList());
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
