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
import org.model.Campaign;
import org.model.Invitation;
import org.model.Party;
import org.model.Player;

import java.util.ArrayList;
import java.util.List;

public class RPGTelegramBot extends TelegramLongPollingBot {
    private final GameLogic gameLogic;
    private final String botToken;
    private boolean isChoosingScenario = false;
    private final DatabaseManager dbManager;

    public RPGTelegramBot(String botToken) {
        this.botToken = botToken;
        DatabaseManager dbManager = new DatabaseManager();
        this.dbManager = dbManager;
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
            String playerId = chatId;
            String input = update.getMessage().getText().trim().toLowerCase();  // toLowerCase здесь

            if (input.startsWith("/")) {
                input = input.substring(1);
            }

            try {
                if (input.equals("start")) {
                    handleStart(chatId, playerId, update);
                } else if (input.startsWith("createparty")) {
                    handleCreateParty(chatId, playerId, input);
                } else if (input.startsWith("invite")) {
                    handleInvite(chatId, playerId, input);
                } else if (input.startsWith("accept")) {
                    handleAccept(chatId, playerId, input);
                } else if (input.startsWith("decline")) {
                    handleDecline(chatId, playerId, input);
                } else if (isChoosingScenario) {
                    handleScenarioChoice(chatId, playerId, input);
                } else {
                    handleInput(chatId, playerId, input);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendMessage(chatId, "Произошла ошибка. Попробуйте снова или свяжитесь с поддержкой.");
            }
        }
    }

    private void handleStart(String chatId, String playerId, Update update) {
        Player player = gameLogic.loadPlayer(playerId);
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

        // Проверяем приглашения
        List<Invitation> invites = dbManager.getInvitationsForPlayer(playerId);
        if (!invites.isEmpty()) {
            StringBuilder sb = new StringBuilder("У вас есть приглашения в партии:\n");
            for (Invitation inv : invites) {
                Party party = dbManager.getParty(inv.getPartyId());
                if (party != null) {
                    sb.append("Партия '").append(party.getName()).append("' (ID: ").append(inv.getPartyId()).append(")\n");
                }
            }
            sb.append("Используйте /accept <partyId> или /decline <partyId>");
            sendMessage(chatId, sb.toString());
        }

        if (player.getPartyId() != null) {
            GameResponse response = gameLogic.startPartyGame(player.getPartyId());
            sendResponseToParty(player.getPartyId(), response);
        } else {
            List<Campaign> campaigns = gameLogic.getAllCampaigns();
            if (!campaigns.isEmpty()) {
                isChoosingScenario = true;
                sendMessageWithKeyboard(chatId, "Выберите вариант сценария:", campaigns.stream().map(Campaign::getName).toList());
            } else {
                sendResponse(chatId, gameLogic.startGame(playerId));
            }
        }
    }

    private void handleScenarioChoice(String chatId, String playerId, String input) {
        List<Campaign> campaigns = gameLogic.getAllCampaigns();
        Campaign selected = campaigns.stream().filter(c -> c.getName().equalsIgnoreCase(input)).findFirst().orElse(null);
        if (selected != null) {
            gameLogic.chooseCampaign(playerId, selected.getId());
            isChoosingScenario = false;
            sendResponse(chatId, gameLogic.startGame(playerId));
        } else {
            sendMessage(chatId, "Неверный выбор. Попробуйте снова.");
        }
    }

    private void handleCreateParty(String chatId, String playerId, String input) {
        String[] parts = input.split(" ");
        if (parts.length < 3) {
            sendMessage(chatId, "Использование: /createparty <partyName> <campaignId>");
            return;
        }
        String partyName = parts[1];
        String campaignId = parts[2];
        int partyId = gameLogic.createParty(playerId, partyName, campaignId);
        if (partyId != -1) {
            sendMessage(chatId, "Партия '" + partyName + "' создана! ID: " + partyId);
            isChoosingScenario = false; // Reset if was choosing
        } else {
            sendMessage(chatId, "Ошибка создания партии. Проверьте campaignId.");
        }
    }

    private void handleInvite(String chatId, String playerId, String input) {
        String[] parts = input.split(" ");
        if (parts.length < 3) {
            sendMessage(chatId, "Использование: /invite <invitedPlayerId> <partyId>");
            return;
        }
        String invitedId = parts[1];
        int partyId;
        try {
            partyId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неверный partyId.");
            return;
        }
        Party party = dbManager.getParty(partyId);
        if (party == null || !party.getLeaderId().equals(playerId)) {
            sendMessage(chatId, "Вы не лидер этой партии или партия не существует.");
            return;
        }
        gameLogic.inviteToParty(partyId, invitedId);
        sendMessage(chatId, "Приглашение отправлено " + invitedId + " в партию " + partyId);
        sendMessage(invitedId, "Вы получили приглашение в партию '" + party.getName() + "' (ID: " + partyId + ") от " + playerId + ". Используйте /accept " + partyId + " или /decline " + partyId);
    }

