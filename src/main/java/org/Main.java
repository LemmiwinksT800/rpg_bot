package org;

import org.telegram.RPGTelegramBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        String botToken = "8429688814:AAHphlaq4BFhcCMhkhDCDmRKHPKBVLVKDjE";

        RPGTelegramBot bot = new RPGTelegramBot(botToken);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Бот запущен!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}