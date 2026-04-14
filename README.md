# LogBuddy

## Overview

LogBuddy is a log-ingestion and rule-based alerting system built as three Java microservices:

1. `SparkProcessing` reads logs from streaming or file-based sources with Apache Spark Structured Streaming.
2. `DataProcessing` receives parsed log batches over gRPC, evaluates configurable rules, and sends alerts to HTTP endpoints.
3. `ControlPanel` appears intended to be a small HTTP gateway for controlling the other services, but it is currently incomplete.

The project solves a common operational problem: collecting logs from multiple platforms, normalizing them into a common structure, checking them against detection rules, and triggering alerts when combinations of rule conditions are met.

At a high level:

- Spark handles ingestion and parsing.
- A Spring Boot service handles rule evaluation and alert dispatch.
- A lightweight control layer is intended to expose operational endpoints such as health, reload, sleep/wake, and query listing/stopping.

## Architecture

### High-level design

- `SparkProcessing` is the ingestion engine.
- `DataProcessing` is the decision engine.
- `ControlPanel` is meant to be the operator-facing entrypoint.

### How the services interact

- `SparkProcessing` communicates with `DataProcessing` using **gRPC client-streaming**.
- `DataProcessing` communicates with external alert receivers using **HTTP POST**.
- `ControlPanel` is intended to communicate with the other services using **REST over HTTP**.

### ASCII diagram

```text
+--------------------+
| External Log       |
| Sources            |
| - Kafka            |
| - Pulsar           |
| - Files            |
| - Delta/Iceberg    |
| - Hudi             |
| - Socket/Rate      |
+---------+----------+
          |
          | Spark Structured Streaming
          v
+--------------------+        gRPC stream        +----------------------+
| SparkProcessing    | ------------------------> | DataProcessing       |
| - reads streams    |                           | - evaluates rules    |
| - parses logs      |                           | - tracks sessions    |
| - batches entries  |                           | - triggers alerts    |
+---------+----------+                           +----------+-----------+
          ^                                                |
          | HTTP control                                   | HTTP POST
          |                                                v
+---------+----------+                           +----------------------+
| ControlPanel       |                           | Alert Endpoints       |
| - intended gateway |                           | Slack/webhook/API     |
| - proxies commands |                           | destinations          |
+--------------------+                           +----------------------+
```

## Microservices Breakdown

### Service 1: ControlPanel

#### Description

`ControlPanel` looks like a planned Spring Boot REST gateway that should expose operational endpoints for the other services. Based on the controllers, it is meant to forward requests to Spark and Data Processing.

Important note: this service is currently not production-ready in the repository. Its `Main.java` is still the IntelliJ template starter, there is no `@SpringBootApplication`, and the controllers do not yet match the Spark service paths exactly.

#### Tech stack

- Java 21
- Spring Boot 4
- Spring Web
- `RestTemplate`

#### Key responsibilities

- Proxy control requests to `SparkProcessing`
- Potentially proxy control requests to `DataProcessing`
- Provide a single operator-facing HTTP API

#### Important files

- `ControlPanel/pom.xml`
- `ControlPanel/src/main/java/com/logbuddy/control/panel/Main.java`
- `ControlPanel/src/main/java/com/logbuddy/control/panel/config/WebConfig.java`
- `ControlPanel/src/main/java/com/logbuddy/control/panel/controller/ControlPanelController.java`
- `ControlPanel/src/main/java/com/logbuddy/control/panel/controller/DataProcessingControlPanelController.java`
- `ControlPanel/src/main/java/com/logbuddy/control/panel/controller/SparkControlPanelController.java`

#### API endpoints

These endpoints are defined in code, but the service is not currently runnable as-is:

- `GET /spark/reload-settings`
- `GET /spark/stop-query/{queryId}`
- `GET /spark/list-queries`

Observed issues:

- `SPARK_HOST` is just `"localhost"` and does not include a scheme or port.
- The controller expects `/reload-settings` and `/stop-query`, while `SparkProcessing` actually exposes `/reload-config` and `/terminate-query`.
- `DataProcessingControlPanelController` has no implemented endpoints yet.

### Service 2: DataProcessing

#### Description

