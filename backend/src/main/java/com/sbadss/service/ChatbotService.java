package com.sbadss.service;

import com.sbadss.dto.ChatRequest;
import com.sbadss.dto.ChatResponse;

public interface ChatbotService {
    ChatResponse processMessage(ChatRequest request, Long userId);
}
