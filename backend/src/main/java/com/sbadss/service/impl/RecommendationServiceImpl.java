package com.sbadss.service.impl;

import com.sbadss.entity.Customer;
import com.sbadss.exception.ResourceNotFoundException;
import com.sbadss.repository.CustomerRepository;
import com.sbadss.repository.SaleRepository;
import com.sbadss.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final RestTemplate restTemplate;

    @Value("${sbadss.ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getChurnPrediction(Long customerId, Long branchId) {
        log.info("Fetching churn prediction for customer: {}", customerId);
        
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        // Prepare data for AI model
        long daysSinceLastPurchase = 0;
        if (customer.getSales() != null && !customer.getSales().isEmpty()) {
            LocalDateTime lastSale = customer.getSales().get(0).getCreatedAt();
            daysSinceLastPurchase = ChronoUnit.DAYS.between(lastSale, LocalDateTime.now());
        }

        Map<String, Object> payload = Map.of(
                "customer_id", customerId,
                "branch_id", branchId,
                "days_since_last_purchase", daysSinceLastPurchase,
                "total_purchases", customer.getSales() != null ? customer.getSales().size() : 0,
                "average_purchase_value", 1500.0 // Placeholder logic
        );

        try {
            String url = aiServiceUrl + "/api/v1/churn/predict";
            return (Map<String, Object>) restTemplate.postForObject(url, payload, Map.class);
        } catch (Exception e) {
            log.warn("AI service churn prediction failed: {}", e.getMessage());
            return Map.of("error", "AI service unavailable", "status", "fallback");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getBusinessRecommendations(Long branchId) {
        log.info("Fetching business recommendations for branch: {}", branchId);
        
        try {
            String url = aiServiceUrl + "/api/v1/chatbot/query";
            Map<String, Object> payload = Map.of(
                    "message", "Suggest strategies to improve sales for branch " + branchId,
                    "branch_id", branchId
            );
            Map<String, Object> response = (Map<String, Object>) restTemplate.postForObject(url, payload, Map.class);
            
            if (response != null && response.containsKey("suggested_prompts")) {
                return (List<String>) response.get("suggested_prompts");
            }
        } catch (Exception e) {
            log.warn("AI service recommendations failed: {}", e.getMessage());
        }
        
        return List.of(
                "Focus on top-selling categories this month",
                "Review expense distribution for high-cost departments",
                "Consider a loyalty program for returning customers"
        );
    }
}
