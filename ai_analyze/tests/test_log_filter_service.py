from services.log_filter_service import LogFilterService


def test_filter_logs_matches_extracted_http_path() -> None:
    service = LogFilterService()
    logs = [
        "2026-04-16 09:59:58 WARN GET /api/auth/login 500 suspicious payload",
        "2026-04-16 09:59:59 INFO GET /api/health 200",
    ]

    result = service.filter_logs(logs, ["/api/auth/login"])

    assert result == ["2026-04-16 09:59:58 WARN GET /api/auth/login 500 suspicious payload"]


def test_filter_logs_falls_back_to_substring_match() -> None:
    service = LogFilterService()
    logs = [
        "proxy trace path=/api/users/details status=403 denied",
        "proxy trace path=/public/info status=200",
    ]

    result = service.filter_logs(logs, ["/api/users"])

    assert result == ["proxy trace path=/api/users/details status=403 denied"]
