import httpx

from core.exceptions import AgentResponseError
from models.request_models import Alert
from models.response_models import AiOverview
from services.ai_agent_service import LogAnalysisAgent


def build_alert() -> Alert:
    return Alert(
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
        aiOverviewEnabled=True,
    )


def make_response(payload: str) -> httpx.Response:
    request = httpx.Request("POST", "http://127.0.0.1:11434/api/generate")
    return httpx.Response(200, request=request, json={"response": payload})


def test_analyze_alert_parses_successful_ai_response() -> None:
    agent = LogAnalysisAgent()
    agent._post_json = lambda path, payload: make_response(  # type: ignore[method-assign]
        """
        {"suspicious": true, "severity": "high", "attack_type": "credential_stuffing", "summary": "Likely brute-force behavior.", "evidence": ["Repeated failed logins", "Multiple usernames targeted"], "recommended_action": "Block the IP and review auth controls.", "confidence": 0.95}
        """.strip()
    )

    result = agent.analyze_alert(build_alert())

    assert isinstance(result, AiOverview)
    assert result.suspicious is True
    assert result.severity == "high"
    assert result.attack_type == "credential_stuffing"


def test_analyze_alert_invalid_json_returns_clear_error() -> None:
    agent = LogAnalysisAgent()
    agent._post_json = lambda path, payload: make_response("not-json")  # type: ignore[method-assign]

    try:
        agent.analyze_alert(build_alert())
        raise AssertionError("Expected AgentResponseError to be raised.")
    except AgentResponseError as exc:
        assert "Ollama returned invalid JSON for analysis" in str(exc)
