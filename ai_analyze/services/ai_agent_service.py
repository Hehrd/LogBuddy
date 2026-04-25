import json

import httpx

from core.config import settings
from core.exceptions import AgentResponseError, OllamaConnectionError
from models.request_models import Alert
from models.response_models import AiOverview


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

    def analyze_alert(self, alert: Alert) -> AiOverview:
        payload = {
            "model": self.model,
            "prompt": self._build_prompt(alert),
            "stream": False,
            "format": "json",
        }

        response = self._post_json("/api/generate", payload)
        data = self._parse_response_json(response, "Ollama /api/generate returned invalid JSON")
        raw_response = data.get("response")

        if not isinstance(raw_response, str):
            raise AgentResponseError(
                "Ollama response is missing the 'response' field or it is not a string."
            )

        raw_response = raw_response.strip()
        if not raw_response:
            raise AgentResponseError("Ollama returned an empty analysis response.")

        try:
            parsed = json.loads(raw_response)
        except json.JSONDecodeError as exc:
            raise AgentResponseError(
                f"Ollama returned invalid JSON for analysis: {exc.msg} at line {exc.lineno}, column {exc.colno}."
            ) from exc

        try:
            return AiOverview.model_validate(parsed)
        except ValueError as exc:
            raise AgentResponseError(f"Ollama analysis did not match AiOverview schema: {exc}") from exc

    def health_check(self) -> dict[str, object]:
        response = self._get("/api/tags")
        data = self._parse_response_json(response, "Ollama /api/tags returned invalid JSON")

        models = data.get("models")
        if not isinstance(models, list):
            raise AgentResponseError("Ollama /api/tags response is missing the 'models' list.")

        available_models = [
            model_info.get("name")
            for model_info in models
            if isinstance(model_info, dict) and isinstance(model_info.get("name"), str)
        ]

        configured_model_names = {self.model, f"{self.model}:latest"}
        model_available = any(name in configured_model_names for name in available_models)
        if not model_available:
            raise AgentResponseError(
                f"Configured Ollama model '{self.model}' is not available. Available models: {available_models}."
            )

        return {
            "status": "ok",
            "baseUrl": self.base_url,
            "model": self.model,
            "reachable": True,
            "modelAvailable": True,
            "availableModels": available_models,
        }

    def _build_prompt(self, alert: Alert) -> str:
        return (
            "You are a security log analysis agent. "
            "Analyze only the provided alert payload. "
            "Use alert metadata, rule completions, and sample logs as evidence. "
            "Return only valid JSON. "
            "Do not include markdown. "
            "Do not include explanations. "
            "Do not include code fences. "
            "The JSON object must contain exactly these fields: "
            "suspicious (boolean), severity (string), attack_type (string or null), "
            "summary (string), evidence (array of strings), recommended_action (string), "
            "confidence (number between 0 and 1).\n\n"
            "Alert payload:\n"
            f"{json.dumps(alert.model_dump(mode='json'), indent=2)}"
        )

    def _get(self, path: str) -> httpx.Response:
        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.get(f"{self.base_url}{path}")
                response.raise_for_status()
                return response
        except httpx.TimeoutException as exc:
            raise OllamaConnectionError(
                f"Ollama request timed out after {self.timeout_seconds} seconds while calling {self.base_url}{path}."
            ) from exc
        except httpx.HTTPStatusError as exc:
            raise OllamaConnectionError(
                self._format_status_error("Ollama request failed", exc)
            ) from exc
        except httpx.ConnectError as exc:
            raise OllamaConnectionError(
                f"Could not connect to Ollama at {self.base_url}{path}: {exc}."
            ) from exc
        except httpx.HTTPError as exc:
            raise OllamaConnectionError(
                f"Unexpected HTTP error while calling Ollama at {self.base_url}{path}: {exc}."
            ) from exc

    def _post_json(self, path: str, payload: dict[str, object]) -> httpx.Response:
        try:
            with httpx.Client(timeout=self.timeout_seconds) as client:
                response = client.post(f"{self.base_url}{path}", json=payload)
                response.raise_for_status()
                return response
        except httpx.TimeoutException as exc:
            raise OllamaConnectionError(
                f"Ollama request timed out after {self.timeout_seconds} seconds while calling {self.base_url}{path}."
            ) from exc
        except httpx.HTTPStatusError as exc:
            raise OllamaConnectionError(
                self._format_status_error("Ollama request failed", exc)
            ) from exc
        except httpx.ConnectError as exc:
            raise OllamaConnectionError(
                f"Could not connect to Ollama at {self.base_url}{path}: {exc}."
            ) from exc
        except httpx.HTTPError as exc:
            raise OllamaConnectionError(
                f"Unexpected HTTP error while calling Ollama at {self.base_url}{path}: {exc}."
            ) from exc

    @staticmethod
    def _parse_response_json(response: httpx.Response, error_prefix: str) -> dict[str, object]:
        try:
            parsed = response.json()
        except json.JSONDecodeError as exc:
            body_preview = response.text.strip()
            if len(body_preview) > 500:
                body_preview = f"{body_preview[:500]}..."
            raise AgentResponseError(
                f"{error_prefix}: {exc.msg} at line {exc.lineno}, column {exc.colno}. Body: {body_preview}"
            ) from exc

        if not isinstance(parsed, dict):
            raise AgentResponseError(f"{error_prefix}: expected a JSON object, got {type(parsed).__name__}.")
        return parsed

    @staticmethod
    def _format_status_error(prefix: str, exc: httpx.HTTPStatusError) -> str:
        response = exc.response
        body_preview = response.text.strip()
        if len(body_preview) > 500:
            body_preview = f"{body_preview[:500]}..."
        return (
            f"{prefix}: status {response.status_code} from {response.request.method} "
            f"{response.request.url}. Response body: {body_preview}"
        )
