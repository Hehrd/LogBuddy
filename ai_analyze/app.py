from fastapi import FastAPI, HTTPException

from core.exceptions import AgentResponseError, OllamaConnectionError
from models.request_models import LogAnalysisRequest
from models.response_models import LogAnalysisResponse
from services.analysis_service import AnalysisService


app = FastAPI(
    title="AI Log Analysis Service",
    version="0.1.0",
    description="FastAPI microservice for AI alert enrichment via Ollama.",
)

analysis_service = AnalysisService()


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "ai-log-analysis-service",
    }


@app.get("/health/ollama")
def health_ollama() -> dict[str, object]:
    try:
        return analysis_service.get_ollama_health()
    except (OllamaConnectionError, AgentResponseError) as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc


@app.post("/api/v1/log-analysis", response_model=LogAnalysisResponse)
def analyze_logs(request: LogAnalysisRequest) -> LogAnalysisResponse:
    try:
        return analysis_service.analyze(request)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        raise HTTPException(status_code=500, detail="Unexpected error during log analysis.") from exc
