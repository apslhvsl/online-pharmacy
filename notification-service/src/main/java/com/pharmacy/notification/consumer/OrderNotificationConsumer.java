package com.pharmacy.notification.consumer;

import com.pharmacy.notification.config.RabbitMQConfig;
import com.pharmacy.notification.dto.OrderNotificationEvent;
import com.pharmacy.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderUpdate(OrderNotificationEvent event) {
        log.info("Received order notification event for order: {}", event.getOrderNumber());
        emailService.sendOrderUpdateEmail(event);
    }
}
