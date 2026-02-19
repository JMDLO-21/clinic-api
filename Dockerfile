# Etapa 1: Compilar
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/clinic-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]