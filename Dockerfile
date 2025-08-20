# Multi-stage build for Kotlin Spring Boot application
FROM gradle:8.5-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY buildSrc/ buildSrc/

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY . .

# Build the application (this will also download dependencies)
RUN ./gradlew build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install necessary packages
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built application from builder stage
COPY --from=builder /app/main/build/libs/*.jar app.jar

# Create necessary directories and set permissions
RUN mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
