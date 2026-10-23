# Multi-stage Dockerfile for Spring Boot Backend
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/fraud-detection-backend-1.0.0.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=mysql
ENV ML_SERVICE_URL=http://ml-service:5001

ENTRYPOINT ["java", "-jar", "app.jar"]
