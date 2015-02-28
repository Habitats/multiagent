import org.junit.Test;

import java.util.stream.Stream;

import skjennum.misc.Inventory;
import skjennum.misc.Item;
import skjennum.misc.Proposal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by anon on 26.02.2015.
 */
public class test_Inventory {

  @Test
  public void test_Offer() {
    Item itemToSell = Item.create("yolo", 10);
    Item itemToBuy = Item.create("swag", 15);
    int delta = 5;
    Proposal proposal = new Proposal(itemToSell, delta, itemToBuy, null);
    assertTrue(proposal.evaluate());
    delta = 8;
    proposal = new Proposal(itemToSell, delta, itemToBuy, null);
    assertTrue(proposal.evaluate());
    delta = 2;
    proposal = new Proposal(itemToSell, delta, itemToBuy, null);
    assertFalse(proposal.evaluate());
  }

  @Test
  public void test_inventory() {
    Inventory inv = Inventory.createEmpty(50);
    Item itemToSell = Item.create("yolo", 20);
    Item itemToBuy = Item.create("swag", 25);
    assertEquals(inv.want().size() + inv.has().size(), Stream.concat(inv.has().stream(), inv.want().stream()).count());

    inv.has().add(itemToSell);
    inv.want().add(itemToBuy);

    Item closestMatchingVendible = inv.getClosestMatchingVendible(itemToBuy.getMarketValue()).get();
    assertEquals(itemToSell, closestMatchingVendible);
    Proposal proposal = new Proposal(closestMatchingVendible, 5, itemToBuy, null);
    assertEquals(proposal.getDelta(), 5);

    Inventory inv2 = Inventory.createEmpty(50);
    inv2.want().add(itemToSell);
    inv2.has().add(itemToBuy);

//    inv.sendProposal(proposal);
    proposal.evaluate();
    inv2.acceptProposal(proposal);
    inv.acceptProposal(proposal);
  }

  @Test
  public void test_exchangeProcess() {
    Inventory inv1 = Inventory.createEmpty(50);
    Item item1 = Item.create("yolo", 30);
    Item item2 = Item.create("swag", 25);
    Item item3 = Item.create("cupcake", 40);
//    Item item4 = Item.create("pie", 15);
    inv1.has().add(item1);
    inv1.has().add(item2);
    inv1.want().add(item3);
//    inv1.want().add(item4);

    Inventory inv2 = Inventory.createEmpty(50);
    inv2.has().add(item1);
    inv2.want().add(item2);
    inv2.has().add(item3);
//    inv2.has().add(item4);

    assertEquals(inv2.getMoney(), 50);
    assertEquals(inv1.getMoney(), 50);

    Proposal p1 = inv1.generateProposal(item3, null).get();
    assertEquals(inv1.getMoney(), 40);
    assertEquals(p1.getProposedItem(), item1);

//    inv2.addProposal(p1);
//    Proposal best = inv2.getBestProposal().get();
//    assertEquals(best, p1);
    inv2.rejectProposal(p1);

    inv1.declined(p1);
    assertEquals(inv1.getMoney(), 50);

    Proposal p2 = inv1.generateBetterProposal(p1).get();
    assertEquals(p2.getProposedItem(), item2);
    assertEquals(inv1.getMoney(), 35);

//    inv2.addProposal(p2);
//    best = inv2.getBestProposal().get();
//    assertEquals(best, p2);
    inv2.acceptProposal(p2);
    assertEquals(inv2.getMoney(), 65);

    inv1.accepted(p2);
    assertEquals(inv1.getMoney(), 35);
  }
}
