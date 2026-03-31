package com.pharmacy.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

// Entry point for the Order Service (cart, checkout, orders, payments)
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync  // needed for async event publishing
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}