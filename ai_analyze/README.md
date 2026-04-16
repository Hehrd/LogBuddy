# AI Log Analysis Service

FastAPI microservice for endpoint-filtered AI log analysis using a local Ollama model.

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

## Sample curl

```bash
curl -X POST "http://localhost:8000/api/v1/log-analysis" \
  -H "Content-Type: application/json" \
  -d '{
    "alerts": [
      {
        "timestamp": "2026-04-16T10:00:00Z",
        "data": [
          {
            "ruleName": "SQL_INJECTION_RULE",
            "timestamp": "2026-04-16T09:59:58Z",
            "logs": [
              "2026-04-16 09:59:58 WARN GET /api/auth/login 500 suspicious payload '\'' OR 1=1 --",
              "2026-04-16 09:59:59 INFO GET /api/health 200"
            ]
          }
        ]
      }
    ],
    "client_endpoints": ["/api/auth/login"],
    "ai_analysis_enabled": true
  }'
```

## Sample response

```json
{
  "ai_analysis_enabled": true,
  "matched_logs_count": 1,
  "matched_logs": [
    "2026-04-16 09:59:58 WARN GET /api/auth/login 500 suspicious payload ' OR 1=1 --"
  ],
  "analysis": {
    "suspicious": true,
    "severity": "high",
    "attack_type": "sql_injection",
    "summary": "Potential SQL injection attempt detected in filtered logs.",
    "evidence": [
      "suspicious payload ' OR 1=1 --",
      "request path /api/auth/login"
    ],
    "recommended_action": "Review the source IP, validate input sanitization, and inspect related authentication requests.",
    "confidence": 0.92
  },
  "message": "Analysis completed successfully."
}
```
