FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="dev@nivasafinance.com"

# Install timezone data for proper logging
RUN apk add --no-cache tzdata

WORKDIR /app

COPY main/build/libs/main.jar /app/navigator.jar

# Create a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# JVM options
ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -XX:+UseContainerSupport"

CMD ["java", "-jar", "/app/navigator.jar"]
