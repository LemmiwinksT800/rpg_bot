package org.model;

public class Campaign {
    private final String id;
    private final String name;
    private final String description;
    private final String startScenarioId;
    private final String faction;
    private final String startStats;

    public Campaign(String id, String name, String description, String startScenarioId, String faction, String startStats) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startScenarioId = startScenarioId;
        this.faction = faction;
        this.startStats = startStats;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStartScenarioId() { return startScenarioId; }
    public String getFaction() { return faction; }
    public String getStartStats() { return startStats; }
}