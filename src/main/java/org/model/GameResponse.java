package org.model;

import java.util.List;

public class GameResponse {
    private final String message;
    private final List<Choice> choices;
    private final boolean success;
    private final String playerStatus;
    private final ResponseType type;
    private final String errorKey;

    public enum ResponseType {
        NORMAL, HELP, STATUS, INVENTORY, END, DEAD, ERROR, EXIT
    }
    public GameResponse(String message, List<Choice> choices, boolean success, String playerStatus, ResponseType type) {
        this(message, choices, success, playerStatus, type, null);
    }

    public GameResponse(String message, List<Choice> choices, boolean success,
                        String playerStatus, ResponseType type, String errorKey) {
        this.message = message;
        this.choices = choices;
        this.success = success;
        this.playerStatus = playerStatus;
        this.type = type;
        this.errorKey = errorKey;
    }

    public String getMessage() { return message; }
    public List<Choice> getChoices() { return choices; }
    public boolean isSuccess() { return success; }
    public String getPlayerStatus() { return playerStatus; }
    public ResponseType getType() { return type; }
    public String getErrorKey() { return errorKey; }
}