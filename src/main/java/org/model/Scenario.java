package org.model;

import java.util.List;

public class Scenario {
    private final String id;
    private final String description;
    private final List<Choice> choices;

    public Scenario(String id, String description, List<Choice> choices) {
        this.id = id;
        this.description = description;
        this.choices = choices;
    }
    public Scenario(String id, String description) {
        this.id = id;
        this.description = description;
        this.choices = null;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public List<Choice> getChoices() { return choices; }
}