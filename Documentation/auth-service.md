# Auth Service — Deep Dive Documentation

## What is the Auth Service?

The Auth Service owns everything related to **user identity**: registration, login, JWT issuance, token refresh, password reset, and profile management. It is the only service that writes to the `auth_db` database and the only service that generates JWT tokens.

Port: `8081`

The gateway routes all `/api/auth/**` traffic here. Internal admin operations go through `/api/auth/internal/**` which is blocked from external access by the gateway.

---

## File-by-File Breakdown

---

### `AuthServiceApplication.java`

Standard Spring Boot entry point. No special annotations beyond `@SpringBootApplication`.

---

### `config/SecurityConfig.java`

```java
.csrf(AbstractHttpConfigurer::disable)
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
```

This might look surprising — it permits all requests. That's intentional. The auth service doesn't do its own JWT validation because **the gateway already validated the token** before the request arrived. The gateway injects `X-User-Id` and `X-User-Role` headers, so the service just reads those. Spring Security is still present to provide the `PasswordEncoder` bean (BCrypt) and to disable CSRF (not needed for stateless REST APIs).

**BCryptPasswordEncoder** — passwords are never stored in plain text. BCrypt is a slow hashing algorithm by design, making brute-force attacks expensive.

---

### `config/RabbitMQConfig.java`

Configures the auth service as a **RabbitMQ publisher** (not a consumer). It only publishes password reset events.

```java
public static final String EXCHANGE          = "pharmacy.notifications";
public static final String PASSWORD_ROUTING_KEY = "password.reset";
```

The `Jackson2JsonMessageConverter` ensures messages are serialized as JSON, not Java binary format, so the notification-service can deserialize them.

---

### `config/OpenApiConfig.java`

Configures Swagger to point its server URL at the gateway (`http://localhost:8080`) rather than the service's own port. This means when you test from Swagger UI, requests go through the gateway (with auth), not directly to the service.

---

### `controller/AuthController.java`

Public-facing REST controller at `/api/auth`. Endpoints split into two groups:

**Public (no token required):**

| Endpoint | Method | What it does |
|---|---|---|
| `/api/auth/signup` | POST | Creates a new CUSTOMER account, returns token pair |
| `/api/auth/login` | POST | Validates credentials, returns token pair |
| `/api/auth/refresh` | POST | Issues new access token from refresh token |
| `/api/auth/forgot-password` | POST | Sends reset email via RabbitMQ |
| `/api/auth/reset-password` | POST | Resets password using the emailed token |

**Authenticated (token required, injected by gateway as `X-User-Id` header):**

| Endpoint | Method | What it does |
|---|---|---|
| `/api/auth/logout` | POST | Deletes all refresh tokens for the user |
| `/api/auth/me` | GET | Returns the user's profile |
| `/api/auth/update-profile` | PUT | Updates name and mobile |
| `/api/auth/change-password` | POST | Changes password, invalidates all refresh tokens |

Note: `@Parameter(hidden = true)` on `@RequestHeader("X-User-Id")` hides the header from Swagger UI since it's injected by the gateway, not sent by the client.

---

### `controller/InternalAuthController.java`

Internal-only controller at `/api/auth/internal`. The gateway blocks this path from external access (`SetStatus=404`). Only the admin-service calls these via Feign.

| Endpoint | What it does |
|---|---|
| `GET /users` | Paginated user list with role/status/search filters |
| `GET /users/{id}` | Get single user |
| `PATCH /users/{id}/status` | Activate/suspend/deactivate a user |
| `POST /users` | Admin creates a user with any role |

---

### `service/AuthService.java`

The core business logic. Key design decisions:

**Signup** — checks for duplicate email AND mobile before creating. Assigns `CUSTOMER` role and `ACTIVE` status by default.

**Login** — uses `passwordEncoder.matches()` to compare the submitted password against the stored BCrypt hash. Never decrypts — BCrypt is one-way.

**Token rotation on refresh** — when a refresh token is used, the old one is immediately revoked and a new one is issued. This prevents refresh token reuse attacks.

**Forgot password** — always returns the same success message regardless of whether the email exists. This prevents **email enumeration** (an attacker finding out which emails are registered by checking the response).

