package org.example.model;

public class Choice {
    private final String text;
    private final String nextScenarioId;
    private final String effect;

    public Choice(String text, String nextScenarioId, String effect) {
        this.text = text;
        this.nextScenarioId = nextScenarioId;
        this.effect = effect;
    }

    public String getText() { return text; }
    public String getNextScenarioId() { return nextScenarioId; }
    public String getEffect() { return effect; }
}