from datetime import datetime

from pydantic import BaseModel, Field


class RuleCompletion(BaseModel):
    ruleName: str = Field(..., min_length=1)
    timestamp: datetime
    logs: list[str] = Field(default_factory=list)


class Alert(BaseModel):
    alertId: str = Field(..., min_length=1)
    alertName: str = Field(..., min_length=1)
    alertType: str = Field(..., min_length=1)
    dataSourceName: str = Field(..., min_length=1)
    traceId: str | None = None
    triggeredAt: datetime
    firstMatchedAt: datetime
    lastMatchedAt: datetime
    timeWindowMillis: int = Field(..., ge=0)
    requiredRules: list[str] = Field(default_factory=list)
    completions: list[RuleCompletion] = Field(default_factory=list)
    sampleLogs: list[str] = Field(default_factory=list)
    aiOverviewEnabled: bool


class LogAnalysisRequest(BaseModel):
    alert: Alert
    endpoints: list[str] = Field(default_factory=list)

    model_config = {
        "json_schema_extra": {
            "example": {
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
                    "aiOverviewEnabled": True
                },
                "endpoints": [
                    "http://127.0.0.1:9001/hook"
                ]
            }
        }
    }
