package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Client {
    private String name;
    private String phoneNumber;
    private String service;
    private LocalDate serviceDate;

    public Client(String name, String phoneNumber, String service, String serviceDate) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.service = service;
        this.serviceDate = parseDate(serviceDate);
    }

    // Парсинг даты с валидацией
    private LocalDate parseDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        }
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getService() {
        return service;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    @Override
    public String toString() {
        return name + " " + phoneNumber + " " + service + " " + serviceDate;
    }
}
