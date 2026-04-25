from datetime import datetime

from pydantic import BaseModel, Field


class AiOverview(BaseModel):
    suspicious: bool
    severity: str
    attack_type: str | None = None
    summary: str
    evidence: list[str] = Field(default_factory=list)
    recommended_action: str
    confidence: float = Field(..., ge=0.0, le=1.0)


class RuleCompletionResponse(BaseModel):
    ruleName: str
    timestamp: datetime
    logs: list[str] = Field(default_factory=list)


class EnrichedAlert(BaseModel):
    alertId: str
    alertName: str
    alertType: str
    dataSourceName: str
    traceId: str
    triggeredAt: datetime
    firstMatchedAt: datetime
    lastMatchedAt: datetime
    timeWindowMillis: int = Field(..., ge=0)
    requiredRules: list[str] = Field(default_factory=list)
    completions: list[RuleCompletionResponse] = Field(default_factory=list)
    sampleLogs: list[str] = Field(default_factory=list)
    aiOverviewEnabled: bool
    aiOverview: AiOverview | None = None


class DeliveryFailure(BaseModel):
    endpoint: str
    error: str


class LogAnalysisResponse(BaseModel):
    alert: EnrichedAlert
    deliveredEndpoints: list[str] = Field(default_factory=list)
    failedDeliveries: list[DeliveryFailure] = Field(default_factory=list)
    message: str

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
                    "aiOverviewEnabled": True,
                    "aiOverview": {
                        "suspicious": True,
                        "severity": "high",
                        "attack_type": "credential_stuffing",
                        "summary": "Repeated failed login attempts from one IP indicate a likely brute-force attempt.",
                        "evidence": [
                            "Multiple failed logins for admin from 192.168.1.15",
                            "The same IP attempted access for multiple usernames"
                        ],
                        "recommended_action": "Block the source IP and review authentication hardening controls.",
                        "confidence": 0.94
                    }
                },
                "deliveredEndpoints": [
                    "http://127.0.0.1:9001/hook"
                ],
                "failedDeliveries": [],
                "message": "AI overview generated and delivered successfully."
            }
        }
    }
