# Eureka Server — Deep Dive Documentation

## What is the Eureka Server?

The Eureka Server is the **service registry** for the entire pharmacy system. Every other service (auth, catalog, order, admin, notification, api-gateway) registers itself here on startup. When one service needs to call another, it asks Eureka for the current IP and port instead of hardcoding addresses. This is called **service discovery**.

Without Eureka, you'd have to hardcode `http://auth-service:8081` everywhere. With Eureka, you just say `lb://auth-service` and the infrastructure resolves it dynamically — which is critical in Docker/Kubernetes where IPs change.

Port: `8761`

---

## File-by-File Breakdown

---

### `EurekaServerApplication.java`

```
eureka-server/src/main/java/com/pharmacy/eureka/EurekaServerApplication.java
```

The entry point. Two annotations do all the work:

- `@SpringBootApplication` — bootstraps the Spring context
- `@EnableEurekaServer` — turns this app into a Eureka registry server. Without this, it would just be a blank Spring Boot app.

---

### `resources/application.yml`

```
eureka-server/src/main/resources/application.yml
```

**Server port**
```yaml
server:
  port: 8761
```
The standard Eureka port. All other services point to `http://eureka-server:8761/eureka/`.

**Self-registration disabled**
```yaml
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```
The Eureka server doesn't register itself in its own registry — it IS the registry. These two flags prevent it from trying to do so.

**Dev optimizations**
```yaml
wait-time-in-ms-when-sync-empty: 0
enable-self-preservation: false
eviction-interval-timer-in-ms: 2000
```

- `wait-time-in-ms-when-sync-empty: 0` — normally Eureka waits before serving traffic if it has no peers to sync with. Setting to 0 makes it start immediately, which is useful in dev.
- `enable-self-preservation: false` — in production, Eureka has a "self-preservation" mode where it stops evicting services if it thinks the network is having issues. In dev, this causes stale/dead service entries to linger. Disabling it means dead services are removed quickly.
- `eviction-interval-timer-in-ms: 2000` — checks for dead services every 2 seconds instead of the default 60 seconds. Makes dev restarts much faster.

---

### `pom.xml`

One dependency: `spring-cloud-starter-netflix-eureka-server`. That's it. The Eureka server is intentionally minimal — no database, no business logic, just the registry.

The Lombok annotation processor is explicitly removed here (via `combine.self="override"`) because Eureka server has no Lombok usage and doesn't need it.

---

### `Dockerfile`

Standard minimal image: Java 21 JRE on Alpine Linux. Same pattern as all other services.

---

## How Other Services Connect

Every other service has this in their `application.yml`:

```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}
  instance:
    prefer-ip-address: true
```

- `defaultZone` — the URL of this Eureka server. In Docker, it's overridden to `http://eureka-server:8761/eureka/` via environment variable.
- `prefer-ip-address: true` — registers using the container's IP rather than hostname, which is more reliable in Docker networking.

The Eureka dashboard is accessible at `http://localhost:8761` and shows all registered services, their instance IDs, and health status.
