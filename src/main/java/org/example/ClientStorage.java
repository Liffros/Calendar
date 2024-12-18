package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientStorage {
    private static final String FILE_NAME = "E:\\Прога\\Java\\Calendar\\clients.json";
    private List<Client> clients = new ArrayList<>();
    private final Gson gson;

    public ClientStorage() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // Подключаем адаптер
                .setPrettyPrinting()
                .create();
        createEmptyFileIfNotExists();
        loadFromFile();
    }

    public void addClient(Client client) {
        clients.add(client);
        saveToFile();
    }

    public List<Client> getClients() {
        return clients;
    }

    public List<Client> getClientsByDate(LocalDate date) {
        return clients.stream()
                .filter(client -> client.getServiceDate().isEqual(date))
                .collect(Collectors.toList());
    }

    void saveToFile() {
        try (Writer writer = new FileWriter(FILE_NAME)) {
            gson.toJson(clients, writer);
            System.out.println("Clients saved to file: " + FILE_NAME);
        } catch (IOException e) {
            System.err.println("Failed to save clients to file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("File does not exist. Starting with an empty list.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if (file.length() == 0) { // Если файл пустой
                System.out.println("File is empty. Initializing with an empty list.");
                return;
            }
            // Чтение данных из файла
            Client[] loadedClients = gson.fromJson(reader, Client[].class);
            if (loadedClients != null) {
                clients.addAll(Arrays.asList(loadedClients));
            }
            System.out.println("Clients loaded successfully.");
        } catch (JsonSyntaxException | EOFException e) {
            System.err.println("Invalid JSON format or empty file: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Failed to load file: " + e.getMessage());
        }
    }


    private void createEmptyFileIfNotExists() {
        File file = new File(FILE_NAME);
        if (!file.exists() || file.length() == 0) {
            try (FileWriter writer = new FileWriter(FILE_NAME)) {
                writer.write("[]"); // Создаем файл с пустым JSON-массивом
                System.out.println("Created a new empty file: " + FILE_NAME);
            } catch (IOException e) {
                System.err.println("Failed to create an empty file: " + e.getMessage());
            }
        }
    }

}
