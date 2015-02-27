package b.misc;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
  private int maxProposalThreshold = 10;

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

  public static Inventory create(List<Item> inventory, List<Item> want) {
    Inventory inv = new Inventory();
    inv.inventory = inventory;
    inv.want = want;
    inv.want.removeIf(inv.inventory::contains);
    return inv;
  }

  public Optional<Proposal> generateProposal(Item itemToBuy, AID proposingAgent) {
    Optional<Item> item = getClosestMatchingVendible(itemToBuy.getMarketValue());
    if (item.isPresent()) {
      int value = computeItemValue(0, itemToBuy, item.get());
      Proposal proposal = new Proposal(item.get(), value, itemToBuy, proposingAgent);
      inventory.remove(item);
      money -= value;
      moneyOnHold += value;
      return Optional.of(proposal);
    }
    return Optional.empty();
  }

  public List<Item> vendible() {
    return inventory.stream().filter(i -> !want.contains(i)).collect(Collectors.toList());
  }

  public Optional<Item> getClosestMatchingVendible(int maxPrice) {
    return vendible().stream().filter(i -> i.getMarketValue() <= maxPrice).max(Comparator.<Item>naturalOrder());
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

  public int getUtilityValue(Proposal p) {
    Optional<Item> wantedItem = want.stream().filter(p.getProposedItem()::equals).findFirst();
    if (wantedItem.isPresent()) {
      return wantedItem.get().getUtilityValue();
    } else {
      return p.getProposedItem().getMarketValue();
    }
  }

  public Optional<Proposal> generateBetterProposal(Proposal oldProposal) {
    Predicate<Item> isSuitableVendible = i -> //
        i.getMarketValue() <= oldProposal.getIemToBuy().getMarketValue() && !oldProposal.declinedItems().contains(i);
    Optional<Item> newItem = vendible().stream().filter(isSuitableVendible).max(Comparator.<Item>naturalOrder());
    if (newItem.isPresent()) {
      int value = computeItemValue(oldProposal.declinedItems().size(), oldProposal.getIemToBuy(), newItem.get());
      Proposal newProposal = new Proposal(newItem.get(), value, oldProposal.getIemToBuy(), //
                                          oldProposal.getProposingAgent(), oldProposal.declinedItems());
      inventory.remove(newItem.get());
      money -= value;
      moneyOnHold += value;
      return Optional.of(newProposal);
    }
    return Optional.empty();
  }

  private int computeItemValue(int proposedItemCount, Item itemToBuy, Item newItem) {
    double normalizingFactor = (1 + proposedItemCount) / (double) (maxProposalThreshold + 1);
    int delta = (itemToBuy.getMarketValue() - newItem.getMarketValue());
    int value = (int) Math.ceil(delta * normalizingFactor);
    return value;
  }

  public void accepted(Proposal proposal) {
    moneyOnHold -= proposal.getDelta();
    add(proposal.getIemToBuy());
  }

  public void declined(Proposal proposal) {
    moneyOnHold -= proposal.getDelta();
    money += proposal.getDelta();
    inventory.add(proposal.getProposedItem());
    proposal.declineItem(proposal.getProposedItem());
  }

  public Optional<Proposal> getBestProposal() {
    Predicate<Proposal> isSatisfactory = v -> getUtilityValue(v) >= v.getIemToBuy().getUtilityValue();
    return proposals.stream().filter(isSatisfactory).max((p1, p2) -> getUtilityValue(p1) - getUtilityValue(p2));
  }

  public void addProposal(Proposal proposal) {
    proposals.add(proposal);
  }

  public int getMoney() {
    return money;
  }

  public static Inventory fromJson(String json) {
    return new Gson().fromJson(json, Inventory.class);
  }

  public static String toJson(Inventory item) {
    return new Gson().toJson(item, Inventory.class);
  }

  @Override
  public String toString() {
    return String.format("> INVENTORY > %s > WANT > %s", getConcat(inventory), getConcat(want));
  }

  private String getConcat(List<Item> inventory) {
    return inventory.stream().map(Item::getName).collect(Collectors.joining(", "));
  }

  public List<Proposal> getProposals() {
    return proposals;
  }
}
