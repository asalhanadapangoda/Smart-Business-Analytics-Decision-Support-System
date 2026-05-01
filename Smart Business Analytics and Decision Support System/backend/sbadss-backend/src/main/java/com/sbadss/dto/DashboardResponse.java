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
public class DashboardResponse {
    private KpiMetricsDTO kpiMetrics;
    private ChartSeriesDTO salesTrends;
    private ChartSeriesDTO topProducts;
}