    private void handleAccept(String chatId, String playerId, String input) {
        String[] parts = input.split(" ");
        if (parts.length < 2) {
            sendMessage(chatId, "Использование: /accept <partyId>");
            return;
        }
        int partyId;
        try {
            partyId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неверный partyId.");
            return;
        }
        if (gameLogic.acceptInvitation(playerId, partyId)) {
            sendMessage(chatId, "Вы присоединились к партии " + partyId + "!");
            notifyParty(partyId, playerId + " присоединился к партии!");
            GameResponse response = gameLogic.startPartyGame(partyId);
            sendResponseToParty(partyId, response);
            isChoosingScenario = false; // Reset if was choosing
        } else {
            sendMessage(chatId, "Ошибка присоединения к партии. Проверьте, существует ли приглашение.");
        }
    }

    private void handleDecline(String chatId, String playerId, String input) {
        String[] parts = input.split(" ");
        if (parts.length < 2) {
            sendMessage(chatId, "Использование: /decline <partyId>");
            return;
        }
        int partyId;
        try {
            partyId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Неверный partyId.");
            return;
        }
        gameLogic.declineInvitation(playerId, partyId);
        sendMessage(chatId, "Приглашение в партию " + partyId + " отклонено.");
    }

    private void handleInput(String chatId, String playerId, String input) {
        if (input.equalsIgnoreCase("exit")) {
            gameLogic.saveCurrentState(playerId);
            sendMessage(chatId, "Прогресс сохранён. До свидания!");
            return;
        }

        GameResponse response = gameLogic.processInput(playerId, input);
        Player player = gameLogic.loadPlayer(playerId);
        if (player.getPartyId() != null) {

            if (response.getType() == GameResponse.ResponseType.NORMAL ||
                    response.getType() == GameResponse.ResponseType.END ||
                    response.getType() == GameResponse.ResponseType.DEAD ||
                    response.getType() == GameResponse.ResponseType.ERROR) {
                sendResponseToParty(player.getPartyId(), response);
            } else {

                sendResponse(chatId, response);
            }
        } else {
            sendResponse(chatId, response);
        }
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
                        • /createparty <name> <campaignId> — создать партию
                        • /invite <playerId> <partyId> — пригласить
                        • /accept <partyId> — принять
                        • /decline <partyId> — отклонить
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
                    case "not_your_turn" -> "Не ваш ход! Ждите своей очереди.";
                    case "party_not_active" -> "Партия не активна.";
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

    private void sendResponseToParty(int partyId, GameResponse response) {
        Party party = dbManager.getParty(partyId);
        if (party == null) return;

        Player currentPlayer = gameLogic.loadPlayer(party.getCurrentTurnPlayerId());
        String currentTurnName = (currentPlayer != null) ? currentPlayer.getName() : "Неизвестно";


        String partyStatus = gameLogic.getPartyStatus(party);

        StringBuilder sb = new StringBuilder("\n=== RPG Adventure ===\n");


        switch (response.getType()) {
            case NORMAL:
                sb.append("\n").append(response.getMessage());
                if (response.getChoices() != null) {
                    sb.append("\nВарианты действий:");
                    for (int i = 0; i < response.getChoices().size(); i++) {
                        sb.append("\n").append(i + 1).append(". ").append(response.getChoices().get(i).getText());
                    }
                }
                sb.append("\n\nТекущий ход: ").append(currentTurnName);
                break;
            case END:
                sb.append("Приключение окончено для партии.");
                break;
            case DEAD:
                sb.append("Все мертвы. Игра окончена для партии.");
                break;
            case ERROR:
                String errorMsg = switch (response.getErrorKey() != null ? response.getErrorKey() : "") {
                    case "invalid_choice" -> "Неверный выбор. Попробуйте снова.";
                    case "invalid_input" -> "Пожалуйста, введите номер выбора.";
                    case "not_your_turn" -> "Не ваш ход! Ждите своей очереди.";
                    case "party_not_active" -> "Партия не активна.";
                    default -> "Ошибка.";
                };
                sb.append(errorMsg);
                break;
            default:

                return;
        }

        sb.append("\n(введите 'help' для справки, 'exit' для выхода)");


        for (String pid : party.getPlayerIds()) {
            SendMessage message = new SendMessage();
            message.setChatId(pid);
            message.setText(sb.toString());


            if (pid.equals(party.getCurrentTurnPlayerId()) && response.getType() == GameResponse.ResponseType.NORMAL && response.getChoices() != null && !response.getChoices().isEmpty()) {
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

    private void sendMessageWithKeyboard(String chatId, String text, List<String> buttons) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (String button : buttons) {
            row.add(button);
            if (row.size() == 3) {
                keyboard.add(row);
                row = new KeyboardRow();
            }
        }
        if (!row.isEmpty()) keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void notifyParty(int partyId, String text) {
        Party party = dbManager.getParty(partyId);
        if (party != null) {
            for (String pid : party.getPlayerIds()) {
                sendMessage(pid, text);
            }
        }
    }
}