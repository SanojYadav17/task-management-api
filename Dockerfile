# ========================================================
# Multi-Stage Dockerfile for Task Management REST API
# Candidate: Sanoj Yadav
# Company: Ikaansh Edutech Private Limited
# ========================================================

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace/app

# Copy Maven wrapper and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies offline (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy application source code
COPY src ./src

# Compile and package application (skip tests during container packaging)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Lightweight production runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root system user for secure container execution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built executable jar from builder stage
COPY --from=builder /workspace/app/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose Spring Boot default port
EXPOSE 8080

# Configure JVM container memory flags and launch app
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

