# Multi-stage build for DevBrain Backend
# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml and source
COPY pom.xml .
COPY src/ ./src
COPY .mvn/ ./.mvn
COPY mvnw ./

# Build without a missing Maven profile and clear any MAVEN_CONFIG wrapper injection
RUN chmod +x mvnw && \
    MAVEN_CONFIG= ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create directories
RUN mkdir -p /app/logs /data/workspace /tmp/devbrain-uploads && \
    chmod 755 /app/logs /data/workspace /tmp/devbrain-uploads

# Copy built application
COPY --from=builder /build/target/DevBrain-*.jar app.jar

# Non-root user for security
RUN useradd -m -u 1000 devbrain && \
    chown -R devbrain:devbrain /app /data /tmp/devbrain-uploads

USER devbrain

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -Xmx1024m -Xms512m" \
    TZ=UTC

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
