# Notification Service — Deep Dive Documentation

## What is the Notification Service?

The Notification Service is responsible for sending **emails** to users. It does not expose any REST endpoints. Instead, it listens to RabbitMQ queues and sends HTML emails when messages arrive.

Port: `8085`

It handles two types of notifications:
1. Order status updates (e.g., "Your order RX-2026-00042 is now PACKED")
2. Password reset emails (with a one-time token)

This is a classic **event-driven** design. The services that trigger notifications (auth-service, order-service) don't know or care about the notification-service. They just publish a message to RabbitMQ and move on.

---

## File-by-File Breakdown

---

### `NotificationServiceApplication.java`

```java
Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
SpringApplication.run(NotificationServiceApplication.class, args);
```

Before starting Spring, it loads the `.env` file using the `dotenv-java` library and injects all variables as system properties. This allows local development without setting environment variables manually. `ignoreIfMissing()` means it won't crash if there's no `.env` file (e.g., in Docker where env vars are set directly).

---

### `config/RabbitMQConfig.java`

Defines the full RabbitMQ topology that the notification-service owns:

```java
public static final String EXCHANGE         = "pharmacy.notifications";
public static final String ORDER_QUEUE      = "order.notification.queue";
public static final String PASSWORD_QUEUE   = "password.notification.queue";
public static final String ORDER_ROUTING_KEY    = "order.update";
public static final String PASSWORD_ROUTING_KEY = "password.reset";
```

**Exchange**: `pharmacy.notifications` — a `DirectExchange`. Messages are routed to queues based on an exact routing key match.

**Queues**:
- `order.notification.queue` — receives order update events
- `password.notification.queue` — receives password reset events

Both queues are **durable** (`QueueBuilder.durable()`), meaning they survive RabbitMQ restarts. Messages won't be lost if RabbitMQ goes down briefly.

**Bindings** connect queues to the exchange via routing keys:
- `order.update` → `order.notification.queue`
- `password.reset` → `password.notification.queue`

**Why this service owns the queue/exchange declarations**: In RabbitMQ, it's safe for multiple services to declare the same exchange/queue — if it already exists with the same settings, nothing happens. The notification-service declares everything it needs so it works even if it starts before the publishers.

---

### `consumer/OrderNotificationConsumer.java`

```java
@RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
public void handleOrderUpdate(OrderNotificationEvent event) {
    log.info("Received order notification event for order: {}", event.getOrderNumber());
    emailService.sendOrderUpdateEmail(event);
}
```

`@RabbitListener` registers this method as a message handler for the order queue. Spring AMQP automatically deserializes the JSON message into an `OrderNotificationEvent` object using the `Jackson2JsonMessageConverter` configured in `RabbitMQConfig`.

If the email fails, the exception is logged but the message is acknowledged (removed from queue). There's no retry/dead-letter queue configured — failed notifications are lost. This is acceptable since notifications are non-critical.

---

### `consumer/PasswordResetConsumer.java`

Same pattern as `OrderNotificationConsumer` but for the password reset queue. Receives `PasswordResetEvent` and calls `emailService.sendPasswordResetEmail()`.

---

### `service/EmailService.java`

The actual email sending logic using Spring's `JavaMailSender` and Thymeleaf templates.

**`sendOrderUpdateEmail()`**:
1. Creates a Thymeleaf `Context` with template variables (userName, orderNumber, status, totalAmount, updatedAt)
2. Processes the `order-update` template to produce HTML
3. Calls `sendHtmlEmail()`

**`sendPasswordResetEmail()`**:
1. Creates a Thymeleaf `Context` with template variables (userName, resetToken, expiresAt)
2. Processes the `password-reset` template to produce HTML
3. Calls `sendHtmlEmail()`

**`sendHtmlEmail()`** — the shared private method:
```java
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
helper.setFrom(fromAddress);
helper.setTo(to);
helper.setSubject(subject);
helper.setText(htmlBody, true);  // true = isHtml
mailSender.send(message);
```

