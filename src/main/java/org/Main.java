package org;

import org.console.ConsoleInterface;
import org.logic.GameLogic;
//import org.logic.ScenarioGen;
import org.logic.DatabaseManager;

public class Main {
    public static void main(String[] args) {

        DatabaseManager dbManager = new DatabaseManager();
        GameLogic gameLogic = new GameLogic(dbManager);
        ConsoleInterface consoleInterface = new ConsoleInterface(gameLogic);

        consoleInterface.startGame();
    }
}