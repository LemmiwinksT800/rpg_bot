package org.model;

import java.util.*;

public class Player {
    private final String name;
    private int health;
    private int maxHealth;
    private int level;
    private final Map<String, Integer> stats;
    private final List<Item> inventory;

    public Player(String name) {
        this.name = name;
        this.health = 100;
        this.maxHealth = 100;
        this.level = 1;
        this.stats = new HashMap<>();
        this.stats.put("stealth", 10);
        this.stats.put("strength", 10);
        this.inventory = new ArrayList<>();
    }


    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public List<Item> getInventory() { return Collections.unmodifiableList(inventory); }
    public Map<String, Integer> getStats(){return Collections.unmodifiableMap(stats);}


    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    public void heal(int amount) {
        setHealth(health + amount);
    }

    public void damage(int amount) {
        setHealth(health - amount);
    }

    public void addItem(Item item) {
        inventory.add(item);
    }

    public boolean removeItem(Item item) {
        return inventory.remove(item);
    }

    public boolean hasItem(String itemId) {
        return inventory.stream().anyMatch(i -> i.getId().equals(itemId));
    }

    public Optional<Item> findItem(String itemId) {
        return inventory.stream().filter(i -> i.getId().equals(itemId)).findFirst();
    }

    public String getStatus() {
        String items = inventory.isEmpty()
                ? "пусто"
                : inventory.stream().map(Item::getName).collect(java.util.stream.Collectors.joining(", "));
        return String.format("Герой: %s | HP: %d/%d | Уровень: %d | Предметы: %s",
                name, health, maxHealth, level, items);
    }

    public boolean isAlive() {
        return health > 0;
    }
}