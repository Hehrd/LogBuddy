from datetime import datetime
from pydantic import BaseModel, Field

class RuleCompletion(BaseModel):
    ruleName: str = Field(..., min_length=1)
    timestamp: datetime
    logs: list[str] = Field(default_factory=list)


class Alert(BaseModel):
    data: list[RuleCompletion] = Field(default_factory=list)
    timestamp: datetime


class LogAnalysisRequest(BaseModel):
    alerts: list[Alert] = Field(default_factory=list)
    client_endpoints: list[str] = Field(default_factory=list)
    ai_analysis_enabled: bool
