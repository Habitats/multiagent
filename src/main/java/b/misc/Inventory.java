package b.misc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import b.ItemManager;
import jade.core.AID;

/**
 * Created by anon on 26.02.2015.
 */
public class Inventory {

  private List<Item> inventory = new ArrayList<>();
  private List<Item> want = new ArrayList<>();
  private List<Proposal> proposals = new ArrayList<>();
  private int money = 200;
  private int moneyOnHold = 0;

  private Inventory() {

  }

  // used for testing
  public static Inventory createEmpty(int money) {
    Inventory inv = new Inventory();
    inv.has().clear();
    inv.want().clear();
    inv.money = money;
    return inv;
  }

  public static Inventory create() {
    Inventory inv = new Inventory();
    inv.inventory = ItemManager.getInstance().aquireItems();
    inv.want = ItemManager.getInstance().wantedItems();
    inv.want.removeIf(inv.inventory::contains);
    return inv;
  }


  public Item sendOffer(Proposal proposal) {
    money -= proposal.getDelta();
    moneyOnHold += proposal.getDelta();
    Item
        offeredItem =
        inventory.stream().filter(v -> v.getId() == proposal.getProposedItem().getId()).findFirst().get();
    inventory.remove(offeredItem);
    return offeredItem;
  }

  public Proposal generateOffer(Item itemToBuy, AID proposingAgent) {
    Item item = getClosestMatchingVendible(itemToBuy.getvalue());
    int delta = itemToBuy.getvalue() - item.getvalue();
    Proposal proposal = new Proposal(item, delta, itemToBuy, proposingAgent);
    inventory.remove(item);
    money -= delta;
    moneyOnHold += delta;
    return proposal;
  }


  public List<Item> vendible() {
    return inventory.stream().filter(i -> !want.contains(i)).collect(Collectors.toList());
  }

  public Item getClosestMatchingVendible(int maxPrice) {
    return vendible().stream().filter(i -> i.getvalue() <= maxPrice).max(Comparator.<Item>naturalOrder()).get();
  }

  private boolean evaluateOffer(Proposal proposal) {
    return want.contains(proposal.getProposedItem()) && proposal.evaluate();
  }

  public void acceptProposal(Proposal proposal) {
    money += proposal.getDelta();
    add(proposal.getIemToBuy());
  }

  public void declineProposal(Proposal proposal) {
    add(proposal.getIemToBuy());
  }

  private void add(Item item) {
    inventory.add(item);
    want.remove(item);
  }

  public List<Item> has() {
    return inventory;
  }

  public List<Item> want() {
    return want;
  }


  public int getValue(Proposal p) {
    return want.contains(p.getProposedItem()) ? p.getProposedItem().getvalue() + p.getDelta() : 0;
  }

  public Optional<Proposal> generateBetterProposal(Proposal oldProposal) {

    Optional<Item> newItem = vendible().stream()  //
        .filter(i -> i.getvalue() <= oldProposal.getIemToBuy().getvalue() && !oldProposal.declinedItems().contains(i))
        .max(Comparator.<Item>naturalOrder());
    if (newItem.isPresent()) {
      int delta = oldProposal.getIemToBuy().getvalue() - newItem.get().getvalue();
      Proposal newProposal = new Proposal(newItem.get(), delta, oldProposal.getIemToBuy(), oldProposal.getProposingAgent(), oldProposal.declinedItems());
      inventory.remove(newItem.get());
      money -= delta;
      moneyOnHold += delta;
      return Optional.of(newProposal);
    }
    return Optional.empty();
  }

  public void accepted(Proposal proposal) {
    moneyOnHold -= proposal.getDelta();
    inventory.add(proposal.getIemToBuy());
  }

  public void declined(Proposal proposal) {
    moneyOnHold -= proposal.getDelta();
    money += proposal.getDelta();
    inventory.add(proposal.getProposedItem());
    proposal.declineItem(proposal.getProposedItem());
  }

  public Optional<Proposal> getBestProposal() {
    return proposals.stream().max((p1, p2) -> getValue(p1) - getValue(p2));
  }

  public void addProposal(Proposal proposal) {
    proposals.add(proposal);
  }

  public int getMoney() {
    return money;
  }

  @Override
  public String toString() {
    return String.format("I: %s - W: %s", getConcat(inventory), getConcat(want));
  }

  private String getConcat(List<Item> inventory) {
    return inventory.stream().map(Item::getName).collect(Collectors.joining(", "));
  }
}
