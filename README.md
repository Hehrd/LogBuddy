# LogBuddy

## Overview

LogBuddy is a log-ingestion and rule-based alerting system built around three backend services plus a separate frontend:

1. `SparkProcessing` reads logs from streaming or file-based sources with Apache Spark Structured Streaming.
2. `DataProcessing` receives parsed log batches over gRPC, evaluates configurable rules, and sends alerts to HTTP endpoints.
3. `ControlPanel` appears intended to be a small HTTP gateway for controlling the other services, but it is currently incomplete.
4. `Frontend` is a Vite + Vue application for dashboards, alerts, and config editing.

The project solves a common operational problem: collecting logs from multiple platforms, normalizing them into a common structure, checking them against detection rules, and triggering alerts when combinations of rule conditions are met.

At a high level:

- Spark handles ingestion and parsing.
- A Spring Boot service handles rule evaluation and alert dispatch.
- A lightweight control layer is intended to expose operational endpoints such as health, reload, sleep/wake, and query listing/stopping.
- A Vue frontend consumes live alerts over WebSocket and offers UI flows for config and monitoring.

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
- `DataProcessing/src/main/java/com/alexander/processing/context/ProcessingContext.java`
- `DataProcessing/src/main/java/com/alexander/processing/controlpanel/controller/ControlPanelController.java`
- `DataProcessing/src/main/java/com/alexander/processing/config/AppSettingsConfig.java`
- `DataProcessing/src/main/java/com/alexander/processing/service/ds/DataSourceIngestService.java`
- `DataProcessing/src/main/java/com/alexander/processing/service/ds/DataProcessingService.java`
- `DataProcessing/src/main/java/com/alexander/processing/service/rule/RuleProcessingService.java`
- `DataProcessing/src/main/java/com/alexander/processing/service/alert/AlertingService.java`

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

- `string_value_check`
- `numeric_value_check`
- `data_regex_match_check`
- `timestamp_check`
- `duplicate_event_check`
- `fields_change_check`

