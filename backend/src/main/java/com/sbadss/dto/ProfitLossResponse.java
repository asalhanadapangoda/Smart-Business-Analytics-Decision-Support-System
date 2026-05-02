package com.sbadss.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class ProfitLossResponse {
    private BigDecimal grossRevenue;
    private BigDecimal costOfGoodsSold;
    private BigDecimal grossProfit;
    private BigDecimal operatingExpenses; // Placeholder or static for now
    private BigDecimal taxAmount;
    private BigDecimal netProfit;
    private Map<String, BigDecimal> revenueByCategory;
}
