from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import date


class HistoricalDataPoint(BaseModel):
    date: str
    revenue: float


class ForecastRequest(BaseModel):
    branch_id: int
    horizon_days: int = Field(default=30, ge=1, le=365)
    historical_data: List[HistoricalDataPoint]


class ForecastDataPoint(BaseModel):
    date: str
    value: float
    lower_bound: Optional[float] = None
    upper_bound: Optional[float] = None


class ForecastResponse(BaseModel):
    branch_id: int
    horizon_days: int
    model_used: str
    confidence: float
    predictions: List[ForecastDataPoint]
    recommendation: str


class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None
    branch_id: Optional[int] = None


class ChatResponse(BaseModel):
    session_id: str
    intent: str
    confidence: float
    message: str
    suggested_prompts: List[str] = []


class ChurnRequest(BaseModel):
    branch_id: int
    customer_id: int
    days_since_last_purchase: int
    total_purchases: int
    average_purchase_value: float


class ChurnResponse(BaseModel):
    customer_id: int
    churn_probability: float
    risk_level: str
    recommendation: str
