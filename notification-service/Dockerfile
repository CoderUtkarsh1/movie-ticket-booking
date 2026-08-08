FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && mvn install -DskipTests -B -q

COPY notification-service/.mvn notification-service/.mvn
COPY notification-service/mvnw notification-service/mvnw
COPY notification-service/pom.xml notification-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd notification-service && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY notification-service/src notification-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd notification-service && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/notification-service/target/*.jar app.jar

EXPOSE 8087
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8087/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
