from fastapi import APIRouter
from app.schemas.schemas import ChurnRequest, ChurnResponse

router = APIRouter()


@router.post("/predict", response_model=ChurnResponse)
def predict_churn(request: ChurnRequest):
    """
    Customer churn prediction using a simple rule-based scoring model.
    Phase 2: Replace with trained sklearn LogisticRegression model.
    """
    score = 0.0

    # Recency score
    if request.days_since_last_purchase > 90:
        score += 0.4
    elif request.days_since_last_purchase > 30:
        score += 0.2

    # Frequency score (fewer purchases = higher churn risk)
    if request.total_purchases < 3:
        score += 0.3
    elif request.total_purchases < 10:
        score += 0.1

    # Monetary score (low value = higher churn risk)
    if request.average_purchase_value < 500:
        score += 0.2
    elif request.average_purchase_value < 2000:
        score += 0.1

    churn_probability = min(score, 0.99)

    if churn_probability > 0.7:
        risk_level = "HIGH"
        recommendation = "Immediate action required. Offer personalized discount or loyalty rewards."
    elif churn_probability > 0.4:
        risk_level = "MEDIUM"
        recommendation = "Send re-engagement email with product recommendations based on past purchases."
    else:
        risk_level = "LOW"
        recommendation = "Customer is healthy. Continue standard engagement and loyalty programs."

    return ChurnResponse(
        customer_id=request.customer_id,
        churn_probability=round(churn_probability, 4),
        risk_level=risk_level,
        recommendation=recommendation
    )
