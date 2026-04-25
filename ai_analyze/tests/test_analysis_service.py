from core.exceptions import OllamaConnectionError
from models.request_models import Alert, LogAnalysisRequest
from models.response_models import AiOverview, DeliveryFailure
from services.analysis_service import AnalysisService


class StubAgent:
    def __init__(self, result: AiOverview | None = None, error: Exception | None = None) -> None:
        self.result = result
        self.error = error
        self.calls = 0

    def analyze_alert(self, alert: Alert) -> AiOverview:
        self.calls += 1
        if self.error is not None:
            raise self.error
        assert self.result is not None
        return self.result

    def health_check(self) -> dict[str, object]:
        return {"status": "ok"}


class StubDeliveryService:
    def __init__(
        self,
        delivered: list[str] | None = None,
        failures: list[DeliveryFailure] | None = None,
    ) -> None:
        self.delivered = delivered or []
        self.failures = failures or []
        self.calls = 0
        self.last_payload: AiOverview | None = None
        self.last_endpoints: list[str] | None = None

    def deliver(self, ai_overview: AiOverview, endpoints: list[str]) -> tuple[list[str], list[DeliveryFailure]]:
        self.calls += 1
        self.last_payload = ai_overview
        self.last_endpoints = endpoints
        return self.delivered, self.failures


def build_request(ai_enabled: bool = True, endpoints: list[str] | None = None) -> LogAnalysisRequest:
    return LogAnalysisRequest(
        alert=Alert(
            alertId="alert-8f3c2a11",
            alertName="Multiple Failed Logins Detected",
            alertType="ALL_RULES_MATCHED",
            dataSourceName="auth-service-prod",
            traceId="trace-91ab47de",
            triggeredAt="2026-04-25T15:10:45Z",
            firstMatchedAt="2026-04-25T15:08:10Z",
            lastMatchedAt="2026-04-25T15:10:40Z",
            timeWindowMillis=300000,
            requiredRules=["FailedLoginThreshold", "RepeatedIpAttempts"],
            completions=[
                {
                    "ruleName": "FailedLoginThreshold",
                    "timestamp": "2026-04-25T15:09:30Z",
                    "logs": [
                        "User admin failed login from 192.168.1.15",
                        "User admin failed login from 192.168.1.15",
                    ],
                }
            ],
            sampleLogs=[
                "2026-04-25T15:08:10Z WARN auth-service Failed login for admin from 192.168.1.15",
            ],
            aiOverviewEnabled=ai_enabled,
        ),
        endpoints=endpoints or [],
    )


def build_ai_overview() -> AiOverview:
    return AiOverview(
        suspicious=True,
        severity="high",
        attack_type="credential_stuffing",
        summary="Repeated failed logins from the same IP indicate a likely brute-force attempt.",
        evidence=[
            "Multiple failed logins for admin from 192.168.1.15",
            "Same IP attempted logins for multiple usernames",
        ],
        recommended_action="Block the source IP and review authentication hardening controls.",
        confidence=0.94,
    )


def test_analyze_skips_ai_and_delivery_when_disabled() -> None:
    agent = StubAgent(result=build_ai_overview())
    delivery_service = StubDeliveryService()
    service = AnalysisService(agent=agent, delivery_service=delivery_service)

    response = service.analyze(build_request(ai_enabled=False, endpoints=["https://a.example/hook"]))

    assert response.alert.aiOverview is None
    assert response.message == "AI overview disabled by alert."
    assert agent.calls == 0
    assert delivery_service.calls == 0


def test_analyze_enriches_alert_and_delivers_to_endpoints() -> None:
    ai_overview = build_ai_overview()
    agent = StubAgent(result=ai_overview)
    delivery_service = StubDeliveryService(delivered=["https://a.example/hook", "https://b.example/hook"])
    service = AnalysisService(agent=agent, delivery_service=delivery_service)

    response = service.analyze(
        build_request(endpoints=["https://a.example/hook", "https://b.example/hook"])
    )

    assert response.alert.aiOverview == ai_overview
    assert response.deliveredEndpoints == ["https://a.example/hook", "https://b.example/hook"]
    assert response.failedDeliveries == []
    assert response.message == "AI overview generated and delivered successfully."
    assert delivery_service.calls == 1
    assert delivery_service.last_payload == ai_overview


def test_analyze_returns_agent_error_without_delivery() -> None:
    agent = StubAgent(
        error=OllamaConnectionError("Could not connect to Ollama at http://127.0.0.1:11434/api/generate.")
    )
    delivery_service = StubDeliveryService()
    service = AnalysisService(agent=agent, delivery_service=delivery_service)

    response = service.analyze(build_request(endpoints=["https://a.example/hook"]))

    assert response.alert.aiOverview is None
    assert response.deliveredEndpoints == []
    assert response.failedDeliveries == []
    assert (
        response.message
        == "AI overview generation failed: Could not connect to Ollama at http://127.0.0.1:11434/api/generate."
    )
    assert delivery_service.calls == 0


def test_analyze_keeps_ai_overview_when_delivery_fails() -> None:
    ai_overview = build_ai_overview()
    agent = StubAgent(result=ai_overview)
    delivery_service = StubDeliveryService(
        failures=[
            DeliveryFailure(
                endpoint="http://127.0.0.1:9001/hook",
                error="Webhook returned status 501. Response body: Unsupported method ('POST')",
            )
        ]
    )
    service = AnalysisService(agent=agent, delivery_service=delivery_service)

    response = service.analyze(build_request(endpoints=["http://127.0.0.1:9001/hook"]))

    assert response.alert.aiOverview == ai_overview
    assert response.deliveredEndpoints == []
    assert len(response.failedDeliveries) == 1
    assert response.failedDeliveries[0].endpoint == "http://127.0.0.1:9001/hook"
    assert response.message == "AI overview generated, but some webhook deliveries failed."
