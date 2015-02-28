package skjennum.behaviors;

import java.util.Optional;

import skjennum.MessageListener;
import skjennum.agents.NegotiatingAgent;
import skjennum.behaviors.NegotiatingBehavior.State;
import skjennum.misc.Inventory;
import skjennum.misc.Proposal;
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
      sendProposal(proposal.get());
    }
  }

  @Override
  public void newMessage(ACLMessage msg) {
    if (msg.getConversationId().equals(conversationId)) {
      switch (msg.getPerformative()) {
        case ACLMessage.REJECT_PROPOSAL:
          prooisalRejected(msg);
          break;
        case ACLMessage.ACCEPT_PROPOSAL:
          proposalAccepted(msg);
          break;
        case ACLMessage.CANCEL:
          currentState = State.DONE;
      }
    }
  }

  private void sendProposal(Proposal proposal) {
    ACLMessage proposalMsg = new ACLMessage(ACLMessage.PROPOSE);
    proposalMsg.addReceiver(seller);
    proposalMsg.setContent(Proposal.toJson(proposal));
    proposalMsg.setConversationId(conversationId);
    agent.send(proposalMsg);
  }

  private void proposalAccepted(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    inv.accepted(proposal);
    Log.v(getTag(), String.format("ACCEPT_PROPOSAL > My proposal was accepted! %s %s ", proposal, inv));
    currentState = State.DONE;
  }

  private void prooisalRejected(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    Log.v(getTag(), String.format("REJECT_PROPOSAL > My proposal was declined!", proposal));
    inv.declined(proposal);
    Optional<Proposal> newProposal = inv.generateBetterProposal(proposal);
    if (newProposal.isPresent()) {
      sendProposal(newProposal.get());
    } else {
      currentState = State.DONE;
    }
  }

  @Override
  public int onEnd() {
    agent.removeMessageListener(this);
    return super.onEnd();
  }

  @Override
  public boolean done() {
    return currentState == State.DONE;
  }

  private String getTag() {
    return agent.getTag();
  }
}
