# Simple build for development - using locally built JAR
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the pre-built JAR from local target directory
COPY target/booking-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 