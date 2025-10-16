package org.example;

import org.example.console.ConsoleInterface;
import org.example.logic.GameLogic;
import org.example.logic.ScenarioGen;

public class Main {
    public static void main(String[] args) {
        ScenarioGen scenarioGen = new ScenarioGen();
        GameLogic gameLogic = new GameLogic(scenarioGen);
        ConsoleInterface consoleInterface = new ConsoleInterface(gameLogic);

        consoleInterface.startGame();
    }
}