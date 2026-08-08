FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
COPY common-lib/mvnw common-lib/mvnw
COPY common-lib/.mvn common-lib/.mvn
RUN --mount=type=cache,target=/root/.m2 \
    cd common-lib && chmod +x mvnw && ./mvnw install -DskipTests -B -q

COPY user-service/.mvn user-service/.mvn
COPY user-service/mvnw user-service/mvnw
COPY user-service/pom.xml user-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd user-service && chmod +x mvnw && ./mvnw dependency:go-offline -B -q

COPY user-service/src user-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd user-service && ./mvnw package -DskipTests -B -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgrp && adduser -S appuser -G appgrp
USER appuser
COPY --from=builder /build/user-service/target/*.jar app.jar

EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
