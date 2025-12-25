package org.model;

import java.util.List;

public class Party {
    private final int id;
    private final String name;
    private final String leaderId;
    private List<String> playerIds;
    private String currentScenarioId;
    private String currentTurnPlayerId;
    private String status;

    public Party(int id, String name, String leaderId, List<String> playerIds, String currentScenarioId, String currentTurnPlayerId, String status) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
        this.playerIds = playerIds;
        this.currentScenarioId = currentScenarioId;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.status = status;
    }


    public int getId() { return id; }
    public String getName() { return name; }
    public String getLeaderId() { return leaderId; }
    public List<String> getPlayerIds() { return playerIds; }
    public void setPlayerIds(List<String> playerIds) { this.playerIds = playerIds; }
    public String getCurrentScenarioId() { return currentScenarioId; }
    public void setCurrentScenarioId(String currentScenarioId) { this.currentScenarioId = currentScenarioId; }
    public String getCurrentTurnPlayerId() { return currentTurnPlayerId; }
    public void setCurrentTurnPlayerId(String currentTurnPlayerId) { this.currentTurnPlayerId = currentTurnPlayerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void addPlayer(String playerId) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
    }
}