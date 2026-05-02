from fastapi import APIRouter
from app.schemas.schemas import ForecastRequest, ForecastResponse
from app.services import forecast_service

router = APIRouter()


@router.post("/sales", response_model=ForecastResponse)
def forecast_sales(request: ForecastRequest):
    historical = [{"date": d.date, "revenue": d.revenue} for d in request.historical_data]
    result = forecast_service.generate_forecast(historical, request.horizon_days)

    return ForecastResponse(
        branch_id=request.branch_id,
        horizon_days=request.horizon_days,
        model_used=result["model_used"],
        confidence=result["confidence"],
        predictions=result["predictions"],
        recommendation=result["recommendation"]
    )