`MimeMessageHelper` with `true` as the second argument enables multipart mode (required for HTML emails). The `fromAddress` is injected from `notification.mail.from` in `application.yaml`.

If sending fails, it logs the error but doesn't rethrow — the consumer won't crash.

---

### `dto/OrderNotificationEvent.java`

The message payload published by order-service and consumed here:

```java
private Long userId;
private String userEmail;
private String userName;
private String orderNumber;
private String status;
private BigDecimal totalAmount;
private LocalDateTime updatedAt;
```

Implements `Serializable` (required for message serialization). Uses Lombok `@Builder` for clean construction in order-service.

---

### `dto/PasswordResetEvent.java`

The message payload published by auth-service:

```java
private Long userId;
private String userEmail;
private String userName;
private String resetToken;
private LocalDateTime expiresAt;
```

---

### `templates/order-update.html`

Thymeleaf HTML email template for order updates. Uses `th:text` attributes to inject dynamic values:

```html
<p>Your order <strong th:text="${orderNumber}">ORD-0000</strong> has been updated.</p>
```

Renders a table with status, total amount, and update timestamp. Inline CSS for email client compatibility (most email clients strip `<style>` tags).

---

### `templates/password-reset.html`

Thymeleaf HTML email template for password resets. Displays the reset token prominently in a styled box:

```html
<strong style="font-size:20px; letter-spacing:2px;" th:text="${resetToken}">TOKEN</strong>
```

Uses `#temporals.format()` (Thymeleaf's date formatting utility) to display the expiry time in a human-readable format.

---

### `resources/application.yaml`

```yaml
server:
  port: 8085

spring:
  mail:
    host: mail.smtp2go.com
    port: 587
    username: ${MAIL_USERNAME:dev@example.com}
    password: ${MAIL_PASSWORD:changeme}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

notification:
  mail:
    from: ${MAIL_FROM:noreply@yourdomain.com}
```

Uses **SMTP2GO** as the email provider (port 587 with STARTTLS). Credentials are injected from environment variables. The `from` address is separate from the SMTP username — this is the "From:" field in the email.

---

### `pom.xml`

Key dependencies:

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-amqp` | RabbitMQ consumer |
| `spring-boot-starter-mail` | JavaMailSender for SMTP |
| `spring-boot-starter-thymeleaf` | HTML email template engine |
| `spring-boot-starter-web` | Required for Eureka HTTP transport |
| `dotenv-java` | Load `.env` file in local dev |

Note: `spring-boot-starter-web` is included not because this service has REST endpoints, but because the Eureka client needs an HTTP transport layer to communicate with the registry.

---

## Message Flow

```
auth-service
  → forgotPassword() called
  → Saves PasswordResetToken to DB
  → rabbitTemplate.convertAndSend("pharmacy.notifications", "password.reset", event)
  → Returns immediately (fire and forget)

RabbitMQ
  → Routes message to "password.notification.queue"

notification-service
  → PasswordResetConsumer.handlePasswordReset(event)
  → EmailService.sendPasswordResetEmail(event)
  → Thymeleaf renders password-reset.html
  → JavaMailSender sends via SMTP2GO
  → User receives email with reset token
```

```
order-service
  → Any status change (cancel, confirm, etc.)
  → OrderEventPublisher.publishOrderUpdate(order) [@Async]
  → Fetches user email from auth-service via Feign
  → rabbitTemplate.convertAndSend("pharmacy.notifications", "order.update", event)

RabbitMQ
  → Routes message to "order.notification.queue"

notification-service
  → OrderNotificationConsumer.handleOrderUpdate(event)
  → EmailService.sendOrderUpdateEmail(event)
  → Thymeleaf renders order-update.html
  → JavaMailSender sends via SMTP2GO
  → User receives order status email
```
