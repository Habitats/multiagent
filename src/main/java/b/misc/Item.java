package b.misc;

/**
 * Created by Patrick on 19.02.2015.
 */
public class Item {

  private final String name;
  private final int value;
  private final int id = 0;

  public Item(String name, int value) {
    this.name = name;
    this.value = value;
//    this.id = ItemManager.getInstance().getUniqueId();
  }

  public static Item create() {
    return new Item("spnoge", 10);
  }

  @Override
  public String toString() {
    return String.format("ID: %3d - Name: %s - Value: %4d", id, name, value);
  }
}
