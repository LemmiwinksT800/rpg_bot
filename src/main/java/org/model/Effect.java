package org.model;

public class Effect {
    private final EffectType type;
    private final String target;
    private final int value;

    public enum EffectType {
        DAMAGE, HEAL, ADD_ITEM, REMOVE_ITEM, CHECK_STAT
    }

    public Effect(EffectType type, String target, int value) {
        this.type = type;
        this.target = target;
        this.value = value;
    }

    public EffectType getType() { return type; }
    public String getTarget() { return target; }
    public int getValue() { return value; }

    // Парсер строки вида "hp-15", "heal:20", "add:sword"
    public static Effect parse(String effectStr) {
        if (effectStr == null || effectStr.isEmpty()) return null;

        if (effectStr.startsWith("hp-")) {
            int dmg = Integer.parseInt(effectStr.substring(3));
            return new Effect(EffectType.DAMAGE, "health", dmg);
        }
        if (effectStr.startsWith("heal:")) {
            int heal = Integer.parseInt(effectStr.substring(5));
            return new Effect(EffectType.HEAL, "health", heal);
        }
        if (effectStr.startsWith("add:")) {
            return new Effect(EffectType.ADD_ITEM, effectStr.substring(4), 0);
        }
        if (effectStr.startsWith("check:")) {
            return new Effect(EffectType.CHECK_STAT, effectStr.substring(6), 0);
        }
        return null;
    }
}