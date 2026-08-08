FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && mvn install -DskipTests -B -q

COPY theater-service/.mvn theater-service/.mvn
COPY theater-service/mvnw theater-service/mvnw
COPY theater-service/pom.xml theater-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd theater-service && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY theater-service/src theater-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd theater-service && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/theater-service/target/*.jar app.jar

EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8083/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
