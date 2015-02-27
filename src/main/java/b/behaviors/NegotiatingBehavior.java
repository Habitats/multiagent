package b.behaviors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import b.MessageListener;
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
    NEGOTIATING, SEARCHING, WAITING_FOR_PROPOSAL;
  }

  private State currentState;
  public static final String DELIMITER = "asdasdasd";
  private final NegotiatingAgent agent;
  private Inventory inv;

  public NegotiatingBehavior(NegotiatingAgent agent, Inventory inventory) {
    this.inv = inventory;
    this.agent = agent;
    agent.addMessageListener(this);
    String has = this.inv.has().stream().map(Item::toString).collect(Collectors.joining("\n", "\nInventory:\n", ""));
    String want = this.inv.want().stream().map(Item::toString).collect(Collectors.joining("\n", "\nWant:\n", ""));
    Log.v(getTag(), has + want);
  }

  @Override
  public void onStart() {
    currentState = State.SEARCHING;
  }

  @Override
  public void action() {
    agent.addBehaviour(new TickerBehaviour(agent, 5000) {
      @Override
      protected void onTick() {
        if (currentState == State.SEARCHING) {
          announceInventoryItems();
          currentState = State.NEGOTIATING;
        }
      }
    });

    block();
  }

  public void newMessage(ACLMessage msg) {
    switch (msg.getPerformative()) {
      case ACLMessage.QUERY_REF:
        announceInventoryItems();
        break;
      case ACLMessage.CFP:
        processInventoryAnnouncement(msg);
        break;
      case ACLMessage.PROPOSE:
        receivedProposal(msg);
        break;
      case ACLMessage.REJECT_PROPOSAL:
        declinedProposal(msg);
        break;
      case ACLMessage.ACCEPT_PROPOSAL:
        acceptedProposal(msg);
        break;
      case ACLMessage.CANCEL:
        currentState = State.SEARCHING;
    }
  }

  private void announceInventoryItems() {

    Log.v(getTag(), String.format("Announcing my inventory %s", inv));
    currentState = State.NEGOTIATING;
    List<AID> sellerAgents = agent.getAgentIds();
    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
    sellerAgents.forEach(cfp::addReceiver);
    cfp.setContent(inv.has().stream().map(Item::toJson).collect(Collectors.joining(DELIMITER)));
    agent.send(cfp);
    agent.addBehaviour(new WakerBehaviour(agent, 1000) {
      @Override
      protected void onWake() {
        evaluateProposals();
      }
    });

  }

  private void processInventoryAnnouncement(ACLMessage msg) {
    List<Item> announcedItems = Arrays.asList(msg.getContent().split(DELIMITER)).stream()//
        .map(Item::fromJson).collect(Collectors.toList());
    Optional<Item> wantedItem = inv.want().stream().filter(announcedItems::contains).findFirst();
//    Log.v(getTag(), String.format("CFP > Received inventory announcement from %s!", msg.getSender().getLocalName()));
    if (wantedItem.isPresent()) {

      Optional<Proposal> proposal = inv.generateOffer(wantedItem.get(), getAgent().getAID());
      if (proposal.isPresent()) {
        Log.v(getTag(), String.format("PROPOSE > Attempting to buy %s from %s! for %s and %d", //
                                      proposal.get().getIemToBuy().getName(), msg.getSender().getLocalName(),
                                      proposal.get().getProposedItem().getName(), proposal.get().getDelta()));
        ACLMessage proposalMsg = new ACLMessage(ACLMessage.PROPOSE);
        proposalMsg.addReceiver(msg.getSender());
        proposalMsg.setContent(Proposal.toJson(proposal.get()));
        agent.send(proposalMsg);
        return;
      }
    }

    currentState = State.SEARCHING;
  }

  private void announceWantedItems(ACLMessage msg) {
    Log.v(getTag(), Arrays.asList(msg.getContent().split(DELIMITER)).stream()//
        .map(Item::fromJson).filter(inv.want()::contains).map(Item::toString)
        .collect(Collectors.joining("\n", "\nI want:\n", "\nFrom: " + msg.getSender().getLocalName())));
  }

  private void evaluateProposals() {
    Optional<Proposal> bestProposal = inv.getBestProposal();
    if (bestProposal.isPresent()) {
      Log.v(getTag(), String.format("Evaluating proposals ... Accepted %s", bestProposal.get()));
      ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      msg.addReceiver(bestProposal.get().getProposingAgent());
      msg.setContent(Proposal.toJson(bestProposal.get()));
      agent.send(msg);
    } else {
      Log.v(getTag(), String.format("Evaluating proposals ... No satisfying proposals. Rejecting ..."));
    }

    Predicate<Proposal> isLoser = v -> //
        !(bestProposal.isPresent() && v.getProposingAgent().getName()
            .equals(bestProposal.get().getProposingAgent().getName()));

    inv.getProposals().stream().filter(isLoser).forEach(v -> sendRejection(v));
    inv.getProposals().clear();
  }

  private void sendRejection(Proposal v) {
    ACLMessage rejection = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
    rejection.addReceiver(v.getProposingAgent());
    rejection.setContent(Proposal.toJson(v));
    agent.send(rejection);
  }

  private void receivedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    Log.v(getTag(),
          String.format("PROPOSE > Received proposal from %s ... %s", msg.getSender().getLocalName(), proposal));
    currentState = State.NEGOTIATING;
    inv.addProposal(proposal);
  }

  private void acceptedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    inv.accepted(proposal);
    Log.v(getTag(), String.format("ACCEPT_PROPOSAL > My proposal was accepted! %s %s ", proposal, inv));
    currentState = State.SEARCHING;
  }

  private void declinedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    Log.v(getTag(), String.format("REJECT_PROPOSAL > My proposal was declined!", proposal));
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
      currentState = State.SEARCHING;
    }
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
