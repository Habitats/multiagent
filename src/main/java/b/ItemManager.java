package b;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import b.misc.Item;

/**
 * Created by Patrick on 19.02.2015.
 */
public class ItemManager {

  private final int NUMBER_OF_AGENTS = 5;
  private final List<Item> inventory = new ArrayList<>();
  private final List<List<Item>> inventorySubLists = new ArrayList<>();
  private final List<List<Item>> wantedSubLists = new ArrayList<>();

  private int id = 0;

  private ItemManager() {
    generateItems();
  }

  public static ItemManager getInstance() {
    if (instance == null) {
      instance = new ItemManager();
    }
    return instance;
  }

  private static ItemManager instance;

  public void generateItems() {

    Collections.nCopies(100, null).forEach(i -> inventory.add(Item.create()));
    int start = 0;
    int end = inventory.size() / NUMBER_OF_AGENTS;
    for (int i = 0; i < NUMBER_OF_AGENTS; i += inventory.size() / NUMBER_OF_AGENTS) {
      inventorySubLists.add(inventory.subList(start, end));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
    Collections.shuffle(inventory);
    for (int i = 0; i < NUMBER_OF_AGENTS; i += inventory.size() / NUMBER_OF_AGENTS) {
      wantedSubLists.add(inventory.subList(start, end));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
  }

  public List<Item> aquireItems() {
    return inventorySubLists.remove(0);
  }

  public List<Item> wantedItems() {
    return wantedSubLists.remove(0);
  }

  public int getUniqueId() {
    return id++;
  }
}
