FROM eclipse-temurin:21-jre-noble

LABEL maintainer="dev@nivasafinance.com"

RUN apt-get update \
 && apt-get install -y --no-install-recommends tzdata libgcc-s1 ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY main/build/libs/main.jar /app/navigator.jar

RUN groupadd --system appgroup \
 && useradd --system --gid appgroup --create-home --home-dir /home/appuser --shell /usr/sbin/nologin appuser \
 && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -XX:+UseContainerSupport"

CMD ["java", "-jar", "/app/navigator.jar"]