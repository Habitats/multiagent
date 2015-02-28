package skjennum.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import skjennum.MessageListener;
import skjennum.behaviors.BuyerBehavior;
import skjennum.behaviors.NegotiatingBehavior;
import skjennum.misc.Inventory;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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

  private static final String ITEM_MANAGER_SERVICE = "ITEM_MANAGER";
  private static final String SERVICE_PREFIX = "NEGOTIATION";
  private static final String SERVICE_NAME = "yoloswag";
  private List<MessageListener> listeners = new ArrayList<>();

  @Override
  protected void setup() {
    registerWithYellowPages();
    addBehaviour(createMessageManagerBehavior());
    addBehaviour(createInventoryAquisitionBehavior());
  }

  private WakerBehaviour createInventoryAquisitionBehavior() {
    return new WakerBehaviour(this, 5000) {
      @Override
      protected void onWake() {
        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
        req.addReceiver(getItemManager());
        send(req);
      }
    };
  }

  private CyclicBehaviour createMessageManagerBehavior() {
    return new CyclicBehaviour() {
      @Override
      public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
          block();
          return;
        }
        if (msg.getPerformative() == ACLMessage.INFORM) {
          addBehaviour(new WakerBehaviour(NegotiatingAgent.this, 1000) {
            @Override
            protected void onWake() {
              addBehaviour(new NegotiatingBehavior(NegotiatingAgent.this, Inventory.fromJson(msg.getContent())));
            }
          });
        } else {
          listeners.stream().forEach(l -> l.newMessage(msg));
        }
      }
    };
  }

  private AID getItemManager() {
    ServiceDescription sd = new ServiceDescription();
    sd.setType(ITEM_MANAGER_SERVICE);
    DFAgentDescription template = new DFAgentDescription();
    template.addServices(sd);
    List<DFAgentDescription> result;
    try {
      result = Arrays.asList(DFService.search(this, template));
    } catch (FIPAException e) {
      result = new ArrayList<>();
    }
    return result.get(0).getName();
  }

  public List<AID> getAgentIds() {
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
    return result.stream().map(DFAgentDescription::getName).filter(name -> !name.getLocalName().equals(getLocalName()))
        .collect(Collectors.toList());
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

  public String generateConversationId() {
    return System.currentTimeMillis() + ":" + Math.random();
  }

  public void addMessageListener(MessageListener messageListener) {
    listeners.add(messageListener);
  }

  public void removeMessageListener(MessageListener messageListener) {
    listeners.remove(messageListener);
  }

  public boolean done() {
    Predicate<MessageListener> isRelevant = v -> !(v instanceof NegotiatingBehavior) && !(v instanceof BuyerBehavior);
    Predicate<MessageListener> isRunning = v -> !((Behaviour) v).done();
    return listeners.size() == 1 || listeners.stream().filter(isRelevant).noneMatch(isRunning);
  }
}