**Password reset token** — expires in 15 minutes, single-use (`used` flag). On use, all refresh tokens are also invalidated (forces re-login on all devices).

**Change password** — also invalidates all refresh tokens, forcing re-login everywhere.

**buildAuthResponse()** — the shared helper that generates a JWT access token + a UUID refresh token and saves the refresh token to the database.

---

### `service/JwtService.java`

Handles JWT creation and parsing using the `jjwt` library.

```java
public String generateToken(Long userId, String email, String role) {
    return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
}
```

The token payload contains:
- `sub` (subject) = userId — this is what the gateway extracts as `X-User-Id`
- `email` — informational
- `role` — this is what the gateway extracts as `X-User-Role` for authorization

The secret and expiration are injected from `application.yaml` (24 hours by default).

---

### Entity Classes

**`User.java`** — maps to the `users` table. Key fields:
- `password` stored in column `password_hash` — the BCrypt hash, never the plain text
- `role` — enum: `CUSTOMER` or `ADMIN`
- `status` — enum: `ACTIVE`, `INACTIVE`, `SUSPENDED`
- `@PrePersist` / `@PreUpdate` — automatically sets `createdAt` and `updatedAt` timestamps

**`RefreshToken.java`** — maps to `refresh_tokens`. Each user can have multiple refresh tokens (one per device/session). Has `revoked` flag and `expiresAt`. `@ManyToOne` to User with lazy loading.

**`PasswordResetToken.java`** — maps to `password_reset_tokens`. UUID token, 15-minute expiry, single-use (`used` flag). Old tokens are deleted before creating a new one.

**`Role.java`** — simple enum: `CUSTOMER`, `ADMIN`.

**`UserStatus.java`** — simple enum: `ACTIVE`, `INACTIVE`, `SUSPENDED`.

---

### `exception/GlobalExceptionHandler.java`

`@RestControllerAdvice` catches exceptions across all controllers and returns structured JSON error responses.

| Exception | HTTP Status | When it happens |
|---|---|---|
| `DuplicateEmailException` | 409 Conflict | Email or mobile already registered |
| `BadCredentialsException` | 401 Unauthorized | Wrong password, expired token, inactive account |
| `MethodArgumentNotValidException` | 400 Bad Request | Bean validation failures (`@Valid`) |
| `MissingRequestHeaderException` | 401 Unauthorized | Missing `X-User-Id` header |
| `Exception` (catch-all) | 500 Internal Server Error | Anything unexpected |

---

### `resources/application.yaml`

Key settings:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auth_db

  jpa:
    hibernate:
      ddl-auto: update   # auto-creates/updates tables on startup

jwt:
  secret: ...            # must match the gateway's JWT_SECRET
  expiration: 86400000   # 24 hours in milliseconds
```

The JWT secret must be identical between auth-service (which signs tokens) and api-gateway (which verifies them). In production both read from the `JWT_SECRET` environment variable.

---

### `Dockerfile`

Standard: Java 21 JRE Alpine, copies the fat JAR, runs it directly.

---

### `pom.xml`

Key dependencies:

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-security` | BCrypt password encoder, security filter chain |
| `spring-boot-starter-data-jpa` | JPA/Hibernate for PostgreSQL |
| `spring-boot-starter-amqp` | RabbitMQ for publishing password reset events |
| `jjwt-*` | JWT creation and parsing |
| `postgresql` | JDBC driver |
| `dotenv-java` | Loads `.env` file in local development |

---

## Token Flow Summary

```
Client → POST /api/auth/login
  → AuthService.login()
    → BCrypt.matches(password, hash)
    → JwtService.generateToken(userId, email, role)  → access token (24h)
    → UUID.randomUUID()                              → refresh token (30d, stored in DB)
  ← AuthResponse { accessToken, refreshToken, expiresIn, userId, userRole }

Client → any protected request
  → Authorization: Bearer <accessToken>
  → Gateway validates token, injects X-User-Id + X-User-Role
  → Downstream service reads headers

Client → POST /api/auth/refresh { refreshToken }
  → AuthService.refresh()
    → Find token in DB, check not revoked/expired
    → Revoke old token
    → Issue new access + refresh token pair
```
