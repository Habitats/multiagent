package skjennum.behaviors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import skjennum.MessageListener;
import skjennum.agents.NegotiatingAgent;
import skjennum.behaviors.NegotiatingBehavior.State;
import skjennum.misc.Inventory;
import skjennum.misc.Item;
import skjennum.misc.Proposal;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import util.Log;

/**
 * Created by Patrick on 27.02.2015.
 */
public class SellerBehavior extends Behaviour implements MessageListener {

  private final NegotiatingAgent agent;
  private final Inventory inv;
  private final String conversationId;
  private final int proposalTimeout = 3000;
  private final List<Proposal> proposals = new ArrayList<>();
  private State currentState;

  public SellerBehavior(NegotiatingAgent agent, Inventory inv, ACLMessage cfp) {
    currentState = State.READY;
    this.agent = agent;
    conversationId = cfp.getConversationId();
    agent.addMessageListener(this);
    this.inv = inv;
  }

  @Override
  public void action() {
    if (currentState == State.READY) {
      currentState = State.WAITING_FOR_PROPOSAL;
      agent.addBehaviour(new TickerBehaviour(agent, 100) {
        @Override
        protected void onTick() {
          if (currentState == State.WAITING_FOR_PROPOSAL) {
            currentState = State.NEGOTIATING;
            agent.addBehaviour(new WakerBehaviour(agent, proposalTimeout) {
              @Override
              protected void onWake() {
                evaluateProposals();
              }
            });
          }
        }
      });
    }
  }

  @Override
  public void newMessage(ACLMessage msg) {
    if (msg.getConversationId().equals(conversationId)) {
      switch (msg.getPerformative()) {
        case ACLMessage.PROPOSE:
          receivedProposal(msg);
          break;
      }
    }
  }

  private void receivedProposal(ACLMessage msg) {
    Proposal proposal = Proposal.fromJson(msg.getContent());
    Log.v(getTag(), String.format("PROPOSE > Received %sproposal from %s ... %s",//
                                  proposal.declinedItems().size() > 0 ? "new " : "", msg.getSender().getLocalName(),
                                  proposal));
    proposals.add(proposal);
  }

  private Optional<Proposal> getBestProposal() {
    Predicate<Proposal> isSatisfactory = v -> getUtilityOfProposal(v) >= 0;
    Comparator<Proposal> comparator = (p1, p2) -> getUtilityOfProposal(p1) - getUtilityOfProposal(p2);
    return proposals.stream().filter(isSatisfactory).max(comparator);
  }

  private int getUtilityOfProposal(Proposal proposal) {
    return getUtilityValueOfSelling(proposal) - getUtilityValueOfKeeping(proposal);
  }

  private int getUtilityValueOfSelling(Proposal p) {
    Optional<Item> proposedItem = inv.want().stream().filter(p.getProposedItem()::equals).findFirst();
    if (proposedItem.isPresent()) {
      return proposedItem.get().getUtilityValue() + p.getDelta();
    } else {
      return p.getProposedItem().getMarketValue() + p.getDelta();
    }
  }

  private int getUtilityValueOfKeeping(Proposal p) {
    Optional<Item> sellingItem = inv.want().stream().filter(p.getIemToBuy()::equals).findFirst();
    if (sellingItem.isPresent()) {
      return sellingItem.get().getUtilityValue();
    } else {
      return p.getIemToBuy().getMarketValue();
    }
  }

  private void evaluateProposals() {
    Optional<Proposal> bestProposal = getBestProposal();
    Predicate<Proposal> isLoser = v -> //
        !(bestProposal.isPresent() && v.getProposingAgent().getName()
            .equals(bestProposal.get().getProposingAgent().getName()));
    if (bestProposal.isPresent()) {
      Log.v(getTag(), String.format("Evaluating proposals ... Accepted %s", bestProposal.get()));
      ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      msg.addReceiver(bestProposal.get().getProposingAgent());
      msg.setContent(Proposal.toJson(bestProposal.get()));
      msg.setConversationId(conversationId);
      agent.send(msg);
      proposals.stream().filter(isLoser).forEach(this::sendCancel);
      currentState = State.DONE;
    } else if (proposals.size() > 0) {
      Log.v(getTag(),
            String.format("Evaluating proposals ... No satisfying proposals. Rejecting and waiting for new ones ..."));
      proposals.stream().filter(isLoser).forEach(this::sendRejection);
      currentState = State.WAITING_FOR_PROPOSAL;
    } else {
      Log.v(getTag(), String.format("No proposals ... Going back to search!"));
      currentState = State.DONE;
    }

    proposals.clear();
  }

  private void sendCancel(Proposal v) {
    ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
    cancel.addReceiver(v.getProposingAgent());
    cancel.setContent(Proposal.toJson(v));
    cancel.setConversationId(conversationId);
    agent.send(cancel);
  }

  private void sendRejection(Proposal v) {
    ACLMessage rejection = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
    rejection.addReceiver(v.getProposingAgent());
    rejection.setContent(Proposal.toJson(v));
    rejection.setConversationId(conversationId);
    agent.send(rejection);
  }

  private String getTag() {
    return agent.getTag();
  }

  @Override
  public boolean done() {
    return currentState == State.DONE;
  }

  @Override
  public int onEnd() {
    agent.removeMessageListener(this);
    return super.onEnd();
  }

}
