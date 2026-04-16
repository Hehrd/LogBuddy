import json

import httpx

from core.config import settings
from core.exceptions import AgentResponseError, OllamaConnectionError
from models.response_models import AnalysisResult


class LogAnalysisAgent:
    def __init__(
        self,
        base_url: str = settings.ollama_base_url,
        model: str = settings.ollama_model,
        timeout_seconds: float = settings.ollama_timeout_seconds,
    ) -> None:
        self.base_url = base_url.rstrip("/")
        self.model = model
        self.timeout_seconds = timeout_seconds

    def analyze_logs(self, logs: list[str]) -> AnalysisResult:
        payload = {
            "model": self.model,
            "prompt": self._build_prompt(logs),
            "stream": False,
            "format": "json",
        }

        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.post(f"{self.base_url}/api/generate", json=payload)
                response.raise_for_status()
        except httpx.HTTPError as exc:
            raise OllamaConnectionError("Failed to call Ollama for log analysis.") from exc

        data = response.json()
        raw_response = data.get("response", "")
        if not raw_response:
            raise AgentResponseError("Ollama returned an empty analysis response.")

        try:
            parsed = json.loads(raw_response)
            return AnalysisResult.model_validate(parsed)
        except (json.JSONDecodeError, ValueError) as exc:
            raise AgentResponseError("Ollama returned invalid JSON for analysis.") from exc

    def _build_prompt(self, logs: list[str]) -> str:
        return (
            "You are a security log analysis agent. "
            "Analyze only the provided log lines. "
            "Do not infer meaning from external systems or full alert objects. "
            "Return strict JSON with exactly these fields: "
            "suspicious (boolean), severity (string), attack_type (string or null), "
            "summary (string), evidence (array of strings), recommended_action (string), "
            "confidence (number between 0 and 1).\n\n"
            "Log lines:\n"
            f"{chr(10).join(logs)}"
        )
