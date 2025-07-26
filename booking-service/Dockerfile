# Multi-stage build for booking service
FROM maven:3-openjdk-17 as builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/booking-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 