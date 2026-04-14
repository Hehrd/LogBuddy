# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21-jammy AS build

WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package && \
    cp target/spark-processing.jar /workspace/app.jar

FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

ENV APP_PORT=8081

COPY --from=build /workspace/app.jar /app/app.jar

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
