package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private int health;
    private int level;
    private final List<String> inventory;

    public Player(String name) {
        this.name = name;
        this.health = 100;
        this.level = 1;
        this.inventory = new ArrayList<>();
    }


    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getLevel() { return level; }
    public List<String> getInventory() { return new ArrayList<>(inventory); }


    public void setHealth(int health) { this.health = health; }
    public void setLevel(int level) { this.level = level; }


    public void addItem(String item) {
        inventory.add(item);
    }

    public boolean removeItem(String item) {
        return inventory.remove(item);
    }

    public String getStatus() {
        return String.format("Герой: %s | HP: %d | Уровень: %d | Предметы: %s |",
                name, health, level, inventory);
    }
}