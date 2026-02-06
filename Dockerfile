FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR file
COPY target/money-manager-backend-1.0.0.jar app.jar

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
