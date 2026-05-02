package com.sbadss.service.impl;

import com.sbadss.dto.ChatRequest;
import com.sbadss.dto.ChatResponse;
import com.sbadss.entity.ChatMessage;
import com.sbadss.entity.User;
import com.sbadss.exception.ResourceNotFoundException;
import com.sbadss.repository.ChatMessageRepository;
import com.sbadss.repository.UserRepository;
import com.sbadss.service.AnalyticsService;
import com.sbadss.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    // ─── NLP Intent patterns (Phase 1: Rule-based) ──────────────────────────
    private static final Map<String, String[]> INTENT_PATTERNS = Map.of(
        "REVENUE_QUERY",      new String[]{"revenue", "income", "earnings", "sales total", "how much did we make"},
        "PROFIT_QUERY",       new String[]{"profit", "net profit", "margin", "gain"},
        "EXPENSE_QUERY",      new String[]{"expense", "cost", "spending", "expenditure"},
        "TOP_PRODUCTS",       new String[]{"top product", "best selling", "most popular", "which product"},
        "GROWTH_QUERY",       new String[]{"growth", "trend", "increase", "decrease", "compared to last"},
        "CUSTOMER_QUERY",     new String[]{"customer", "client", "buyer", "active customer"},
        "RECOMMENDATION",     new String[]{"recommend", "suggest", "improve", "strategy", "how can", "what should"}
    );

    private static final Map<String, String> FALLBACK_RESPONSES = Map.of(
        "REVENUE_QUERY",   "Based on your current data, I can see your revenue metrics. Please check your dashboard for live figures.",
        "PROFIT_QUERY",    "Your net profit is calculated as total revenue minus total expenses. See the KPI cards on your dashboard.",
        "EXPENSE_QUERY",   "Your expense overview shows category-wise spending. The Expense Overview chart gives a full breakdown.",
        "TOP_PRODUCTS",    "Your top-selling products are shown in the Products bar chart on your dashboard.",
        "GROWTH_QUERY",    "Revenue growth is tracked month-over-month. Check the Sales Trends chart for the full view.",
        "CUSTOMER_QUERY",  "Your active customer count and customer segmentation are available on the Customers dashboard.",
        "RECOMMENDATION",  "To increase profitability: 1) Focus on top-selling products 2) Monitor expense categories 3) Target high-value customers with promotions."
    );

    @Override
    @Transactional
    public ChatResponse processMessage(ChatRequest request, Long userId) {
        log.info("Processing chatbot message from user: {}, session: {}", userId, request.getSessionId());
        
        String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : UUID.randomUUID().toString();
        
        // Step 1: NLP — Detect Intent
        String detectedIntent = detectIntent(request.getMessage());
        double confidence = calculateConfidence(request.getMessage(), detectedIntent);
        
        log.info("Detected intent: {} with confidence: {:.2f}", detectedIntent, confidence);
        
        // Step 2: Fetch relevant data based on intent
        Object data = null;
        if (detectedIntent != null && !detectedIntent.equals("UNKNOWN")) {
            data = fetchDataForIntent(detectedIntent, request.getBranchId());
        }

        // Step 3: Build response
        String responseMessage = buildResponse(detectedIntent, request.getMessage());
        
        // Step 4: Persist conversation
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            ChatMessage chatMsg = ChatMessage.builder()
                    .sessionId(sessionId)
                    .user(user)
                    .userMessage(request.getMessage())
                    .botResponse(responseMessage)
                    .intentDetected(detectedIntent)
                    .confidenceScore(confidence)
                    .build();
            chatMessageRepository.save(chatMsg);
        }

        return ChatResponse.builder()
                .sessionId(sessionId)
                .message(responseMessage)
                .intent(detectedIntent)
                .confidence(confidence)
                .data(data)
                .suggestedPrompts(getSuggestedPrompts(detectedIntent))
                .build();
    }

    private String detectIntent(String message) {
        if (message == null || message.isBlank()) return "UNKNOWN";
        String lowerMsg = message.toLowerCase();

        for (Map.Entry<String, String[]> entry : INTENT_PATTERNS.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (lowerMsg.contains(pattern)) {
                    return entry.getKey();
                }
            }
        }
        return "UNKNOWN";
    }

    private double calculateConfidence(String message, String intent) {
        if ("UNKNOWN".equals(intent)) return 0.0;
        String lowerMsg = message.toLowerCase();
        String[] patterns = INTENT_PATTERNS.get(intent);
        if (patterns == null) return 0.5;
        int matchCount = 0;
        for (String p : patterns) {
            if (lowerMsg.contains(p)) matchCount++;
        }
        return Math.min(0.5 + (matchCount * 0.2), 0.99);
    }

    private Object fetchDataForIntent(String intent, Long branchId) {
        try {
            return switch (intent) {
                case "REVENUE_QUERY", "PROFIT_QUERY", "EXPENSE_QUERY", "GROWTH_QUERY" ->
                        analyticsService.getDashboardData(branchId).getKpiMetrics();
                case "TOP_PRODUCTS" ->
                        analyticsService.getDashboardData(branchId).getTopProducts();
                default -> null;
            };
        } catch (Exception e) {
            log.warn("Failed to fetch data for intent {}: {}", intent, e.getMessage());
            return null;
        }
    }

    private String buildResponse(String intent, String userMessage) {
        if ("UNKNOWN".equals(intent)) {
            return "I understand you're asking about '" + userMessage + "'. I can help you with revenue, profit, expenses, top products, growth trends, customers, and business recommendations. What would you like to know?";
        }
        return FALLBACK_RESPONSES.getOrDefault(intent,
                "I found relevant information for your query. Please check the data above.");
    }

    private List<String> getSuggestedPrompts(String currentIntent) {
        List<String> prompts = new ArrayList<>();
        if (!"REVENUE_QUERY".equals(currentIntent))   prompts.add("Show me this month's revenue");
        if (!"PROFIT_QUERY".equals(currentIntent))    prompts.add("What is our net profit?");
        if (!"TOP_PRODUCTS".equals(currentIntent))    prompts.add("Which product is selling the most?");
        if (!"RECOMMENDATION".equals(currentIntent)) prompts.add("Suggest strategies to improve sales");
        return prompts.subList(0, Math.min(3, prompts.size()));
    }
}
