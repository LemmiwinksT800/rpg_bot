package org;

<<<<<<< HEAD
import org.console.ConsoleInterface;
import org.logic.GameLogic;
//import org.logic.ScenarioGen;
import org.logic.DatabaseManager;
import org.logic.PlayerRepository;
import org.logic.ScenarioRepository;
=======
import org.telegram.RPGTelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
>>>>>>> develop

public class Main {
    public static void main(String[] args) {
        String botToken = "8429688814:AAHphlaq4BFhcCMhkhDCDmRKHPKBVLVKDjE";

<<<<<<< HEAD
        DatabaseManager dbManager = new DatabaseManager();
        PlayerRepository playerRepo= dbManager;
        ScenarioRepository scenarioRepo = dbManager;
        GameLogic gameLogic = new GameLogic(playerRepo, scenarioRepo);
        ConsoleInterface consoleInterface = new ConsoleInterface(gameLogic);
=======
        RPGTelegramBot bot = new RPGTelegramBot(botToken);
>>>>>>> develop

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Бот запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}