package com.pharmacy.notification;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point for the Notification Service (email via RabbitMQ consumers)
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        // load .env file if present — useful for local dev without setting system env vars
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
