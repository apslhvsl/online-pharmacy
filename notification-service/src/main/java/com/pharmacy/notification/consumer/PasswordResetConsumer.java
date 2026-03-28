package com.pharmacy.notification.consumer;

import com.pharmacy.notification.config.RabbitMQConfig;
import com.pharmacy.notification.dto.PasswordResetEvent;
import com.pharmacy.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.PASSWORD_QUEUE)
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("Received password reset event for user: {}", event.getUserEmail());
        emailService.sendPasswordResetEmail(event);
    }
}
