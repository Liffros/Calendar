package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        // Инициализация списка клиентов
        ClientStorage storage = new ClientStorage();

        // Создание бота
        MyTelegramBot bot = new MyTelegramBot(storage);

        // Старт бота
        TelegramBotsApi botsApi;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Bot started successfully!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        // Старт календаря с уведомлениями
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(
                () -> {
                    // Chat ID для уведомлений
                    String chatId = "647809494";
                    bot.sendReminders(chatId);
                },
                0,
                1,
                TimeUnit.DAYS
        );
    }
}
