package skjennum.behaviors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import skjennum.MessageListener;
import skjennum.agents.NegotiatingAgent;
import skjennum.misc.Inventory;
import skjennum.misc.Item;
import skjennum.misc.Proposal;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * Created by anon on 26.02.2015.
 */
public class NegotiatingBehavior extends Behaviour implements MessageListener {

  public enum State {
    READY, WAITING_FOR_PROPOSAL, WAITING_FOR_PROPOSAL_TIMEOUT, DONE, NEGOTIATING;
  }

  public static final String DELIMITER = "asdasdasd";
  private State currentState;
  private final NegotiatingAgent agent;
  private Inventory inv;

  public NegotiatingBehavior(NegotiatingAgent agent, Inventory inventory) {
    currentState = State.READY;
    this.inv = inventory;
    this.agent = agent;
    agent.addMessageListener(this);
    String has = this.inv.has().stream().map(Item::toString).collect(Collectors.joining("\n", "\nInventory:\n", ""));
    String want = this.inv.want().stream().map(Item::toString).collect(Collectors.joining("\n", "\nWant:\n", ""));
    Log.v(getTag(), has + want);
  }

  @Override
  public void action() {
//    if (agent.getLocalName().equalsIgnoreCase("JOE")) {
//      block();
//      return;
//    }
    if (currentState == State.READY) {
      currentState = State.NEGOTIATING;
      agent.addBehaviour(new TickerBehaviour(agent, 1000) {
        @Override
        protected void onTick() {
          if (agent.done()) {
            announceInventoryItems();
          }
        }
      });
    }
  }

  public void newMessage(ACLMessage msg) {
    switch (msg.getPerformative()) {
      case ACLMessage.QUERY_REF:
        announceInventoryItems();
        break;
      case ACLMessage.CFP:
        processInventoryAnnouncement(msg);
        break;
    }
  }

  private void announceInventoryItems() {
    Log.v(getTag(), String.format("Announcing my inventory %s", inv));
    List<AID> sellerAgents = agent.getAgentIds();
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    cfp.setConversationId(agent.generateConversationId());
    sellerAgents.forEach(cfp::addReceiver);
    cfp.setContent(inv.has().stream().map(Item::toJson).collect(Collectors.joining(DELIMITER)));
    agent.send(cfp);
    agent.addBehaviour(new SellerBehavior(agent, inv, cfp));
  }

  private void processInventoryAnnouncement(ACLMessage msg) {
    agent.addBehaviour(new OneShotBehaviour() {
      @Override
      public void action() {
        List<Item> announcedItems = Arrays.asList(msg.getContent().split(DELIMITER)).stream()//
            .map(Item::fromJson).collect(Collectors.toList());
        Optional<Item> wantedItem = inv.want().stream().filter(announcedItems::contains).findFirst();
//    Log.v(getTag(), String.format("CFP > Received inventory announcement from %s!", msg.getSender().getLocalName()));
        if (wantedItem.isPresent()) {
          Optional<Proposal> proposal = inv.generateProposal(wantedItem.get(), getAgent().getAID());
          if (proposal.isPresent()) {
            agent.addBehaviour(new BuyerBehavior(agent, inv, proposal, msg));
            return;
          }
        }
      }
    });
  }

  private void announceWantedItems(ACLMessage msg) {
    Log.v(getTag(), Arrays.asList(msg.getContent().split(DELIMITER)).stream()//
        .map(Item::fromJson).filter(inv.want()::contains).map(Item::toString)
        .collect(Collectors.joining("\n", "\nI want:\n", "\nFrom: " + msg.getSender().getLocalName())));
  }

  @Override
  public boolean done() {
    boolean empty = inv.want().isEmpty();
    if (empty) {
      Log.v(getTag(), "I'm done! Adios!");
      agent.doDelete();
    }
    return empty;
  }

  private String getTag() {
    return agent.getTag();
  }
}
