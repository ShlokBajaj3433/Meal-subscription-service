# ─────────────────────────────────────────────────────────────────────────────
# Dockerfile — Multi-stage build for the Spring Boot backend
#
# Stage 1 (build):  Maven + JDK 21 → compile and package the JAR
# Stage 2 (runtime): JRE 21 Alpine → minimal, non-root image
#
# Usage:
#   docker build -t meal-subscription-service .
#   docker run -p 8080:8080 --env-file .env meal-subscription-service
# ─────────────────────────────────────────────────────────────────────────────

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy Maven wrapper and root POM first (layer caches dependency downloads)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copy meal-service module
COPY meal-service/pom.xml meal-service/pom.xml

# Download dependencies without source code (cache layer)
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -pl meal-service --no-transfer-progress

# Copy source and build
COPY meal-service/src meal-service/src
RUN ./mvnw -B package -pl meal-service -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /workspace/meal-service/target/*.jar app.jar

EXPOSE 8080

# JVM flags:
#   UseContainerSupport  → respects Docker memory limits (not host memory)
#   MaxRAMPercentage=75  → use 75% of container memory for heap
#   -Djava.security.egd  → faster startup (avoids /dev/random blocking)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
