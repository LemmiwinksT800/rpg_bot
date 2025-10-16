package org.example.logic;

import org.example.model.*;
import java.util.*;


public class GameLogic {
    private final ScenarioGen scenarioGenerator;
    private String currentScenarioId;
    private final Map<String, Player> players;

    public GameLogic(ScenarioGen scenarioGenerator) {
        this.scenarioGenerator = scenarioGenerator;
        this.players = new HashMap<>();
        this.currentScenarioId = "start";
    }

    public void addPlayer(String playerId, String playerName) {
        players.put(playerId, new Player(playerName));
    }

    public GameResponse processInput(String playerId, String input) {
        if ("help".equalsIgnoreCase(input)) {
            return getHelpResponse();
        }
        if (currentScenarioId == "end"){
            return endOfGame();
        }

        Player player = players.get(playerId);
        Scenario currentScenario = scenarioGenerator.getScenario(currentScenarioId);

        try {
            int choiceIndex = Integer.parseInt(input) - 1;
            if (choiceIndex >= 0 && choiceIndex < currentScenario.getChoices().size()) {
                Choice choice = currentScenario.getChoices().get(choiceIndex);
                applyChoiceEffects(player, choice);
                currentScenarioId = choice.getNextScenarioId();

                Scenario nextScenario = scenarioGenerator.getScenario(currentScenarioId);
                return new GameResponse(nextScenario.getDescription(),
                        nextScenario.getChoices(),
                        true,
                        getPlayerStatus(player));
            } else {
                return new GameResponse("Неверный выбор. Попробуйте снова.",
                        currentScenario.getChoices(),
                        false,
                        getPlayerStatus(player));
            }
        } catch (NumberFormatException e) {
            return new GameResponse("Пожалуйста, введите номер выбора.",
                    currentScenario.getChoices(),
                    false,
                    getPlayerStatus(player));
        }
    }

    private void applyChoiceEffects(Player player, Choice choice) {
        if (choice.getEffect() != null) {
            // Простая обработка эффектов
            if (choice.getEffect().startsWith("hp-")) {
                int damage = Integer.parseInt(choice.getEffect().substring(3));
                player.setHealth(player.getHealth() - damage);
            }
            // Можно добавить другие эффекты
        }
    }

    private String getPlayerStatus(Player player) {
        return player.getStatus();
    }

    private GameResponse getHelpResponse() {
        String helpText = "Добро пожаловать в RPG бота, Герой!\n" +
                "Команды:\n" +
                "- Вводите цифры для выбора действий\n" +
                "- 'help' - показать это сообщение\n" +
                "- 'status' - показать статус персонажа";
        return new GameResponse(helpText, null, true, null);
    }
    private GameResponse endOfGame() {
        String endText = "Тут пока ничего нет :(. Введи exit \n";
        return new GameResponse(endText, null, true, null);
    }

    public GameResponse startGame(String playerId) {
        Scenario startScenario = scenarioGenerator.getScenario("start");
        Player player = players.get(playerId);
        return new GameResponse(startScenario.getDescription(),
                startScenario.getChoices(),
                true,
                getPlayerStatus(player));
    }
}