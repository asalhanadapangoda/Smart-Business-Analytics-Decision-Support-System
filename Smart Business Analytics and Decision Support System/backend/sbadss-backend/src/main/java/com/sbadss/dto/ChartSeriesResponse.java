package com.sbadss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartSeriesResponse {
    private String seriesName;
    private List<DataPointResponse> dataPoints;
}
