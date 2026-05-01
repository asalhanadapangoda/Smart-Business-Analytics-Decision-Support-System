package com.sbadss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KpiMetricsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private BigDecimal totalExpenses;
    
    // Percentage growth: ((Current - Previous) / Previous) * 100
    private Double revenueGrowth;
    private Double profitGrowth;
    private Double expenseGrowth;
}
