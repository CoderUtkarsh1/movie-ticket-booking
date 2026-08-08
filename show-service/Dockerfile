FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
COPY common-lib/mvnw common-lib/mvnw
COPY common-lib/.mvn common-lib/.mvn
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && chmod +x mvnw && ./mvnw install -DskipTests -B -q

COPY show-service/.mvn show-service/.mvn
COPY show-service/mvnw show-service/mvnw
COPY show-service/pom.xml show-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd show-service && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY show-service/src show-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd show-service && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/show-service/target/*.jar app.jar

EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
