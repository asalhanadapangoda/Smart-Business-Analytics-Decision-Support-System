package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/churn/{customerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChurnPrediction(
            @PathVariable Long customerId, @RequestParam Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getChurnPrediction(customerId, branchId), "Churn prediction fetched"));
    }

    @GetMapping("/business")
    public ResponseEntity<ApiResponse<List<String>>> getBusinessRecommendations(@RequestParam Long branchId) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getBusinessRecommendations(branchId), "Recommendations fetched"));
    }
}
