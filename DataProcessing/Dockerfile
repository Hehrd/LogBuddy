# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21-jammy AS build

WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package && \
    cp build/logBuddy.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /workspace/app.jar /app/app.jar

RUN addgroup -S app && adduser -S -G app app && chown -R app:app /app

USER app

EXPOSE 6969 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
