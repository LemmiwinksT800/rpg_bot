package org.console;

import org.logic.GameLogic;
import org.model.GameResponse;
import org.model.Player;

import java.util.Scanner;

public class ConsoleInterface {
    private final GameLogic gameLogic;
    private final Scanner scanner;

    public ConsoleInterface(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.scanner = new Scanner(System.in);
    }

    public void startGame() {
        System.out.println("Введите ваше имя:");
        String playerName = scanner.nextLine();
        String playerId = playerName.toLowerCase();

        Player player = gameLogic.loadPlayer(playerId);

        if (player == null) {
            gameLogic.addPlayer(playerId, playerName);
            player = gameLogic.loadPlayer(playerId);
            System.out.println("Новый герой создан!");
        } else {
            System.out.println("Прогресс загружен для " + player.getName() + "!");
            gameLogic.setCurrentScenarioId(player.getCurrentScenarioId());
        }

        GameResponse startResponse = gameLogic.startGame(playerId);
        displayResponse(startResponse);

        while (true) {
            System.out.print("\nВаш выбор: ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                gameLogic.saveCurrentState(playerId);
                System.out.println("Прогресс сохранён. Прощай, " + playerName);
                break;
            }

            GameResponse response = gameLogic.processInput(playerId, input);
            displayResponse(response);
        }
    }

    private void displayResponse(GameResponse response) {
        System.out.println("\n=== RPG Adventure ===");

        if (response.getPlayerStatus() != null) {
            System.out.println(response.getPlayerStatus());
        }

        switch (response.getType()) {
            case NORMAL:
                System.out.println("\n" + response.getMessage());
                if (response.getChoices() != null) {
                    System.out.println("\nВарианты действий:");
                    for (int i = 0; i < response.getChoices().size(); i++) {
                        System.out.println((i + 1) + ". " + response.getChoices().get(i).getText());
                    }
                }
                break;
            case HELP:
                System.out.println("""
                    Команды:
                    • номер — выбрать действие
                    • help — справка
                    • status — статус героя
                    • inventory — инвентарь
                    • exit — выход
                    """);
                break;
            case STATUS:
                System.out.println("Ваш статус:\n" + response.getPlayerStatus());
                break;
            case INVENTORY:
                if (response.getMessage() != null) {
                    System.out.println(response.getMessage());
                } else {
                    System.out.println("Ваш инвентарь:\n" + response.getMessage());
                }
                break;
            case END:
                System.out.println("Приключение окончено. Введите 'exit'.");
                break;
            case DEAD:
                System.out.println("Вы мертвы. Игра окончена.");
                break;
            case ERROR:
                String errorMsg = switch (response.getErrorKey() != null ? response.getErrorKey() : "") {
                    case "invalid_choice" -> "Неверный выбор. Попробуйте снова.";
                    case "invalid_input" -> "Пожалуйста, введите номер выбора.";
                    default -> "Ошибка.";
                };
                System.out.println(errorMsg);
                break;
            case EXIT:
                System.out.println("Выход...");
                break;
        }

        System.out.println("\n(введите 'help' для справки, 'exit' для выхода)");
    }
}