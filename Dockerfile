FROM gradle:9.0.0-jdk21-alpine AS builder

WORKDIR /app

COPY build.gradle.kts gradle.properties settings.gradle.kts ./
RUN gradle shadowJar -x test --no-daemon
COPY src ./src
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN adduser -u 10001 -D -s /bin/sh curexcrate-bot

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar curexcrate-bot.jar

USER curexcrate-bot

ENTRYPOINT ["java", "-jar", "curexcrate-bot.jar"]
