package org.example.model;

import java.util.List;

public class GameResponse {
    private final String message;
    private final List<Choice> choices;
    private final boolean success;
    private final String playerStatus;

    public GameResponse(String message, List<Choice> choices, boolean success, String playerStatus) {
        this.message = message;
        this.choices = choices;
        this.success = success;
        this.playerStatus = playerStatus;
    }

    public String getMessage() { return message; }
    public List<Choice> getChoices() { return choices; }
    public boolean isSuccess() { return success; }
    public String getPlayerStatus() { return playerStatus; }
}