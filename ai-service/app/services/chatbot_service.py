import re
import uuid
from typing import List, Optional
import logging

logger = logging.getLogger(__name__)

# ─── NLP Intent Patterns ─────────────────────────────────────────────────────
INTENT_PATTERNS = {
    "REVENUE_QUERY":   [r"revenue", r"income", r"earnings", r"sales total", r"how much.*make"],
    "PROFIT_QUERY":    [r"profit", r"net profit", r"margin", r"gain"],
    "EXPENSE_QUERY":   [r"expense", r"cost", r"spending", r"expenditure"],
    "TOP_PRODUCTS":    [r"top product", r"best sell", r"most popular", r"which product"],
    "GROWTH_QUERY":    [r"growth", r"trend", r"increase", r"decrease", r"compar"],
    "CUSTOMER_QUERY":  [r"customer", r"client", r"buyer", r"active customer"],
    "RECOMMENDATION":  [r"recommend", r"suggest", r"improve", r"strategy", r"how can", r"what should"],
    "FORECAST_QUERY":  [r"forecast", r"predict", r"future", r"next month", r"next week"],
}

RESPONSES = {
    "REVENUE_QUERY":   "Your revenue data has been retrieved from the analytics engine. Check the KPI cards on your dashboard for real-time figures.",
    "PROFIT_QUERY":    "Net Profit = Total Revenue − Total Expenses. Your current profit metrics are shown on the KPI dashboard.",
    "EXPENSE_QUERY":   "Your expense breakdown by category is visualized in the Expense Overview pie chart on your dashboard.",
    "TOP_PRODUCTS":    "The Top Products bar chart on your dashboard shows the best-selling items by quantity and revenue.",
    "GROWTH_QUERY":    "Growth is calculated as: ((Current Period − Previous Period) / Previous Period) × 100. Check the Sales Trends chart.",
    "CUSTOMER_QUERY":  "Your active customer count and segmentation are available in the Customer Analytics section.",
    "RECOMMENDATION":  "📊 Recommendations to improve profitability:\n1. Focus marketing on your top 3 products\n2. Reduce costs in the highest expense category\n3. Target high-value customers with loyalty programs\n4. Analyze slow-moving inventory and run clearance",
    "FORECAST_QUERY":  "Use the AI Forecasting module to get a 7-90 day revenue prediction for your branch.",
    "UNKNOWN":         "I can help with revenue, profit, expenses, top products, growth trends, customers, and business recommendations. What would you like to know?",
}

SUGGESTED_PROMPTS = [
    "Show me this month's revenue",
    "What is our net profit?",
    "Which product is selling the most?",
    "Suggest strategies to improve sales",
    "Forecast next 30 days revenue",
    "Show customer analytics",
]


def process_message(message: str, session_id: Optional[str] = None) -> dict:
    """
    NLP pipeline:
    1. Tokenize and normalize input
    2. Detect intent via regex pattern matching
    3. Calculate confidence
    4. Return structured response
    """
    if not session_id:
        session_id = str(uuid.uuid4())

    normalized = message.lower().strip()
    logger.info(f"Processing message: '{normalized[:50]}...' for session: {session_id}")

    # Intent Detection
    detected_intent, confidence = _detect_intent(normalized)
    logger.info(f"Intent: {detected_intent}, Confidence: {confidence:.2f}")

    response_message = RESPONSES.get(detected_intent, RESPONSES["UNKNOWN"])
    suggestions = [p for p in SUGGESTED_PROMPTS if p.lower() not in normalized][:3]

    return {
        "session_id": session_id,
        "intent": detected_intent,
        "confidence": confidence,
        "message": response_message,
        "suggested_prompts": suggestions
    }


def _detect_intent(message: str) -> tuple[str, float]:
    best_intent = "UNKNOWN"
    best_score = 0.0

    for intent, patterns in INTENT_PATTERNS.items():
        score = 0
        for pattern in patterns:
            if re.search(pattern, message):
                score += 1.0 / len(patterns)
        if score > best_score:
            best_score = score
            best_intent = intent

    # Minimum threshold
    if best_score < 0.15:
        return "UNKNOWN", 0.0

    # Scale to 0.5 - 0.99
    confidence = min(0.50 + (best_score * 0.49), 0.99)
    return best_intent, round(confidence, 4)
