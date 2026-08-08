# =====================================================
# Config Server Dockerfile — Multi-Stage Build
# First microservice to boot (others depend on it)
# =====================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy common-lib first (shared dependency)
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src

# Install common-lib to local Maven repo
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && \
    mvn install -DskipTests -B -q

# Copy service files
COPY config-server/.mvn config-server/.mvn
COPY config-server/mvnw config-server/mvnw
COPY config-server/pom.xml config-server/pom.xml

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 \
    cd config-server && chmod +x mvnw && \
    ./mvnw dependency:go-offline -B -q

# Copy source and build
COPY config-server/src config-server/src
RUN --mount=type=cache,target=/root/.m2 \
    cd config-server && ./mvnw package -DskipTests -B -q

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser

COPY --from=builder /build/config-server/target/*.jar app.jar

EXPOSE 8888
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
