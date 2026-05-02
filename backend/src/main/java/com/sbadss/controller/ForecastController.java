package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.ForecastRequest;
import com.sbadss.dto.ForecastResponse;
import com.sbadss.service.ForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/forecasts")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @PostMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ForecastResponse>> getSalesForecast(
            @Valid @RequestBody ForecastRequest request) {
        log.info("POST /api/v1/ai/forecast/sales - branch: {}, horizon: {} days",
                request.getBranchId(), request.getHorizonDays());
        ForecastResponse response = forecastService.getSalesForecast(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Forecast generated successfully"));
    }
}