`DataProcessing` is the core rule engine. It loads JSON configuration files, maintains per-data-source processing state, accepts parsed log entries over gRPC, applies rules, groups rule completions into alerts, and sends alerts to configured HTTP endpoints.

#### Tech stack

- Java 21
- Spring Boot 4
- Spring Web
- Spring WebFlux `WebClient`
- Spring gRPC
- Protocol Buffers / gRPC
- Log4j2
- Lombok

#### Key responsibilities

- Start a gRPC server for log ingestion
- Load application, data source, and rule configuration from disk
- Maintain in-memory rule sessions and alert sessions
- Evaluate rule checks against incoming log entries
- Send alerts asynchronously to configured HTTP endpoints
- Expose operational REST endpoints for health and sleep/wake control

#### Important files

- `DataProcessing/pom.xml`
- `DataProcessing/src/main/resources/application.properties`
- `DataProcessing/src/main/proto/ingest.proto`
- `DataProcessing/src/main/java/com/alexander/processing/Main.java`
- `DataProcessing/src/main/java/com/alexander/processing/ProcessingContext.java`
- `DataProcessing/src/main/java/com/alexander/processing/controller/ControlPanelController.java`
- `DataProcessing/src/main/java/com/alexander/processing/data/config/AppSettingsConfig.java`
- `DataProcessing/src/main/java/com/alexander/processing/data/service/ds/DataSourceIngestService.java`
- `DataProcessing/src/main/java/com/alexander/processing/data/service/ds/DataProcessingService.java`
- `DataProcessing/src/main/java/com/alexander/processing/data/service/rule/RuleProcessingService.java`
- `DataProcessing/src/main/java/com/alexander/processing/data/service/alert/AlertingService.java`

#### API endpoints

REST endpoints:

- `GET /api/control-panel/health`
- `GET /api/control-panel/status`
- `GET /api/control-panel/sleep`
- `GET /api/control-panel/wake`
- `GET /api/control-panel/restart` returns `501 Not Implemented`

gRPC service:

```proto
service IngestService {
  rpc ingest (stream IngestRequest) returns (IngestResponse);
}
```

`IngestRequest` contains:

- `dsName`: data source name
- `logEntries`: repeated normalized log entries

`IngestResponse` contains:

- `received`: number of streamed request messages received

#### Supported rule types

The rule engine currently supports these check types:

- `LogLevelCheck`
- `DataRegexMatchCheck`
- `MessageLengthCheck`
- `TimestampCheck`

#### Configuration expectations

The service expects these files to exist:

- `/opt/logbuddy/config/ds.conf`
- `/opt/logbuddy/config/rule.conf`
- `/opt/logbuddy/config/app.conf`

From the settings models, these files appear to contain:

- `ds.conf`: data sources, log formats, required rules, alert definitions, schedules
- `rule.conf`: named rules and their check definitions
- `app.conf`: `controlPanelServerPort` and `grpcSettings`

### Service 3: SparkProcessing

#### Description

`SparkProcessing` is the ingestion microservice. It reads configured streaming sources with Spark Structured Streaming, parses each record into a shared log structure, buffers records into batches, and streams those batches to `DataProcessing` over gRPC.

It also starts a small embedded HTTP server for runtime controls such as reloading config and stopping or listing active Spark queries.

#### Tech stack

- Java 21
- Apache Spark 4.1 Structured Streaming
- Scala 2.13 Spark artifacts
- gRPC / Protocol Buffers
- Log4j2
- Lombok
- JDK built-in `HttpServer`

#### Key responsibilities

- Load application and data-source configuration from disk
- Start and manage Spark streaming queries
- Support multiple source connectors
- Parse JSON, logfmt, and table-style logs
- Push parsed log batches to `DataProcessing` over gRPC
- Expose simple HTTP control endpoints for query management

#### Important files

- `SparkProcessing/pom.xml`
- `SparkProcessing/src/main/java/com/alexander/spark/Main.java`
- `SparkProcessing/src/main/java/com/alexander/spark/RuntimeContext.java`
- `SparkProcessing/src/main/java/com/alexander/spark/controlpanel/controller/ControlPanelController.java`
- `SparkProcessing/src/main/java/com/alexander/spark/controlpanel/service/ControlPanelService.java`
- `SparkProcessing/src/main/java/com/alexander/spark/job/service/SparkService.java`
- `SparkProcessing/src/main/java/com/alexander/spark/job/service/QueryScheduler.java`
- `SparkProcessing/src/main/java/com/alexander/spark/job/service/GrpcWriter.java`
- `SparkProcessing/src/main/proto/ingest.proto`

