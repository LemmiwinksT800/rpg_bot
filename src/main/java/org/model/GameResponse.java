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

    // Полный конструктор (все параметры)
    public GameResponse(String message, List<Choice> choices, boolean success, String playerStatus, ResponseType type, String errorKey) {
        this.message = message;
        this.choices = choices;
        this.success = success;
        this.playerStatus = playerStatus;
        this.type = type;
        this.errorKey = errorKey;
    }

    // Перегруженный без errorKey (для нормальных случаев)
    public GameResponse(String message, List<Choice> choices, boolean success, String playerStatus, ResponseType type) {
        this(message, choices, success, playerStatus, type, null);
    }

    // Новый: Для простых ответов (type + success, всё остальное null)
    public GameResponse(ResponseType type, boolean success) {
        this(null, null, success, null, type, null);
    }

    // Новый: Для ответов с статусом (type + success + playerStatus, остальное null)
    public GameResponse(ResponseType type, boolean success, String playerStatus) {
        this(null, null, success, playerStatus, type, null);
    }

    // Новый: Для ошибок (type + success + errorKey, остальное null)
    public GameResponse(ResponseType type, String errorKey,  boolean success) {
        this(null, null, success, null, type, errorKey);
    }

    public String getMessage() { return message; }
    public List<Choice> getChoices() { return choices; }
    public boolean isSuccess() { return success; }
    public String getPlayerStatus() { return playerStatus; }
    public ResponseType getType() { return type; }
    public String getErrorKey() { return errorKey; }
}