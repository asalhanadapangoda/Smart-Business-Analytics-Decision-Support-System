package com.sbadss.service;

import java.util.List;
import java.util.Map;

public interface RecommendationService {
    Map<String, Object> getChurnPrediction(Long customerId, Long branchId);
    List<String> getBusinessRecommendations(Long branchId);
}