#### API endpoints

HTTP endpoints exposed by the embedded server:

- `GET /control-plane/status`
- `GET /control-plane/reload-config`
- `GET /control-plane/terminate-query` with header `Query-Id: <data-source-name>`
- `GET /control-plane/list-queries`

Behavior notes:

- `terminate-query` uses the request header named `Query-Id`, but the value passed into `ControlPanelService.stopQuery` is actually treated as the data source name, not the Spark UUID.
- `reload-config` stops all active queries, reloads config from disk, and reschedules all queries.

#### Supported source platforms

The connector enum shows support for:

- Kafka
- Pulsar
- File text streams
- Delta Lake
- Apache Iceberg
- Apache Hudi
- Socket streams
- Spark rate source

#### Supported log formats

- JSON
- LOGFMT
- TABLE
- CUSTOM is present in the model, but the Spark parser side does not implement a dedicated custom parser yet

## Data Flow

This is the intended end-to-end flow for a single log stream:

1. `SparkProcessing` loads `ds.conf` and `app.conf`.
2. For each configured data source, `QueryScheduler` schedules a Spark Structured Streaming query.
3. A connector reads new records from Kafka, Pulsar, files, lakehouse tables, or another configured platform.
4. `SparkService` chooses the appropriate parser based on `logFormat.logType()`.
5. Parsed rows become `LogEntryDTO` objects.
6. `GrpcWriter` buffers entries and sends them to `DataProcessing` using the `IngestService.ingest` client-streaming gRPC API.
7. `DataSourceIngestService` receives each streamed request and forwards the entries to `DataProcessingService`.
8. `DataProcessingService` looks up the data source definition and evaluates each required rule.
9. Rule results are accumulated inside in-memory processing sessions and alert sessions.
10. When all required rule completions for an alert condition are satisfied, `AlertingService` sends the alert to one or more configured HTTP endpoints.

## Technologies Used

- Java 21
- Spring Boot 4
- Spring Web
- Spring WebFlux
- Spring gRPC
- Apache Spark 4.1
- Protocol Buffers
- gRPC
- Maven
- Log4j2
- Lombok
- JDK `HttpServer`
- Optional stream and storage integrations:
  - Kafka
  - Pulsar
  - Delta Lake
  - Apache Iceberg
  - Apache Hudi
  - Kinesis connector class exists in source, though the dependency setup should be verified separately

## Setup Instructions

### Prerequisites

- JDK 21
- Maven 3.9+
- A machine capable of running local Spark jobs
- Access to whatever input platform your data source config references
- Writable config directory matching the hard-coded paths used by the services

Recommended directory:

```text
/opt/logbuddy/config
```

On Windows, you will likely need to adapt the code or create equivalent paths because both `SparkProcessing` and `DataProcessing` currently expect Linux-style absolute paths.

### Installation steps

1. Clone the repository.
2. Create the required config directory.
3. Create the config files:
   - `app.conf`
   - `ds.conf`
   - `rule.conf`
4. Build each service with Maven.

Example build commands:

```bash
cd ControlPanel
mvn clean package

cd ../DataProcessing
mvn clean package

cd ../SparkProcessing
mvn clean package
```

### Environment variables

No required environment variables are defined in the codebase.

Instead, the project relies mostly on hard-coded config file paths:

```text
/opt/logbuddy/config/app.conf
/opt/logbuddy/config/ds.conf
/opt/logbuddy/config/rule.conf
```

If you want a more portable setup, a good improvement would be to make these paths configurable via environment variables.

### Expected config shape

There are no sample config files in the repository, so this example is inferred from the Java record classes and should be treated as a starting point, not guaranteed final schema.

#### Example `app.conf`

```json
{
  "serverPort": 8081,
  "controlPanelServerPort": 8080,
  "grpcSettings": {
    "serverHost": "localhost",
    "serverPort": 9090,
    "maxLinesPerReq": 100
  }
}
```

