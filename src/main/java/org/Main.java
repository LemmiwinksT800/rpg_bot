package org;

import org.console.ConsoleInterface;
import org.logic.GameLogic;
import org.logic.ScenarioGen;

public class Main {
    public static void main(String[] args) {
        ScenarioGen scenarioGen = new ScenarioGen();
        GameLogic gameLogic = new GameLogic(scenarioGen);
        ConsoleInterface consoleInterface = new ConsoleInterface(gameLogic);

        consoleInterface.startGame();
    }
}