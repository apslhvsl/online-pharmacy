FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

COPY pom.xml ./pom.xml
RUN sed -i 's|<modules>.*</modules>||g; /<module>/d; /<\/modules>/d; /<modules>/d' pom.xml

WORKDIR /build/catalog-service
COPY catalog-service/pom.xml ./pom.xml
COPY catalog-service/src ./src

RUN mvn -f pom.xml clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
COPY --from=builder /build/catalog-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
