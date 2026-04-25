import httpx

from models.response_models import AiOverview, DeliveryFailure


class EndpointDeliveryService:
    def __init__(self, timeout_seconds: float = 10.0) -> None:
        self.timeout_seconds = timeout_seconds

    def deliver(
        self,
        ai_overview: AiOverview,
        endpoints: list[str],
    ) -> tuple[list[str], list[DeliveryFailure]]:
        delivered: list[str] = []
        failures: list[DeliveryFailure] = []

        normalized_endpoints = [endpoint.strip() for endpoint in endpoints if endpoint.strip()]
        if not normalized_endpoints:
            return delivered, failures

        payload = ai_overview.model_dump(mode="json")
        with httpx.Client(timeout=self.timeout_seconds) as client:
            for endpoint in normalized_endpoints:
                try:
                    response = client.post(endpoint, json=payload)
                    response.raise_for_status()
                    delivered.append(endpoint)
                except httpx.TimeoutException as exc:
                    failures.append(
                        DeliveryFailure(
                            endpoint=endpoint,
                            error=f"Webhook request timed out after {self.timeout_seconds} seconds: {exc}",
                        )
                    )
                except httpx.HTTPStatusError as exc:
                    body_preview = exc.response.text.strip()
                    if len(body_preview) > 500:
                        body_preview = f"{body_preview[:500]}..."
                    failures.append(
                        DeliveryFailure(
                            endpoint=endpoint,
                            error=(
                                f"Webhook returned status {exc.response.status_code}. "
                                f"Response body: {body_preview}"
                            ),
                        )
                    )
                except httpx.ConnectError as exc:
                    failures.append(
                        DeliveryFailure(
                            endpoint=endpoint,
                            error=f"Could not connect to webhook endpoint: {exc}",
                        )
                    )
                except httpx.HTTPError as exc:
                    failures.append(
                        DeliveryFailure(
                            endpoint=endpoint,
                            error=f"Unexpected HTTP error during webhook delivery: {exc}",
                        )
                    )

        return delivered, failures
