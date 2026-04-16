from core.exceptions import AgentResponseError, OllamaConnectionError
from models.request_models import LogAnalysisRequest
from models.response_models import LogAnalysisResponse
from services.ai_agent_service import LogAnalysisAgent
from services.log_filter_service import LogFilterService


class AnalysisService:
    def __init__(
        self,
        log_filter_service: LogFilterService | None = None,
        agent: LogAnalysisAgent | None = None,
    ) -> None:
        self.log_filter_service = log_filter_service or LogFilterService()
        self.agent = agent or LogAnalysisAgent()

    def analyze(self, request: LogAnalysisRequest) -> LogAnalysisResponse:
        filtered_logs = self.log_filter_service.extract_and_filter(
            request.alerts,
            request.client_endpoints,
        )

        if not request.ai_analysis_enabled:
            return LogAnalysisResponse(
                ai_analysis_enabled=False,
                matched_logs_count=len(filtered_logs),
                matched_logs=filtered_logs,
                analysis=None,
                message="AI analysis disabled by request.",
            )

        if not filtered_logs:
            return LogAnalysisResponse(
                ai_analysis_enabled=True,
                matched_logs_count=0,
                matched_logs=[],
                analysis=None,
                message="No logs matched the provided client endpoints.",
            )

        try:
            analysis = self.agent.analyze_logs(filtered_logs)
            return LogAnalysisResponse(
                ai_analysis_enabled=True,
                matched_logs_count=len(filtered_logs),
                matched_logs=filtered_logs,
                analysis=analysis,
                message="Analysis completed successfully.",
            )
        except (OllamaConnectionError, AgentResponseError) as exc:
            return LogAnalysisResponse(
                ai_analysis_enabled=True,
                matched_logs_count=len(filtered_logs),
                matched_logs=filtered_logs,
                analysis=None,
                message=str(exc),
            )
