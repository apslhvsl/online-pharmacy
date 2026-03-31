package com.pharmacy.notification.service;

import com.pharmacy.notification.dto.OrderNotificationEvent;
import com.pharmacy.notification.dto.PasswordResetEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail.from}")
    private String fromAddress;

    public void sendOrderUpdateEmail(OrderNotificationEvent event) {
        Context ctx = new Context();
        ctx.setVariable("userName", event.getUserName());
        ctx.setVariable("orderNumber", event.getOrderNumber());
        ctx.setVariable("status", event.getStatus());
        ctx.setVariable("totalAmount", event.getTotalAmount());
        ctx.setVariable("updatedAt", event.getUpdatedAt());

        String html = templateEngine.process("order-update", ctx);
        sendHtmlEmail(event.getUserEmail(), "Order Update - " + event.getOrderNumber(), html);
    }

    public void sendPasswordResetEmail(PasswordResetEvent event) {
        Context ctx = new Context();
        ctx.setVariable("userName", event.getUserName());
        ctx.setVariable("resetToken", event.getResetToken());
        ctx.setVariable("expiresAt", event.getExpiresAt());

        String html = templateEngine.process("password-reset", ctx);
        sendHtmlEmail(event.getUserEmail(), "Password Reset Request", html);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content
            mailSender.send(message);
            log.info("Email sent to {} | subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {} | reason: {}", to, e.getMessage());
        }
    }
}
