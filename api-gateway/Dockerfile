FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml ./pom.xml
RUN sed -i 's|<modules>.*</modules>||g; /<module>/d; /<\/modules>/d; /<modules>/d' pom.xml

WORKDIR /build/api-gateway
COPY api-gateway/pom.xml ./pom.xml
COPY api-gateway/src ./src

RUN mvn -f pom.xml clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=builder /build/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
