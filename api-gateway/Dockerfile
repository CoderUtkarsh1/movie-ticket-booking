# =====================================================
# API Gateway Dockerfile — Entry Point for all requests
# =====================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && mvn install -DskipTests -B -q

COPY api-gateway/.mvn api-gateway/.mvn
COPY api-gateway/mvnw api-gateway/mvnw
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd api-gateway && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY api-gateway/src api-gateway/src
RUN --mount=type=cache,target=/root/.m2 \
    cd api-gateway && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/api-gateway/target/*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
