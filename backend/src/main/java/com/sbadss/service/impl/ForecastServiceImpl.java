package com.sbadss.service.impl;

import com.sbadss.dto.DataPointResponse;
import com.sbadss.dto.ForecastRequest;
import com.sbadss.dto.ForecastResponse;
import com.sbadss.entity.Branch;
import com.sbadss.entity.PredictionRecord;
import com.sbadss.exception.ResourceNotFoundException;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.PredictionRecordRepository;
import com.sbadss.repository.SaleRepository;
import com.sbadss.service.ForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastServiceImpl implements ForecastService {

    private final SaleRepository saleRepository;
    private final BranchRepository branchRepository;
    private final PredictionRecordRepository predictionRecordRepository;
    private final RestTemplate restTemplate;

    @Value("${sbadss.ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Override
    public ForecastResponse getSalesForecast(ForecastRequest request) {
        log.info("Generating sales forecast for branch: {}, horizon: {} days",
                request.getBranchId(), request.getHorizonDays());

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + request.getBranchId()));

        // Fetch last 12 months of data for the model
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(12);
        List<Object[]> historicalData = saleRepository.getSalesTrends(startDate, endDate, request.getBranchId());

        // Try AI microservice — fallback to simple linear projection if unavailable
        try {
            return callAIService(request, branch, historicalData);
        } catch (Exception e) {
            log.warn("AI service unavailable at {}. Using fallback linear projection: {}", aiServiceUrl, e.getMessage());
            return generateFallbackForecast(request, branch, historicalData);
        }
    }

    @SuppressWarnings("unchecked")
    private ForecastResponse callAIService(ForecastRequest request, Branch branch, List<Object[]> historicalData) {
        String endpoint = aiServiceUrl + "/api/v1/forecast/sales";
        Map<String, Object> payload = Map.of(
                "branch_id", request.getBranchId(),
                "horizon_days", request.getHorizonDays(),
                "historical_data", historicalData.stream()
                        .map(row -> Map.of("date", row[0].toString(), "revenue", row[1]))
                        .toList()
        );

        log.info("Calling AI forecast service at: {}", endpoint);
        Map<String, Object> aiResponse = (Map<String, Object>) restTemplate.postForObject(endpoint, payload, Map.class);

        if (aiResponse == null) throw new RuntimeException("Null response from AI service");

        List<DataPointResponse> predictions = ((List<Map<String, Object>>) aiResponse.get("predictions"))
                .stream()
                .map(p -> new DataPointResponse(
                        p.get("date").toString(),
                        new BigDecimal(p.get("value").toString())
                ))
                .toList();

        log.info("AI forecast received: {} predictions for branch {}", predictions.size(), branch.getName());

        return ForecastResponse.builder()
                .branchId(request.getBranchId())
                .branchName(branch.getName())
                .horizonDays(request.getHorizonDays())
                .modelUsed("Prophet (AI Service)")
                .overallConfidence((Double) aiResponse.getOrDefault("confidence", 0.85))
                .predictions(predictions)
                .recommendation(aiResponse.getOrDefault("recommendation", "").toString())
                .build();
    }

    private ForecastResponse generateFallbackForecast(ForecastRequest request, Branch branch, List<Object[]> historicalData) {
        log.info("Generating fallback linear projection forecast");

        // Simple moving average on last 7 data points
        int lookback = Math.min(7, historicalData.size());
        BigDecimal avgRevenue = BigDecimal.ZERO;
        for (int i = historicalData.size() - lookback; i < historicalData.size(); i++) {
            avgRevenue = avgRevenue.add((BigDecimal) historicalData.get(i)[1]);
        }
        if (lookback > 0) avgRevenue = avgRevenue.divide(BigDecimal.valueOf(lookback), 2, java.math.RoundingMode.HALF_UP);

        List<DataPointResponse> predictions = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 1; i <= request.getHorizonDays(); i++) {
            String label = LocalDate.now().plusDays(i).format(fmt);
            // Add slight growth trend (0.5% per day)
            BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(0.005 * i));
            predictions.add(new DataPointResponse(label, avgRevenue.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        return ForecastResponse.builder()
                .branchId(request.getBranchId())
                .branchName(branch.getName())
                .horizonDays(request.getHorizonDays())
                .modelUsed("Linear Moving Average (Fallback)")
                .overallConfidence(0.60)
                .predictions(predictions)
                .recommendation("Connect the AI service for more accurate Prophet-based forecasts.")
                .build();
    }
}
