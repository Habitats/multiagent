package b.behaviors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import b.MessageListener;
import b.agents.Acl;
import b.agents.NegotiatingAgent;
import b.misc.Inventory;
import b.misc.Item;
import b.misc.Proposal;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * Created by anon on 26.02.2015.
 */
public class NegotiatingBehavior extends Behaviour implements MessageListener {

  private enum State {
    NEGOTIATING, ANNOUNCING, WAITING_FOR_PROPOSAL, EVALUATE_PROPOSAL, SEARCHING
  }


  private State currentState;
  private final String delimiter = "asdasdasd";
  private final NegotiatingAgent agent;
  private Inventory inv;

  public NegotiatingBehavior(NegotiatingAgent agent) {
    this.agent = agent;
    agent.addMessageListener(this);
    inv = Inventory.create();
    Log.v(getTag(), inv.has().stream().map(Item::toString).collect(Collectors.joining("\n", "\nInventory:\n", "")));
    Log.v(getTag(), inv.want().stream().map(Item::toString).collect(Collectors.joining("\n", "\nWant:\n", "")));
  }


  private void processAnnouncement(ACLMessage msg) {
    List<Item> announcedItems = Arrays.asList(msg.getContent().split(delimiter)).stream()//
        .map(Item::fromJson).collect(Collectors.toList());
    Optional<Item> wantedItem = inv.want().stream().filter(announcedItems::contains).findFirst();
    if (wantedItem.isPresent()) {
      ACLMessage proposalMsg = new ACLMessage(ACLMessage.PROPOSE);
      proposalMsg.addReceiver(proposalMsg.getSender());

      Proposal proposal = inv.generateOffer(wantedItem.get(), proposalMsg.getSender());
      proposalMsg.setContent(Proposal.toJson(proposal));
      agent.send(proposalMsg);
    }
  }

  private void announceWantedItems(ACLMessage msg) {
    Log.v(getTag(), Arrays.asList(msg.getContent().split(delimiter)).stream()//
        .map(Item::fromJson).filter(inv.want()::contains).map(Item::toString)
        .collect(Collectors.joining("\n", "\nI want:\n", "\nFrom: " + msg.getSender().getLocalName())));
  }

  private void announceItems() {
    List<AID> sellerAgents = agent.getAgentIds();
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    sellerAgents.forEach(cfp::addReceiver);
    cfp.setContent(inv.has().stream().map(Item::toJson).collect(Collectors.joining(delimiter)));
    agent.send(cfp);
    agent.addBehaviour(new WakerBehaviour(agent, 1000) {
      @Override
      protected void onWake() {
        evaluateProposals();
      }
    });
  }


  @Override
  public void action() {
    agent.addBehaviour(new TickerBehaviour(agent, 2000) {

      @Override
      protected void onTick() {
        if (currentState == State.SEARCHING) {
          announceItems();
          currentState = State.NEGOTIATING;
        }
      }
    });
  }

  private void evaluateProposals() {
    Optional<Proposal> bestProposal = inv.getBestProposal();
    if (bestProposal.isPresent()) {
      ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      msg.addReceiver(bestProposal.get().getProposingAgent());
    }
  }


  @Override
  public boolean done() {
    return inv.want().isEmpty();
  }

  private String getTag() {
    return agent.getTag();
  }

  public void newMessage(ACLMessage msg) {
    switch (Acl.get(msg.getPerformative())) {
      case ANNOUNCEMENT:
        processAnnouncement(msg);
        break;
      case PROPOSE_OFFER:
        inv.addProposal(Proposal.fromJson(msg.getContent()));
        break;
      case OFFER_DECLINED:
        declinedProposal(msg);
        break;
      case REQUEST:
        break;
      case INFORM:
        announceWantedItems(msg);
        break;
      case CONFIRM:
        break;
      case ACCPET_PROPOSAL:
        acceptedProposal(msg);
        break;
      case QUERY_REF:
        announceItems();
        break;
    }
  }

  private void acceptedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    inv.accepted(proposal);
  }

  private void declinedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    Optional<Proposal> newProposal = inv.generateBetterProposal(proposal);
    if (newProposal.isPresent()) {
      ACLMessage newProposalMsg = new ACLMessage(ACLMessage.PROPOSE);
      newProposalMsg.addReceiver(msg.getSender());
      newProposalMsg.setContent(Proposal.toJson(proposal));
    } else {
      ACLMessage giveUp = new ACLMessage(ACLMessage.CANCEL);
      giveUp.addReceiver(msg.getSender());
      inv.declined(proposal);
      agent.send(giveUp);
    }
  }
}