`fields_change_check` is trace-oriented. It now evaluates event pairs, not isolated field hits across the whole trace. A trace matches only when one compared pair satisfies all configured field transition rules together.

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
- `SparkProcessing/src/main/java/com/alexander/spark/context/RuntimeContext.java`
- `SparkProcessing/src/main/java/com/alexander/spark/controlpanel/controller/ControlPanelController.java`
- `SparkProcessing/src/main/java/com/alexander/spark/controlpanel/service/ControlPanelService.java`
- `SparkProcessing/src/main/java/com/alexander/spark/query/service/SparkService.java`
- `SparkProcessing/src/main/java/com/alexander/spark/query/service/QueryScheduler.java`
- `SparkProcessing/src/main/java/com/alexander/spark/query/service/GrpcWriter.java`
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
  "isInK8sMode": false,
  "grpcSettings": {
    "serverHost": "localhost",
    "serverPort": 9090,
    "maxLinesPerReq": 100
  },
  "sparkK8sSettings": null
}
```

Notes:

- `SparkProcessing` consumes `serverPort`, `grpcSettings`, `isInK8sMode`, and `sparkK8sSettings`.
- `DataProcessing` consumes `controlPanelServerPort` and `grpcSettings`.
- Both services disable Jackson's unknown-property failures, so a shared `app.conf` can contain the union of both models.

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
          "requestId": "TEXT",
          "statusCode": "NUMERIC",
          "retryCount": "NUMERIC"
        }
      },
      "globalRequiredRules": ["error_regex_rule"],
      "traceRequiredRules": ["user_status_transition_rule"],
      "globalAlertConditions": {
        "error-alert": {
          "alertName": "error-alert",
          "requiredRules": ["error_regex_rule"],
          "timeWindowMillis": 60000,
          "alertEndpoints": ["http://localhost:9000/webhook"],
          "alertConditionType": "GLOBAL",
          "aiOverviewEnabled": false
        }
      },
      "traceAlertConditions": {
        "trace-change-alert": {
          "alertName": "trace-change-alert",
          "requiredRules": ["user_status_transition_rule"],
          "timeWindowMillis": 60000,
          "alertEndpoints": ["http://localhost:9000/webhook"],
          "alertConditionType": "PER_LOG_TRACE",
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

Notes:

- `SparkProcessing` reads `pathInfo`.
- `DataProcessing` reads `path`.
- Because both services ignore unknown JSON properties, one shared `ds.conf` can carry both fields.
- `DataProcessing` expects `globalRequiredRules`, `traceRequiredRules`, `globalAlertConditions`, and `traceAlertConditions`.

#### Example `rule.conf`

```json
{
  "rules": {
    "error_regex_rule": {
      "ruleName": "error_regex_rule",
      "checks": [
        {
          "type": "data_regex_match_check",
          "fields": {
            "msg": {
              "matches": ".*ERROR.*",
              "notMatches": null
            }
          }
        }
      ],
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 1
    },
    "user_status_transition_rule": {
      "ruleName": "user_status_transition_rule",
      "checks": [
        {
          "type": "fields_change_check",
          "fields": {
            "userId": {
              "mode": "CHANGED",
              "previousCheck": {
                "type": "string_value_check",
                "values": {
                  "userId": {
                    "equalTo": "anonymous",
                    "notEqualTo": null,
                    "longerThan": 0,
                    "shorterThan": 0
                  }
                }
              },
              "currentCheck": {
                "type": "string_value_check",
                "values": {
                  "userId": {
                    "equalTo": null,
                    "notEqualTo": "anonymous",
                    "longerThan": 0,
                    "shorterThan": 0
                  }
                }
              }
            },
            "statusCode": {
              "mode": "CHANGED",
              "previousCheck": {
                "type": "numeric_value_check",
                "values": {
                  "statusCode": {
                    "lessThan": null,
                    "moreThan": null,
                    "equalTo": 401,
                    "notEqualTo": null,
                    "divisibleBy": null
                  }
                }
              },
              "currentCheck": {
                "type": "numeric_value_check",
                "values": {
                  "statusCode": {
                    "lessThan": null,
                    "moreThan": null,
                    "equalTo": 200,
                    "notEqualTo": null,
                    "divisibleBy": null
                  }
                }
              }
            }
          },
          "strategy": "COMPARE_TO_PREVIOUS_EVENT"
        }
      ],
      "logTargetCount": 1,
      "maxCompletionsPerAlert": 1
    }
  }
}
```

The `checks[].type` discriminator values defined in code are:

- `string_value_check`
- `numeric_value_check`
- `data_regex_match_check`
- `timestamp_check`
- `duplicate_event_check`
- `fields_change_check`

For `fields_change_check`, each configured field is evaluated against the same `(referenceEvent, currentEvent)` pair selected by `strategy`.

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

### Alert payload shape

`DataProcessing` currently emits alerts with this high-level structure:

```json
{
  "alertId": "uuid",
  "alertName": "trace-change-alert",
  "alertType": "PER_LOG_TRACE",
  "dataSourceName": "app-logs",
  "traceId": "trace-123",
  "triggeredAt": "2026-04-25T09:20:00Z",
  "firstMatchedAt": "2026-04-25T09:18:00Z",
  "lastMatchedAt": "2026-04-25T09:19:30Z",
  "timeWindowMillis": 60000,
  "requiredRules": ["user_status_transition_rule"],
  "completions": [
    {
      "ruleName": "user_status_transition_rule",
      "timestamp": "2026-04-25T09:19:30Z",
      "logs": ["..."]
    }
  ],
  "sampleLogs": ["..."],
  "aiOverviewEnabled": false
}
```

Consumers should prefer `completions` over the old `data` field name. Some frontend/mock code in the repo still assumes the older payload and should be aligned before treating the UI as production-ready.

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
- `DataProcessing` REST API likely runs on Spring Boot default port `8080` unless `server.port` is supplied externally.
- `SparkProcessing` runs its HTTP API on `app.conf.serverPort`.
- Config files are JSON, even though they use `.conf` extensions.

### Unclear or inconsistent parts

- `ControlPanel` is incomplete and currently not runnable as a real microservice.
- `ControlPanel` endpoint names do not match the endpoint names implemented by `SparkProcessing`.
- `SparkProcessing` controller path matching uses `getRequestURI().getHost()` in a way that may not behave as intended.
- `DataProcessing` has `controlPanelServerPort` in config, but its REST server port is not directly wired from that field in the visible code.
- `DataSource.path` exists in `DataProcessing` while `SparkProcessing` uses `pathInfo`; the current setup relies on both services ignoring unknown JSON properties so one file can contain both shapes.
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
|   |   |-- config/
|   |   |-- context/
|   |   |-- controlpanel/
|   |   |-- exception/
|   |   |-- model/
|   |   |-- service/
|   |   |-- settings/
|   |   `-- util/
|   |-- src/main/proto/
|   `-- src/main/resources/
|-- SparkProcessing/
|   |-- pom.xml
|   |-- src/main/java/com/alexander/spark/
|   |   |-- controlpanel/
|   |   |-- context/
|   |   |-- ds/
|   |   |-- exception/
|   |   |-- log/
|   |   |-- query/
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
