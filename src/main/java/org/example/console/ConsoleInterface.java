package org.example.console;

import org.example.logic.GameLogic;
import org.example.model.GameResponse;
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
        String playerId = "player1";

        gameLogic.addPlayer(playerId, playerName);

        GameResponse startResponse = gameLogic.startGame(playerId);
        displayResponse(startResponse);

        while (true) {
            System.out.print("\nВаш выбор: ");
            String input = scanner.nextLine();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println(String.format("Прощай, %s", playerName));
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
        System.out.println("\n" + response.getMessage());

        if (response.getChoices() != null) {
            System.out.println("\nВарианты действий:");
            for (int i = 0; i < response.getChoices().size(); i++) {
                System.out.println((i + 1) + ". " + response.getChoices().get(i).getText());
            }
        }

        System.out.println("\n(введите 'help' для справки, 'exit' для выхода)");
    }
}