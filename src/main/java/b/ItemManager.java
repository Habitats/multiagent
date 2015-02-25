package b;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import b.misc.Item;

/**
 * Created by Patrick on 19.02.2015.
 */
public class ItemManager {

  private final int NUMBER_OF_AGENTS = 3;
  private final int inventorySize = NUMBER_OF_AGENTS * 10;
  private final List<Item> inventory = new ArrayList<>();
  private final List<List<Item>> inventorySubLists = new ArrayList<>();
  private final List<List<Item>> wantedSubLists = new ArrayList<>();

  private String
      nouns =
      "apple\n" + "arm\n" + "banana\n" + "bike\n" + "bird\n" + "book\n" + "chin\n" + "clam\n" + "class\n" + "clover\n"
      + "club\n" + "corn\n" + "crayon\n" + "crow\n" + "crown\n" + "crowd\n" + "crib\n" + "desk\n" + "dime\n" + "dirt\n"
      + "dress\n" + "fang \n" + "field\n" + "flag\n" + "flower\n" + "fog\n" + "game\n" + "heat\n" + "hill\n" + "home\n"
      + "horn\n" + "hose\n" + "joke\n" + "juice\n" + "kite\n" + "lake\n" + "maid\n" + "mask\n" + "mice\n" + "milk\n"
      + "mint\n" + "meal\n" + "meat\n" + "moon\n" + "mother\n" + "morning\n" + "name\n" + "nest\n" + "nose\n" + "pear\n"
      + "pen\n" + "pencil\n" + "plant\n" + "rain\n" + "river\n" + "road\n" + "rock\n" + "room\n" + "rose\n" + "seed\n"
      + "shape\n" + "shoe\n" + "shop\n" + "show\n" + "sink\n" + "snail\n" + "snake\n" + "snow\n" + "soda\n" + "sofa\n"
      + "star\n" + "step\n" + "stew\n" + "stove\n" + "straw\n" + "string\n" + "summer\n" + "swing\n" + "table\n"
      + "tank\n" + "team\n" + "tent\n" + "test\n" + "toes\n" + "tree\n" + "vest\n" + "water\n" + "wing\n" + "winter\n"
      + "woman" + "ball\n" + "bat\n" + "bed\n" + "book\n" + "boy\n" + "bun\n" + "can\n" + "cake\n" + "cap\n" + "car\n"
      + "cat\n" + "cow\n" + "cub\n" + "cup\n" + "dad\n" + "day\n" + "dog\n" + "doll\n" + "dust\n" + "fan\n" + "feet\n"
      + "girl\n" + "gun\n" + "hall\n" + "hat\n" + "hen\n" + "jar\n" + "kite\n" + "man\n" + "map\n" + "men\n" + "mom\n"
      + "pan\n" + "pet\n" + "pie\n" + "pig\n" + "pot\n" + "rat\n" + "son\n" + "sun\n" + "toe\n" + "tub\n" + "van";

  private static int id = 0;
  private List<String> nounsList;

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
    nounsList = Arrays.asList(nouns.split("\\s")).stream().collect(Collectors.toList());
    Collections.shuffle(nounsList);

    Collections.nCopies(inventorySize, null).forEach(i -> inventory.add(Item.create(nounsList.remove(0))));
    int start = 0;
    int end = inventory.size() / NUMBER_OF_AGENTS;
    for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
      inventorySubLists.add(inventory.subList(start, end).stream().collect(Collectors.toList()));
      start += inventory.size() / NUMBER_OF_AGENTS;
      end += inventory.size() / NUMBER_OF_AGENTS;
    }
    start = 0;
    end = inventory.size() / NUMBER_OF_AGENTS;
    Collections.shuffle(inventory);
    for (int i = 0; i < NUMBER_OF_AGENTS; i++) {
      wantedSubLists.add(inventory.subList(start, end).stream().collect(Collectors.toList()));
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

  public static int generateUniqueId() {
    return id++;
  }
}
