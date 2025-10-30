package org.model;

public class Choice {
    private final String text;
    private final String nextScenarioId;
    private final Effect effect;

    public Choice(String text, String nextScenarioId, String effect) {
        this.text = text;
        this.nextScenarioId = nextScenarioId;
        this.effect = Effect.parse(effect);
    }

    public String getText() { return text; }
    public String getNextScenarioId() { return nextScenarioId; }
    public Effect getEffect() { return effect; }
}