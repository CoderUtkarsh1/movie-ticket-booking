FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && mvn install -DskipTests -B -q

COPY movie-service/.mvn movie-service/.mvn
COPY movie-service/mvnw movie-service/mvnw
COPY movie-service/pom.xml movie-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd movie-service && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY movie-service/src movie-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd movie-service && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/movie-service/target/*.jar app.jar

EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
