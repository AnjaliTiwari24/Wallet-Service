# Stage 1: Build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/money-manager-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set default environment variables
ENV SPRING_PROFILE=prod
ENV SERVER_PORT=8080
ENV DB_URL=jdbc:h2:mem:testdb
ENV DB_DRIVER=org.h2.Driver
ENV DB_USERNAME=sa
ENV DB_PASSWORD=

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
