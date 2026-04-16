import re


HTTP_PATH_PATTERN = re.compile(
    r"\b(?:GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\s+([/\w\-\.{}]+(?:\?[^ ]*)?)",
    re.IGNORECASE,
)


def normalize_endpoint(value: str) -> str:
    normalized = value.strip()
    if not normalized:
        return ""
    normalized = normalized.split("?", maxsplit=1)[0]
    return normalized.rstrip("/").lower() or "/"


def extract_path_from_log(log_line: str) -> str | None:
    match = HTTP_PATH_PATTERN.search(log_line)
    if not match:
        return None
    return normalize_endpoint(match.group(1))


def log_matches_endpoints(log_line: str, client_endpoints: list[str]) -> bool:
    normalized_endpoints = [normalize_endpoint(endpoint) for endpoint in client_endpoints if endpoint.strip()]
    if not normalized_endpoints:
        return False

    extracted_path = extract_path_from_log(log_line)
    if extracted_path:
        return any(
            extracted_path == endpoint or extracted_path.startswith(f"{endpoint}/")
            for endpoint in normalized_endpoints
        )

    lowered_log = log_line.lower()
    return any(endpoint in lowered_log for endpoint in normalized_endpoints)
