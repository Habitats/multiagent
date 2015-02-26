package b.misc;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;

/**
 * Created by anon on 26.02.2015.
 */
public class Proposal {

  private final Item proposedItem;
  private final int delta;
  private final Item itemToBuy;
  private final AID proposingAgent;
  private final List<Item> declinedItems;

  public Proposal(Item proposedItem, int delta, Item itemToBuy, AID proposingAgent) {
    this(proposedItem, delta, itemToBuy, proposingAgent, new ArrayList<>());
  }

  public Proposal(Item proposedItem, int delta, Item itemToBuy, AID proposingAgent, List<Item> declinedItems) {
    this.proposedItem = proposedItem;
    this.delta = delta;
    this.itemToBuy = itemToBuy;
    this.proposingAgent = proposingAgent;
    this.declinedItems = declinedItems;
  }

  public Item getProposedItem() {
    return proposedItem;
  }

  public boolean evaluate() {
    return proposedItem.getvalue() + delta - itemToBuy.getvalue() >= 0;
  }

  public int getDelta() {
    return delta;
  }

  public Item getIemToBuy() {
    return itemToBuy;
  }

  public static Proposal fromJson(String json) {
    return new Gson().fromJson(json, Proposal.class);
  }

  public static String toJson(Proposal proposal) {
    return new Gson().toJson(proposal, Proposal.class);
  }

  public AID getProposingAgent() {
    return proposingAgent;
  }

  public List<Item> declinedItems() {
    return declinedItems;
  }


  public void declineItem(Item proposedItem) {
    declinedItems.add(proposedItem);
  }
}
