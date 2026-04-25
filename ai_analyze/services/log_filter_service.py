from models.request_models import Alert
from utils.endpoint_matching import log_matches_endpoints


class LogFilterService:
    def extract_logs(self, alerts: list[Alert]) -> list[str]:
        logs: list[str] = []
        for alert in alerts:
            for completion in alert.completions:
                logs.extend(completion.logs)
            logs.extend(alert.sampleLogs)
        return logs

    def filter_logs(self, logs: list[str], client_endpoints: list[str]) -> list[str]:
        if not client_endpoints:
            return []
        return [log for log in logs if log_matches_endpoints(log, client_endpoints)]

    def extract_and_filter(self, alerts: list[Alert], client_endpoints: list[str]) -> list[str]:
        return self.filter_logs(self.extract_logs(alerts), client_endpoints)
