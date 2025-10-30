package org.model;

import java.util.List;

public class Item {
    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int value;
    private final String effect;

    public enum ItemType {
        WEAPON, ARMOR, POTION, QUEST, MISC
    }

    public Item(String id, String name, String description, ItemType type, int value, String effect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
        this.effect = effect;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public int getValue() { return value; }
    public String getEffect() { return effect; }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", name, description, type.toString().toLowerCase());
    }

    public static Item createHealthPotion() {
        return new Item("health_potion", "Зелье здоровья", "Восстанавливает 20 HP",
                ItemType.POTION, 10, "heal:20");
    }

    public static Item createSword() {
        return new Item("sword", "Стальной меч", "Обычный стальной меч",
                ItemType.WEAPON, 50, "damage:10");
    }

    public static Item createShield() {
        return new Item("shield", "Деревянный щит", "Простой деревянный щит",
                ItemType.ARMOR, 30, "defense:5");
    }
    public static List<Item> predefinedItems() {
        return List.of(
                createHealthPotion(),
                createSword(),
                createShield(),
                new Item("key", "Ржавый ключ", "Открывает старую дверь", ItemType.MISC, 0, null)
        );
    }
}