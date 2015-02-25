package b.misc;

import com.google.gson.Gson;

import b.ItemManager;

/**
 * Created by Patrick on 19.02.2015.
 */
public class Item {

  private final String name;
  private final int value;
  private final int id;


  public Item(String name, int value) {
    this.name = name;
    this.value = value;
    this.id = ItemManager.generateUniqueId();
  }

  public static Item create() {
    return new Item("sponge", (int) (Math.random() * 100));
  }

  public static Item create(String name) {
    return new Item(name, (int) (Math.random() * 100));
  }

  public static Item fromJson(String json) {
    return new Gson().fromJson(json, Item.class);
  }

  public static String toJson(Item item) {
    return new Gson().toJson(item, Item.class);
  }

  @Override
  public boolean equals(Object obj) {
    return id == ((Item) obj).id;
  }

  @Override
  public String toString() {
    return String.format("ID: %3d - Name: %8s - Value: %4d", id, name, value);
  }
}
