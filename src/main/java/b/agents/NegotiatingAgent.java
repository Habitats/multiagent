package b.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import b.MessageListener;
import b.behaviors.NegotiatingBehavior;
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


  private List<MessageListener> listeners;

  public void addMessageListener(MessageListener negotiatingBehavior) {
    listeners.add(negotiatingBehavior);
  }


  private static final String SERVICE_PREFIX = "NEGOTIATION";
  private static final String SERVICE_NAME = "yoloswag";

  @Override
  protected void setup() {
    listeners = new ArrayList<>();

    registerWithYellowPages();
    addBehaviour(createMessageManagerBehavior());
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
        listeners.forEach(l -> l.newMessage(msg));
        block();
      }
    };
  }


  private String generateConversationId() {
    return NegotiatingAgent.SERVICE_PREFIX + System.currentTimeMillis() + Math.random();
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
    return result.stream().map(DFAgentDescription::getName).collect(Collectors.toList());
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

        addBehaviour(new WakerBehaviour(NegotiatingAgent.this, 1000) {
          @Override
          protected void onWake() {
            addBehaviour(new NegotiatingBehavior(NegotiatingAgent.this));
          }
        });
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
