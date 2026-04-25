# AI Log Analysis Service

FastAPI microservice for generating an AI overview for a single alert and forwarding that AI output to downstream endpoints.

## Run locally

1. Activate your virtual environment.
2. Install dependencies:

```bash
pip install -r requirements.txt
```

3. Start Ollama and pull a local model:

```bash
ollama serve
ollama pull llama3.1
```

4. Start the API:

```bash
uvicorn app:app --reload
```

## Endpoints

- `GET /health`
- `POST /api/v1/log-analysis`

## Request shape

`POST /api/v1/log-analysis` now accepts a single alert plus a list of delivery endpoints:

```json
{
  "alert": {
    "alertId": "alert-8f3c2a11",
    "alertName": "Multiple Failed Logins Detected",
    "alertType": "ALL_RULES_MATCHED",
    "dataSourceName": "auth-service-prod",
    "traceId": "trace-91ab47de",
    "triggeredAt": "2026-04-25T15:10:45Z",
    "firstMatchedAt": "2026-04-25T15:08:10Z",
    "lastMatchedAt": "2026-04-25T15:10:40Z",
    "timeWindowMillis": 300000,
    "requiredRules": [
      "FailedLoginThreshold",
      "RepeatedIpAttempts"
    ],
    "completions": [
      {
        "ruleName": "FailedLoginThreshold",
        "timestamp": "2026-04-25T15:09:30Z",
        "logs": [
          "User admin failed login from 192.168.1.15",
          "User admin failed login from 192.168.1.15",
          "User admin failed login from 192.168.1.15"
        ]
      },
      {
        "ruleName": "RepeatedIpAttempts",
        "timestamp": "2026-04-25T15:10:40Z",
        "logs": [
          "IP 192.168.1.15 attempted login for user admin",
          "IP 192.168.1.15 attempted login for user root"
        ]
      }
    ],
    "sampleLogs": [
      "2026-04-25T15:08:10Z WARN auth-service Failed login for admin from 192.168.1.15",
      "2026-04-25T15:09:30Z WARN auth-service Failed login for admin from 192.168.1.15",
      "2026-04-25T15:10:40Z WARN auth-service Failed login for root from 192.168.1.15"
    ],
    "aiOverviewEnabled": true
  },
  "endpoints": [
    "https://example.com/incident-hook",
    "https://example.com/archive-hook"
  ]
}
```

## Response shape

The service returns the original alert enriched with `aiOverview`. When AI is enabled and generation succeeds, the same `aiOverview` payload is POSTed to every endpoint in `endpoints`.

```json
{
  "alert": {
    "alertId": "alert-8f3c2a11",
    "alertName": "Multiple Failed Logins Detected",
    "alertType": "ALL_RULES_MATCHED",
    "dataSourceName": "auth-service-prod",
    "traceId": "trace-91ab47de",
    "triggeredAt": "2026-04-25T15:10:45Z",
    "firstMatchedAt": "2026-04-25T15:08:10Z",
    "lastMatchedAt": "2026-04-25T15:10:40Z",
    "timeWindowMillis": 300000,
    "requiredRules": [
      "FailedLoginThreshold",
      "RepeatedIpAttempts"
    ],
    "completions": [
      {
        "ruleName": "FailedLoginThreshold",
        "timestamp": "2026-04-25T15:09:30Z",
        "logs": [
          "User admin failed login from 192.168.1.15",
          "User admin failed login from 192.168.1.15",
          "User admin failed login from 192.168.1.15"
        ]
      }
    ],
    "sampleLogs": [
      "2026-04-25T15:08:10Z WARN auth-service Failed login for admin from 192.168.1.15"
    ],
    "aiOverviewEnabled": true,
    "aiOverview": {
      "suspicious": true,
      "severity": "high",
      "attack_type": "credential_stuffing",
      "summary": "Repeated failed logins from one IP suggest a likely brute-force attempt.",
      "evidence": [
        "Multiple failed logins for admin from 192.168.1.15",
        "Same IP attempted access across multiple usernames"
      ],
      "recommended_action": "Block the source IP and review authentication hardening controls.",
      "confidence": 0.94
    }
  },
  "deliveredEndpoints": [
    "https://example.com/incident-hook",
    "https://example.com/archive-hook"
  ],
  "failedDeliveries": [],
  "message": "AI overview generated and delivered successfully."
}
```
