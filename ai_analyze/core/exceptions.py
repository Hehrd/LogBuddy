class OllamaError(Exception):
    """Base exception for Ollama-related failures."""


class OllamaConnectionError(OllamaError):
    """Raised when Ollama cannot be reached or returns an unusable HTTP response."""


class AgentResponseError(OllamaError):
    """Raised when Ollama responds but the model output is invalid for this service."""
