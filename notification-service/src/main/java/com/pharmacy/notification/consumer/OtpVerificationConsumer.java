package com.pharmacy.notification.consumer;

import com.pharmacy.notification.config.RabbitMQConfig;
import com.pharmacy.notification.dto.OtpVerificationEvent;
import com.pharmacy.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpVerificationConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
    public void handleOtpVerification(OtpVerificationEvent event) {
        log.info("Received OTP verification event for user: {}", event.getUserEmail());
        emailService.sendOtpVerificationEmail(event);
    }
}