Notes:

- `SparkProcessing` appears to use `serverPort`.
- `DataProcessing` appears to use `controlPanelServerPort` and `grpcSettings`.
- Sharing one file between both services may require both fields to be present.

#### Example `ds.conf`

```json
{
  "dataSources": {
    "app-logs": {
      "name": "app-logs",
      "path": "/var/log/app.log",
      "pathInfo": {
        "platform": "FILE_TEXT",
        "location": "/var/log/input",
        "options": {
          "maxFilesPerTrigger": "1"
        }
      },
      "logFormat": {
        "logType": "JSON",
        "defaultFields": {
          "timestamp": "timestamp",
          "timestampFormat": "yyyy-MM-dd HH:mm:ss",
          "level": "level",
          "message": "message",
          "source": "source",
          "data": "data",
          "logger": "logger"
        },
        "customFields": {
          "requestId": "STRING"
        }
      },
      "requiredRules": ["error-level-rule"],
      "alertData": {
        "error-alert": {
          "alertName": "error-alert",
          "requiredRules": ["error-level-rule"],
          "timeWindowMillis": 60000,
          "alertEndpoints": ["http://localhost:9000/webhook"],
          "aiOverviewEnabled": false
        }
      },
      "schedule": {
        "delayAfterStartUpMillis": 1000,
        "intervalsMillis": []
      }
    }
  }
}
```

#### Example `rule.conf`

```json
{
  "rules": {
    "error-level-rule": {
      "ruleName": "error-level-rule",
      "check": {
        "level": "ERROR"
      },
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 1
    }
  }
}
```

Important caveat:

- The exact JSON polymorphism for `check` objects is not obvious from the code alone. The repository defines the check classes, but there is no sample configuration showing how Jackson distinguishes between `LogLevelCheck`, `TimestampCheck`, `MessageLengthCheck`, and `DataRegexMatchCheck`.

### How to run each service

#### Run DataProcessing

```bash
cd DataProcessing
mvn spring-boot:run
```

Expected defaults:

- gRPC server port: `9090` from `application.properties`
- Config files:
  - `/opt/logbuddy/config/ds.conf`
  - `/opt/logbuddy/config/rule.conf`
  - `/opt/logbuddy/config/app.conf`

#### Run SparkProcessing

```bash
cd SparkProcessing
mvn clean package
java -jar target/spark-processing.jar
```

Notes:

- The final shaded JAR name should be `target/spark-processing.jar` because `finalName` is `spark-processing`.
- The service starts Spark locally with `local[*]`.
- It also starts an embedded HTTP server on the `serverPort` value from `app.conf`.

#### Run ControlPanel

The current codebase does not contain a runnable Spring Boot bootstrap class for this service, so these instructions are tentative:

```bash
cd ControlPanel
mvn clean package
```

Before this service can run properly, it likely needs:

- `@SpringBootApplication` in `Main.java`
- a valid server port configuration
- proper downstream base URLs including `http://` and ports
- endpoint path alignment with `SparkProcessing`

## Usage

Because `ControlPanel` is incomplete, the most reliable usage examples are against `DataProcessing` and `SparkProcessing` directly.

### Check DataProcessing health

```bash
curl -i http://localhost:8080/api/control-panel/health
```

Expected response:

```http
HTTP/1.1 200 OK
```

The exact HTTP port is not explicit in `application.properties`, so unless another config overrides it, Spring Boot default port `8080` is the most likely assumption.

### Put DataProcessing to sleep

```bash
curl -i http://localhost:8080/api/control-panel/sleep
```

Expected behavior:

- Service returns `200 OK`
- Incoming gRPC batches are ignored while the sleep flag is enabled

### Wake DataProcessing back up

```bash
curl -i http://localhost:8080/api/control-panel/wake
```

### Check SparkProcessing status

```bash
curl -i http://localhost:8081/control-plane/status
```

Expected response:

```http
HTTP/1.1 200 OK
```

Assumption:

- `8081` is just an example. Use the `serverPort` value from `app.conf`.

### List active Spark queries

```bash
curl -i http://localhost:8081/control-plane/list-queries
```

Expected response:

```json
["app-logs", "security-stream"]
```

### Reload Spark config

