package com.pharmacy.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Declares the exchange, queues, and bindings for the notification service
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE         = "pharmacy.notifications";
    public static final String ORDER_QUEUE      = "order.notification.queue";
    public static final String PASSWORD_QUEUE   = "password.notification.queue";
    public static final String ORDER_ROUTING_KEY    = "order.update";
    public static final String PASSWORD_ROUTING_KEY = "password.reset";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder.durable(ORDER_QUEUE).build();
    }

    @Bean
    public Queue passwordNotificationQueue() {
        return QueueBuilder.durable(PASSWORD_QUEUE).build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderNotificationQueue())
                .to(notificationExchange())
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding passwordBinding() {
        return BindingBuilder.bind(passwordNotificationQueue())
                .to(notificationExchange())
                .with(PASSWORD_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
