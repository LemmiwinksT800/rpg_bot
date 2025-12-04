package org.logic;

import org.model.*;

import java.util.*;

public class GameLogic {
    private String currentScenarioId;
    private final Map<String, Player> players;
    private final DatabaseManager dbManager;

    public GameLogic(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.players = new HashMap<>();
        this.currentScenarioId = "start";
    }

    public void addPlayer(String playerId, String playerName) {
        Player player = new Player(playerName);
        players.put(playerId, player);
        dbManager.savePlayer(playerId, player, "start");
    }

    public GameResponse startGame(String playerId) {
        Player player = dbManager.loadPlayer(playerId);
        Scenario start = dbManager.getScenario("start");
        return buildResponse(start, player, "Игра началась!");
    }

    public GameResponse processInput(String playerId, String input) {
        Player player = players.get(playerId);
        if (player == null) return new GameResponse("Игрок не найден.", null, false, null);

        if (!player.isAlive()) {
            return new GameResponse("Вы мертвы. Игра окончена.", null, false, null);
        }

        // Обработка команд
        if ("help".equalsIgnoreCase(input)) return getHelpResponse();
        if ("status".equalsIgnoreCase(input)) return getStatusResponse(player);
        if ("inventory".equalsIgnoreCase(input)) return getInventoryResponse(player);
        if ("exit".equalsIgnoreCase(input)) return new GameResponse("Выход...", null, true, null);

        Scenario current = dbManager.getScenario(currentScenarioId);
        if (current == null || current.getChoices() == null) {
            return endOfGame();
        }
        dbManager.savePlayer(playerId, player, currentScenarioId);
        try {
            int choiceIdx = Integer.parseInt(input) - 1;
            if (choiceIdx < 0 || choiceIdx >= current.getChoices().size()) {
                return buildResponse(current, player, "Неверный выбор.");
            }

            Choice choice = current.getChoices().get(choiceIdx);
            applyEffect(player, choice.getEffect());

            if (!player.isAlive()) {
                return new GameResponse("Вы умерли от полученных ран...", null, false, null);
            }

            currentScenarioId = choice.getNextScenarioId();
            Scenario next = dbManager.getScenario(currentScenarioId);

            String message = next != null ? next.getDescription() : "Конец пути...";
            return buildResponse(next, player, message);

        } catch (NumberFormatException e) {
            return buildResponse(current, player, "Введите номер варианта.");
        }
    }

    private void applyEffect(Player player, Effect effect) {
        if (effect == null) return;

        switch (effect.getType()) {
            case DAMAGE -> player.damage(effect.getValue());
            case HEAL -> player.heal(effect.getValue());
            case ADD_ITEM -> Item.predefinedItems().stream()
                    .filter(i -> i.getId().equals(effect.getTarget()))
                    .findFirst()
                    .ifPresent(player::addItem);
            case CHECK_STAT -> {
                int required = effect.getValue();
                int playerStat = player.getStats().getOrDefault(effect.getTarget(), 0);
                if (playerStat < required) {
                    player.damage(20); // провал проверки
                }
            }
        }
    }

    private GameResponse buildResponse(Scenario scenario, Player player, String overrideMessage) {
        String message = overrideMessage != null ? overrideMessage : (scenario != null ? scenario.getDescription() : "");
        List<Choice> choices = scenario != null ? scenario.getChoices() : null;
        return new GameResponse(message, choices, true, player.getStatus());
    }

    private GameResponse getHelpResponse() {
        String help = """
                Команды:
                • номер — выбрать действие
                • help — справка
                • status — статус героя
                • inventory — инвентарь
                • exit — выход
                """;
        return new GameResponse(help, null, true, null);
    }

    private GameResponse getStatusResponse(Player player) {
        return new GameResponse("Ваш статус:\n" + player.getStatus(), null, true, null);
    }

    private GameResponse getInventoryResponse(Player player) {
        if (player.getInventory().isEmpty()) {
            return new GameResponse("Инвентарь пуст.", null, true, null);
        }
        String items = player.getInventory().stream()
                .map(i -> "- " + i.toString())
                .collect(java.util.stream.Collectors.joining("\n"));
        return new GameResponse("Ваш инвентарь:\n" + items, null, true, null);
    }

    private GameResponse endOfGame() {
        return new GameResponse("Приключение окончено. Введите 'exit'.", null, true, null);
    }
}