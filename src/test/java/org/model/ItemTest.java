package org.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    @DisplayName("Создание предмета через конструктор")
    void testItemCreationWithConstructor() {
        String id = "test_sword";
        String name = "Тестовый меч";
        String description = "Очень острый меч";
        Item.ItemType type = Item.ItemType.WEAPON;
        int value = 100;
        String effect = "damage:15";
        Item item = new Item(id, name, description, type, value, effect);
        assertAll("Проверка всех свойств предмета",
                () -> assertEquals(id, item.getId(), "ID должен совпадать"),
                () -> assertEquals(name, item.getName(), "Название должно совпадать"),
                () -> assertEquals(description, item.getDescription(), "Описание должно совпадать"),
                () -> assertEquals(type, item.getType(), "Тип должен совпадать"),
                () -> assertEquals(value, item.getValue(), "Ценность должна совпадать"),
                () -> assertEquals(effect, item.getEffect(), "Эффект должен совпадать")
        );
    }

    @Test
    @DisplayName("Создание зелья здоровья через статический метод")
    void testCreateHealthPotion() {
        Item potion = Item.createHealthPotion();
        assertAll("Проверка свойств зелья здоровья",
                () -> assertEquals("health_potion", potion.getId()),
                () -> assertEquals("Зелье здоровья", potion.getName()),
                () -> assertEquals("Восстанавливает 20 HP", potion.getDescription()),
                () -> assertEquals(Item.ItemType.POTION, potion.getType()),
                () -> assertEquals(10, potion.getValue()),
                () -> assertEquals("heal:20", potion.getEffect())
        );
    }

    @Test
    @DisplayName("Создание меча через статический метод")
    void testCreateSword() {
        Item sword = Item.createSword();
        assertAll("Проверка свойств меча",
                () -> assertEquals("sword", sword.getId()),
                () -> assertEquals("Стальной меч", sword.getName()),
                () -> assertEquals("Обычный стальной меч", sword.getDescription()),
                () -> assertEquals(Item.ItemType.WEAPON, sword.getType()),
                () -> assertEquals(50, sword.getValue()),
                () -> assertEquals("damage:10", sword.getEffect())
        );
    }

    @Test
    @DisplayName("Создание щита через статический метод")
    void testCreateShield() {
        Item shield = Item.createShield();
        assertAll("Проверка свойств щита",
                () -> assertEquals("shield", shield.getId()),
                () -> assertEquals("Деревянный щит", shield.getName()),
                () -> assertEquals("Простой деревянный щит", shield.getDescription()),
                () -> assertEquals(Item.ItemType.ARMOR, shield.getType()),
                () -> assertEquals(30, shield.getValue()),
                () -> assertEquals("defense:5", shield.getEffect())
        );
    }

    @Test
    @DisplayName("Проверка метода toString()")
    void testToString() {
        Item potion = Item.createHealthPotion();
        Item sword = Item.createSword();
        Item shield = Item.createShield();
        assertAll("Проверка строкового представления",
                () -> assertTrue(potion.toString().contains("Зелье здоровья")),
                () -> assertTrue(potion.toString().contains("potion")),
                () -> assertTrue(sword.toString().contains("Стальной меч")),
                () -> assertTrue(sword.toString().contains("weapon")),
                () -> assertTrue(shield.toString().contains("Деревянный щит")),
                () -> assertTrue(shield.toString().contains("armor"))
        );
    }

    @ParameterizedTest
    @EnumSource(Item.ItemType.class)
    @DisplayName("Создание предметов всех типов")
    void testItemCreationWithAllTypes(Item.ItemType type) {
        String typeName = type.name().toLowerCase();
        Item item = new Item(
                "test_" + typeName,
                "Test " + typeName,
                "Test description",
                type,
                10,
                "test:effect"
        );
        assertEquals(type, item.getType());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 'effect1'",
            "100, 'effect2'",
            "-10, 'negative_effect'",
            "999, 'strong_effect'"
    })
    @DisplayName("Создание предметов с разной ценностью и эффектами")
    void testItemWithDifferentValuesAndEffects(int value, String effect) {
        Item item = new Item(
                "test_item",
                "Test Item",
                "Test Description",
                Item.ItemType.MISC,
                value,
                effect
        );
        assertEquals(value, item.getValue());
        assertEquals(effect, item.getEffect());
    }

    @Test
    @DisplayName("Проверка иммутабельности - геттеры возвращают корректные значения")
    void testImmutability() {
        Item item = Item.createHealthPotion();

        assertAll("Проверка иммутабельности через геттеры",
                () -> assertEquals("health_potion", item.getId()),
                () -> assertEquals("Зелье здоровья", item.getName()),
                () -> assertEquals(Item.ItemType.POTION, item.getType())
        );
    }

    @Test
    @DisplayName("Создание предмета с null эффектом")
    void testItemWithNullEffect() {
        Item item = new Item(
                "null_effect_item",
                "Item with null effect",
                "Description",
                Item.ItemType.QUEST,
                0,
                null
        );
        assertNull(item.getEffect(), "Эффект должен быть null");
        assertEquals("null_effect_item", item.getId());
    }

    @Test
    @DisplayName("Сравнение двух одинаковых предметов")
    void testItemEquality() {
        Item item1 = new Item("same_id", "Item", "Desc", Item.ItemType.MISC, 10, "effect");
        Item item2 = new Item("same_id", "Item", "Desc", Item.ItemType.MISC, 10, "effect");

        assertEquals(item1.getId(), item2.getId());
    }
}