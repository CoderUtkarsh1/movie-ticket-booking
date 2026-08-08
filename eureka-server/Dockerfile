# =====================================================
# Eureka Server Dockerfile — Service Discovery
# =====================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && mvn install -DskipTests -B -q

COPY eureka-server/.mvn eureka-server/.mvn
COPY eureka-server/mvnw eureka-server/mvnw
COPY eureka-server/pom.xml eureka-server/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd eureka-server && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY eureka-server/src eureka-server/src
RUN --mount=type=cache,target=/root/.m2 \
    cd eureka-server && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/eureka-server/target/*.jar app.jar

EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8761/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
