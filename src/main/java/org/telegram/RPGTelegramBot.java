package org.telegram;

import org.logic.DatabaseManager;
import org.logic.GameLogic;
import org.logic.PlayerRepository;
import org.logic.ScenarioRepository;
import org.model.Choice;
import org.model.GameResponse;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class RPGTelegramBot extends TelegramLongPollingBot {
    private final GameLogic gameLogic;
    private final String botToken;

    public RPGTelegramBot(String botToken) {
        this.botToken = botToken;
        DatabaseManager dbManager = new DatabaseManager();
        PlayerRepository playerRepo = dbManager;
        ScenarioRepository scenarioRepo = dbManager;
        this.gameLogic = new GameLogic(playerRepo, scenarioRepo);
    }

    @Override
    public String getBotUsername() {
        return "heroin_hero_rpg_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = String.valueOf(update.getMessage().getChatId());
            String playerId = chatId;  // Используем chatId как playerId (для приватных чатов)
            String input = update.getMessage().getText().trim();


            if (input.startsWith("/")) {
                input = input.substring(1).toLowerCase();
            }

            if (input.equals("start")) {
                handleStart(chatId, playerId, update);
            } else {
                handleInput(chatId, playerId, input);
            }
        }
    }

    private void handleStart(String chatId, String playerId, Update update) {
        var player = gameLogic.loadPlayer(playerId);
        String playerName;
        if (player == null) {

            playerName = update.getMessage().getFrom().getFirstName();
            if (playerName == null || playerName.isEmpty()) {
                playerName = "Player_" + chatId;
            }
            gameLogic.addPlayer(playerId, playerName);
            player = gameLogic.loadPlayer(playerId);
            sendMessage(chatId, "Новый герой создан! Добро пожаловать, " + playerName + ".");
        } else {
            playerName = player.getName();
            sendMessage(chatId, "Прогресс загружен для " + playerName + "!");
            gameLogic.setCurrentScenarioId(player.getCurrentScenarioId());
        }

        GameResponse response = gameLogic.startGame(playerId);
        sendResponse(chatId, response);
    }

    private void handleInput(String chatId, String playerId, String input) {
        if (input.equalsIgnoreCase("exit")) {
            gameLogic.saveCurrentState(playerId);
            sendMessage(chatId, "Прогресс сохранён. До свидания!");
            return;
        }

        GameResponse response = gameLogic.processInput(playerId, input);
        sendResponse(chatId, response);
    }

    private void sendResponse(String chatId, GameResponse response) {
        StringBuilder sb = new StringBuilder("\n=== RPG Adventure ===\n");

        if (response.getPlayerStatus() != null) {
            sb.append(response.getPlayerStatus()).append("\n");
        }

        switch (response.getType()) {
            case NORMAL:
                sb.append("\n").append(response.getMessage());
                if (response.getChoices() != null) {
                    sb.append("\nВарианты действий:");
                    for (int i = 0; i < response.getChoices().size(); i++) {
                        sb.append("\n").append(i + 1).append(". ").append(response.getChoices().get(i).getText());
                    }
                }
                break;
            case HELP:
                sb.append("""
                        Команды:
                        • номер — выбрать действие
                        • help — справка
                        • status — статус героя
                        • inventory — инвентарь
                        • exit — выход
                        """);
                break;
            case STATUS:
                sb.append("Ваш статус:\n").append(response.getPlayerStatus());
                break;
            case INVENTORY:
                sb.append(response.getMessage() != null ? response.getMessage() : "Ваш инвентарь пуст.");
                break;
            case END:
                sb.append("Приключение окончено. Введите 'exit'.");
                break;
            case DEAD:
                sb.append("Вы мертвы. Игра окончена.");
                break;
            case ERROR:
                String errorMsg = switch (response.getErrorKey() != null ? response.getErrorKey() : "") {
                    case "invalid_choice" -> "Неверный выбор. Попробуйте снова.";
                    case "invalid_input" -> "Пожалуйста, введите номер выбора.";
                    default -> "Ошибка.";
                };
                sb.append(errorMsg);
                break;
            case EXIT:
                sb.append("Выход...");
                break;
        }

        sb.append("\n(введите 'help' для справки, 'exit' для выхода)");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());


        if (response.getChoices() != null && !response.getChoices().isEmpty() && response.getType() == GameResponse.ResponseType.NORMAL) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(false);
            List<KeyboardRow> keyboard = new ArrayList<>();
            for (int i = 0; i < response.getChoices().size(); i++) {
                KeyboardRow row = new KeyboardRow();
                row.add(String.valueOf(i + 1));
                keyboard.add(row);
            }

            KeyboardRow commandsRow = new KeyboardRow();
            commandsRow.add("help");
            commandsRow.add("status");
            commandsRow.add("inventory");
            keyboard.add(commandsRow);

            keyboardMarkup.setKeyboard(keyboard);
            message.setReplyMarkup(keyboardMarkup);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}