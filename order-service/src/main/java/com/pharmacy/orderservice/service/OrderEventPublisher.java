package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.client.AuthClient;
import com.pharmacy.orderservice.config.RabbitMQConfig;
import com.pharmacy.orderservice.dto.OrderNotificationEvent;
import com.pharmacy.orderservice.dto.UserInfo;
import com.pharmacy.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AuthClient authClient;

    @Async
    public void publishOrderUpdate(Order order) {
        try {
            UserInfo user = authClient.getUserById(order.getUserId());
            OrderNotificationEvent event = OrderNotificationEvent.builder()
                    .userId(order.getUserId())
                    .userEmail(user.getEmail())
                    .userName(user.getName())
                    .orderNumber(order.getOrderNumber())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .updatedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ORDER_ROUTING_KEY, event);
            log.info("Published order update event | order={} status={}", order.getOrderNumber(), order.getStatus());
        } catch (Exception e) {
            // Non-critical — don't fail the order operation if notification fails
            log.warn("Failed to publish order update event | order={} reason={}", order.getOrderNumber(), e.getMessage());
        }
    }
}
