FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml ./pom.xml
RUN sed -i 's|<modules>.*</modules>||g; /<module>/d; /<\/modules>/d; /<modules>/d' pom.xml

WORKDIR /build/order-service
COPY order-service/pom.xml ./pom.xml
COPY order-service/src ./src

RUN mvn -f pom.xml clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=builder /build/order-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
