from fastapi import APIRouter
from app.schemas.schemas import ChatRequest, ChatResponse
from app.services import chatbot_service

router = APIRouter()


@router.post("/query", response_model=ChatResponse)
def process_query(request: ChatRequest):
    result = chatbot_service.process_message(request.message, request.session_id)
    return ChatResponse(**result)
