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

# Copy backend module
COPY backend/pom.xml backend/pom.xml

# Download dependencies without source code (cache layer)
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline -pl backend --no-transfer-progress

# Copy source and build
COPY backend/src backend/src
RUN ./mvnw -B package -pl backend -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /workspace/backend/target/*.jar app.jar

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
