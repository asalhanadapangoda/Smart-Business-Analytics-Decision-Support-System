from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import forecast, chatbot, churn

app = FastAPI(
    title="SBADSS AI Microservice",
    description="Python AI/ML service for sales forecasting, NLP chatbot, and churn prediction",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(forecast.router, prefix="/api/v1/forecast", tags=["Forecasting"])
app.include_router(chatbot.router, prefix="/api/v1/chatbot", tags=["Chatbot"])
app.include_router(churn.router, prefix="/api/v1/churn", tags=["Churn Prediction"])


@app.get("/health")
def health_check():
    return {"status": "ok", "service": "SBADSS AI Microservice"}
