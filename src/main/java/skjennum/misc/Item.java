package skjennum.misc;


import com.google.gson.Gson;

import skjennum.agents.ItemManagerAgent;

/**
 * Created by Patrick on 19.02.2015.
 */
public class Item implements Comparable<Item> {

  private final String name;
  private final int value;
  private final int id;
  private final int marketValue;


  public Item(String name, int marketValue) {
    this(name, marketValue, marketValue, ItemManagerAgent.generateUniqueId());
  }

  public Item(String name, int value, int marketValue, int id) {
    this.name = name;
    this.marketValue = marketValue;
    this.value = value;
    this.id = id;
  }

  public static Item create() {
    return new Item("sponge", 1 + (int) (Math.random() * 100));
  }

  public static Item create(String name) {
    return new Item(name, (int) Math.ceil(Math.random() * 100));
  }

  public static Item create(String name, int value) {
    return new Item(name, value);
  }

  public static Item fromJson(String json) {
    return new Gson().fromJson(json, Item.class);
  }

  public static String toJson(Item item) {
    return new Gson().toJson(item, Item.class);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    return id == ((Item) obj).id;
  }

  @Override
  public String toString() {
    return String.format("ID: %3d - Name: %9s - Value: %4d - Market Value: %4d", id, name, value, marketValue);
  }

  public String toStringSimple() {
    return String.format("%s($%d)", name, marketValue);
  }

  public int getMarketValue() {
    return marketValue;
  }

  @Override
  public int compareTo(Item o) {
    return value - o.value;
  }

  public int getUtilityValue() {
    return value;
  }

  public static Item wantedCopy(Item item) {
    return new Item(item.name, (int) Math.ceil(item.value * (1.05 + Math.random() * 0.1)), item.marketValue, item.id);
  }
}
