from core.exceptions import AgentResponseError, OllamaConnectionError
from models.request_models import LogAnalysisRequest
from models.response_models import EnrichedAlert, LogAnalysisResponse
from services.ai_agent_service import LogAnalysisAgent
from services.endpoint_delivery_service import EndpointDeliveryService


class AnalysisService:
    def __init__(
        self,
        agent: LogAnalysisAgent | None = None,
        delivery_service: EndpointDeliveryService | None = None,
    ) -> None:
        self.agent = agent or LogAnalysisAgent()
        self.delivery_service = delivery_service or EndpointDeliveryService()

    def analyze(self, request: LogAnalysisRequest) -> LogAnalysisResponse:
        enriched_alert = EnrichedAlert(**request.alert.model_dump(), aiOverview=None)

        if not request.alert.aiOverviewEnabled:
            return LogAnalysisResponse(
                alert=enriched_alert,
                deliveredEndpoints=[],
                failedDeliveries=[],
                message="AI overview disabled by alert.",
            )

        try:
            ai_overview = self.agent.analyze_alert(request.alert)
        except (OllamaConnectionError, AgentResponseError) as exc:
            return LogAnalysisResponse(
                alert=enriched_alert,
                deliveredEndpoints=[],
                failedDeliveries=[],
                message=f"AI overview generation failed: {exc}",
            )

        enriched_alert.aiOverview = ai_overview
        delivered_endpoints, failed_deliveries = self.delivery_service.deliver(
            ai_overview,
            request.endpoints,
        )

        if failed_deliveries:
            message = "AI overview generated, but some webhook deliveries failed."
        elif delivered_endpoints:
            message = "AI overview generated and delivered successfully."
        else:
            message = "AI overview generated successfully."

        return LogAnalysisResponse(
            alert=enriched_alert,
            deliveredEndpoints=delivered_endpoints,
            failedDeliveries=failed_deliveries,
            message=message,
        )

    def get_ollama_health(self) -> dict[str, object]:
        return self.agent.health_check()
