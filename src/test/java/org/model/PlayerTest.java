package org.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("TestHero");
    }

    @Test
    void testInitialState() {
        assertEquals("TestHero", player.getName());
        assertEquals(100, player.getHealth());
        assertEquals(100, player.getMaxHealth());
        assertEquals(1, player.getLevel());
        assertTrue(player.getInventory().isEmpty());
        assertEquals(10, player.getStats().get("stealth"));
        assertTrue(player.isAlive());
    }

    @Test
    void testDamageAndHeal() {
        player.damage(20);
        assertEquals(80, player.getHealth());

        player.heal(10);
        assertEquals(90, player.getHealth());

        player.damage(100);
        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());

        player.heal(50);  // Не выше max, но от 0
        assertEquals(50, player.getHealth());
        assertTrue(player.isAlive());
    }

    @Test
    void testInventory() {
        Item sword = Item.createSword();
        player.addItem(sword);
        assertTrue(player.hasItem("sword"));
        assertEquals(1, player.getInventory().size());

        player.removeItem(sword);
        assertFalse(player.hasItem("sword"));
    }

    @Test
    void testStats() {
        Map<String, Integer> newStats = Map.of("strength", 15);
        player.setStats(newStats);
        assertEquals(15, player.getStats().get("strength"));
        assertNull(player.getStats().get("stealth"));  // Старые статы очищены
    }

    @Test
    void testStatusString() {
        String status = player.getStatus();
        assertTrue(status.contains("Герой: TestHero"));
        assertTrue(status.contains("HP: 100/100"));
        assertTrue(status.contains("Предметы: пусто"));
    }
}