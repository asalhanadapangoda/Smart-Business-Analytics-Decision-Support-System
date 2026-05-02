package com.sbadss.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForecastResponse {
    private Long branchId;
    private String branchName;
    private int horizonDays;
    private String modelUsed;
    private Double overallConfidence;
    private List<DataPointResponse> predictions;
    private String recommendation;
}
