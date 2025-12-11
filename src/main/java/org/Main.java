package org;

import org.console.ConsoleInterface;
import org.logic.GameLogic;
//import org.logic.ScenarioGen;
import org.logic.DatabaseManager;
import org.logic.PlayerRepository;
import org.logic.ScenarioRepository;

public class Main {
    public static void main(String[] args) {

        DatabaseManager dbManager = new DatabaseManager();
        PlayerRepository playerRepo= dbManager;
        ScenarioRepository scenarioRepo = dbManager;
        GameLogic gameLogic = new GameLogic(playerRepo, scenarioRepo);
        ConsoleInterface consoleInterface = new ConsoleInterface(gameLogic);

        consoleInterface.startGame();
    }
}