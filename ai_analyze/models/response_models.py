from pydantic import BaseModel, Field

class AnalysisResult(BaseModel):
    suspicious: bool
    severity: str
    attack_type: str | None = None
    summary: str
    evidence: list[str] = Field(default_factory=list)
    recommended_action: str
    confidence: float = Field(..., ge=0.0, le=1.0)

class LogAnalysisResponse(BaseModel):
    ai_analysis_enabled: bool
    matched_logs_count: int = Field(..., ge=0)
    matched_logs: list[str] = Field(default_factory=list)
    analysis: AnalysisResult | None = None
    message: str