```bash
curl -i http://localhost:8081/control-plane/reload-config
```

Expected behavior:

- Existing queries are stopped
- Config files are reloaded from disk
- Queries are scheduled again

### Stop a Spark query

```bash
curl -i -H "Query-Id: app-logs" http://localhost:8081/control-plane/terminate-query
```

Expected behavior:

- The named query is stopped
- The header name says `Query-Id`, but the value used in code is the data source name

## Development Notes

### Assumptions made

- The project title should remain `LogBuddy`, based on the root README, package names, Spark app name, and config paths.
- `ControlPanel` is intended to be a Spring Boot API gateway, even though its bootstrap class is unfinished.
- `DataProcessing` REST API likely runs on Spring Boot default port `8080` unless overridden elsewhere.
- `SparkProcessing` runs its HTTP API on `app.conf.serverPort`.
- Config files are JSON, even though they use `.conf` extensions.

### Unclear or inconsistent parts

- `ControlPanel` is incomplete and currently not runnable as a real microservice.
- `ControlPanel` endpoint names do not match the endpoint names implemented by `SparkProcessing`.
- `SparkProcessing` controller path matching uses `getRequestURI().getHost()` in a way that may not behave as intended.
- `DataProcessing` has `controlPanelServerPort` in config, but its REST server port is not directly wired from that field in the visible code.
- `DataSource.path` exists in `DataProcessing` but `SparkProcessing` uses `pathInfo` instead; this suggests the two services may expect slightly different config models.
- `rule.conf` polymorphic deserialization format for the `check` field is not obvious without example config.
- `DataProcessing/Dockerfile` looks inconsistent:
  - it copies `build/logBuddyProcessing-exec.jar`
  - it exposes port `6969`
  - it runs `logBuddy-exec.jar`
  These names do not match each other cleanly.

### Suggestions for improvement

- Add a root parent Maven project for the three services.
- Provide sample `app.conf`, `ds.conf`, and `rule.conf`.
- Finish the `ControlPanel` bootstrap and align its proxied routes with the downstream services.
- Replace hard-coded config paths with environment variables.
- Add OpenAPI or gRPC documentation.
- Add integration tests for Spark-to-gRPC-to-alert flow.
- Add durable state or persistence if alerts must survive restarts.

## Folder Structure

```text
LogBuddy/
|-- ControlPanel/
|   |-- pom.xml
|   `-- src/main/java/com/logbuddy/control/panel/
|       |-- config/
|       `-- controller/
|-- DataProcessing/
|   |-- pom.xml
|   |-- Dockerfile
|   |-- src/main/java/com/alexander/processing/
|   |   |-- controller/
|   |   |-- data/
|   |   |   |-- config/
|   |   |   |-- model/
|   |   |   `-- service/
|   |   |-- error/
|   |   |-- settings/
|   |   `-- util/
|   |-- src/main/proto/
|   `-- src/main/resources/
|-- SparkProcessing/
|   |-- pom.xml
|   |-- src/main/java/com/alexander/spark/
|   |   |-- controlpanel/
|   |   |-- ds/
|   |   |-- grpc/
|   |   |-- job/
|   |   |-- log/
|   |   |-- settings/
|   |   `-- util/
|   |-- src/main/proto/
|   `-- src/main/resources/
`-- logs/
```

### Directory layout explained

- `ControlPanel/`: intended operator-facing proxy service
- `DataProcessing/`: Spring Boot rule engine and alert sender
- `SparkProcessing/`: Spark ingestion and parsing engine
- `logs/`: runtime log outputs already present in the repository

## Future Improvements

- Make all ports and config paths environment-driven.
- Add Docker support for all three services, not just one partial Dockerfile.
- Add `docker-compose.yml` or Kubernetes manifests for local orchestration.
- Add example webhook receiver for testing alerts.
- Add retry and dead-letter behavior for failed alert deliveries.
- Add metrics and tracing for Spark ingestion, gRPC throughput, and rule matches.
- Clarify configuration schema with JSON Schema or YAML examples.
- Persist alert and session state if this system needs crash recovery.
- Standardize API naming:
  - `reload-config` vs `reload-settings`
  - `terminate-query` vs `stop-query`
  - `control-plane` vs `control-panel`
