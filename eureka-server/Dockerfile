FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Parent pom must be at ../pom.xml relative to the service (matching relativePath in child pom)
COPY pom.xml ./pom.xml
# Strip <modules> block so Maven doesn't look for sibling services
RUN sed -i 's|<modules>.*</modules>||g; /<module>/d; /<\/modules>/d; /<modules>/d' pom.xml

WORKDIR /build/eureka-server
COPY eureka-server/pom.xml ./pom.xml
COPY eureka-server/src ./src

RUN mvn -f pom.xml clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=builder /build/eureka-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
