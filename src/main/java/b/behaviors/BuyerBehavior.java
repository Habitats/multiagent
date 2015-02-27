package b.behaviors;

import java.util.Optional;

import b.MessageListener;
import b.agents.NegotiatingAgent;
import b.behaviors.NegotiatingBehavior.State;
import b.misc.Inventory;
import b.misc.Proposal;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * Created by Patrick on 27.02.2015.
 */
public class BuyerBehavior extends Behaviour implements MessageListener {

  private final NegotiatingAgent agent;
  private final Inventory inv;
  private final Optional<Proposal> proposal;
  private final AID seller;
  private final String conversationId;
  private State currentState;

  public BuyerBehavior(NegotiatingAgent agent, Inventory inv, Optional<Proposal> proposal, ACLMessage msg) {
    currentState = State.READY;
    conversationId = msg.getConversationId();
    this.agent = agent;
    agent.addMessageListener(this);
    this.inv = inv;
    this.proposal = proposal;
    seller = msg.getSender();
  }

  @Override
  public void action() {
    if (currentState == State.READY) {
      currentState = State.NEGOTIATING;
      Log.v(getTag(), String.format("PROPOSE > Attempting to buy %s from %s! for %s and %d", //
                                    proposal.get().getIemToBuy().getName(), seller.getLocalName(),
                                    proposal.get().getProposedItem().getName(), proposal.get().getDelta()));
      ACLMessage proposalMsg = new ACLMessage(ACLMessage.PROPOSE);
      proposalMsg.addReceiver(seller);
      proposalMsg.setContent(Proposal.toJson(proposal.get()));
      proposalMsg.setConversationId(conversationId);
      agent.send(proposalMsg);
    }
  }

  private void acceptedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    inv.accepted(proposal);
    Log.v(getTag(), String.format("ACCEPT_PROPOSAL > My proposal was accepted! %s %s ", proposal, inv));
    currentState = State.DONE;
  }

  private void declinedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    Log.v(getTag(), String.format("REJECT_PROPOSAL > My proposal was declined!", proposal));
    inv.declined(proposal);
    Optional<Proposal> newProposal = inv.generateBetterProposal(proposal);
    if (newProposal.isPresent()) {
      sendNewProposal(msg, newProposal);
    } else {
      currentState = State.DONE;
    }
  }

  private void sendNewProposal(ACLMessage msg, Optional<Proposal> newProposal) {
    ACLMessage newProposalMsg = new ACLMessage(ACLMessage.PROPOSE);
    newProposalMsg.addReceiver(msg.getSender());
    newProposalMsg.setContent(Proposal.toJson(newProposal.get()));
    newProposalMsg.setConversationId(conversationId);
    agent.send(newProposalMsg);
  }

  @Override
  public boolean done() {
    return currentState == State.DONE;
  }

  private String getTag() {
    return agent.getTag();
  }

  @Override
  public void newMessage(ACLMessage msg) {
    if (msg.getConversationId().equals(conversationId)) {
      switch (msg.getPerformative()) {
        case ACLMessage.REJECT_PROPOSAL:
          declinedProposal(msg);
          break;
        case ACLMessage.ACCEPT_PROPOSAL:
          acceptedProposal(msg);
          break;
        case ACLMessage.CANCEL:
          currentState = State.DONE;
      }
    }
  }
}
