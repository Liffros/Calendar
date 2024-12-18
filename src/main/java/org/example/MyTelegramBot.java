package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final ClientStorage clientStorage;

    public MyTelegramBot(ClientStorage clientStorage) {
        this.clientStorage = clientStorage;
    }

    @Override
    public String getBotUsername() {
        return "motivcalendar_bot";
    }

    @Override
    public String getBotToken() {
        return "7599732664:AAHDzKKt5OT2ZUbjdbK1FX7wMFzwuNg1oZ8";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            if (text.startsWith("Удалить клиента") || text.startsWith("/delete")) {
                sendMessage(chatId, "Чтобы удалить клиента, восьпользуйтесь коммандой \n" +
                        "/delete `номер клиента`");
                if (text.startsWith("/delete ")) {
                    String phoneNumber = text.replace("/delete ", "").trim();
                    handleDeleteByPhoneCommand(chatId, phoneNumber);
                }
            } else {
                switch (text) {
                    case "/start":
                        sendCommandMenu(chatId);
                        handleStartCommand(chatId);
                        break;
                    case "Список команд":
                    case "/help":
                        handleHelpCommand(chatId);
                        break;
                    case "Клиенты":
                    case "/clients":
                        handleListCommand(chatId);
                        break;
                    case "Удалить всех клиентов":
                    case "/all_delete":
                        handleDeleteAllCommand(chatId);
                        break;
                    default:
                        if (text.startsWith("Добавить клиента") || text.startsWith("/add")) {
                            handleAddCommand(chatId, text);
                        } else {
                            handleUnknownCommand(chatId);
                        }
                        break;
                }
            }
        }
    }

    // Добавление клиента
    private void handleAddCommand(String chatId, String text) {
        String[] parts = text.split(" ", 5);
        if (parts.length < 5) {
            sendMessage(chatId, "Используйте: `/add Имя Телефон Услуга YYYY-MM-DD`");
            return;
        }

        try {
            String name = parts[1];
            String phone = parts[2];
            String service = parts[3];
            String date = parts[4];

            Client client = new Client(name, phone, service, date);
            clientStorage.addClient(client);

            sendMessage(chatId, "✅ Клиент успешно добавлен:\n" + client);
            clientStorage.saveToFile();
        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "Ошибка: " + e.getMessage());
        }
    }

    // Список клиентов
    private void handleListCommand(String chatId) {
        List<Client> clients = clientStorage.getClients();
        if (clients.isEmpty()) {
            sendMessage(chatId, "📭 Клиенты не найдены.");
        } else {
            StringBuilder response = new StringBuilder("📋 Список клиентов:\n");
            for (Client client : clients) {
                response.append(client).append("\n");
            }
            sendMessage(chatId, response.toString());
        }
    }

    // Список команд
    private void handleHelpCommand(String chatId) {
        String helpText = "Доступные команды:\n" +
                "/add - Добавить нового клиента\n" +
                "/list - Показать всех клиентов\n" +
                "/delete - Удалить клиента по номеру\n" +
                "/allDelete - удалить всех клиентов\n" +
                "/help - Показать подсказки по командам\n" +
                "/start - Показать меню команд";
        sendMessage(chatId, helpText);
    }

    // /start команда
    private void handleStartCommand(String chatId) {
        sendMessage(chatId, "👋 Добро пожаловать! Выберите команду из кнопок ниже:");
    }

    // Обработка неизвестных команд
    private void handleUnknownCommand(String chatId) {
        sendMessage(chatId, "❓ Неизвестная команда.\nИспользуйте /help для просмотра доступных команд.");
    }
    // Удалить клиента по номеру телефона
    private void handleDeleteByPhoneCommand(String chatId, String phoneNumber) {
        List<Client> clients = clientStorage.getClients();
        if (clients.isEmpty()) {
            sendMessage(chatId, "Список клиентов пуст. Удалять нечего.");
            return;
        }

        // Поиск клиента по номеру телефона
        Client clientToRemove = null;
        for (Client client : clients) {
            if (client.getPhoneNumber().equals(phoneNumber)) {
                clientToRemove = client;
                break;
            }
        }

        // Если клиент найден, удаляем
        if (clientToRemove != null) {
            clients.remove(clientToRemove);
            sendMessage(chatId, "Клиент \"" + clientToRemove.getName() + "\" с номером \"" + clientToRemove.getPhoneNumber() + "\" успешно удален.");
            clientStorage.saveToFile();
        } else {
            sendMessage(chatId, "Клиент с номером \"" + phoneNumber + "\" не найден.");
        }
    }
    // Удалить всех клиентов
    private void handleDeleteAllCommand(String chatId) {
        List<Client> clients = clientStorage.getClients();
        if (clients.isEmpty()) {
            sendMessage(chatId, "Список клиентов уже пуст.");
            return;
        }

        // Очищаем весь список
        clients.clear();
        clientStorage.saveToFile();
        sendMessage(chatId, "Все клиенты успешно удалены.");
    }

    public void sendReminders(String chatId) {
        List<Client> todayClients = clientStorage.getClientsByDate(LocalDate.now());
        if (todayClients.isEmpty()) {
            sendMessage(chatId, "📅 На сегодня клиентов нет.");
        } else {
            StringBuilder reminderMessage = new StringBuilder("📆 Сегодняшние клиенты:\n");
            for (Client client : todayClients) {
                reminderMessage.append("🔹 ").append(client).append("\n");
            }
            sendMessage(chatId, reminderMessage.toString());
        }
    }

    // Отправка сообщений
    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableMarkdown(true); // Разрешаем использование форматирования Markdown
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    //Меню команд
    public void sendCommandMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите команду:");

        // Создаем разметку для клавиатуры
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        // Создаем строки клавиатуры
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Первая строка с кнопками
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Добавить клиента");
        row1.add("Клиенты");

        // Вторая строка с кнопками
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Список команд");
        row2.add("Удалить клиента");

        // Добавляем строки в клавиатуру
        keyboardRows.add(row1);
        keyboardRows.add(row2);

        // Устанавливаем клавиатуру
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
