package com.sbadss.service.impl;

import com.sbadss.dto.ChartSeriesResponse;
import com.sbadss.dto.DashboardResponse;
import com.sbadss.dto.DataPointResponse;
import com.sbadss.dto.KpiMetricsResponse;
import com.sbadss.repository.ExpenseRepository;
import com.sbadss.repository.SaleItemRepository;
import com.sbadss.repository.SaleRepository;
import com.sbadss.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final SaleItemRepository saleItemRepository;

    @Override
    @Cacheable(value = "dashboardData", key = "#branchId != null ? #branchId : 'global'")
    public DashboardResponse getDashboardData(Long branchId) {
        log.info("Calculating expensive dashboard aggregates for branch: {}", branchId);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusNanos(1);

        // Current Period Metrics
        BigDecimal currentRevenue = getRevenue(startOfMonth, now, branchId);
        BigDecimal currentExpense = getExpense(startOfMonth.toLocalDate(), now.toLocalDate(), branchId);
        BigDecimal currentProfit = currentRevenue.subtract(currentExpense);

        // Previous Period Metrics
        BigDecimal previousRevenue = getRevenue(startOfLastMonth, endOfLastMonth, branchId);
        BigDecimal previousExpense = getExpense(startOfLastMonth.toLocalDate(), endOfLastMonth.toLocalDate(), branchId);
        BigDecimal previousProfit = previousRevenue.subtract(previousExpense);

        KpiMetricsResponse kpi = KpiMetricsResponse.builder()
                .totalRevenue(currentRevenue)
                .totalExpenses(currentExpense)
                .totalProfit(currentProfit)
                .revenueGrowth(calculateGrowth(currentRevenue, previousRevenue))
                .expenseGrowth(calculateGrowth(currentExpense, previousExpense))
                .profitGrowth(calculateGrowth(currentProfit, previousProfit))
                .build();

        // Chart Data
        ChartSeriesResponse trends = fetchSalesTrends(startOfMonth, now, branchId);
        ChartSeriesResponse topProducts = fetchTopProducts(branchId);

        return DashboardResponse.builder()
                .kpiMetrics(kpi)
                .salesTrends(trends)
                .topProducts(topProducts)
                .build();
    }

    @Override
    @CacheEvict(value = "dashboardData", key = "#branchId != null ? #branchId : 'global'")
    public void invalidateDashboardCache(Long branchId) {
        log.info("Invalidating dashboard cache for branch: {}", branchId);
        // Also invalidate global cache if a specific branch is updated, since global includes all branches
        if (branchId != null) {
            invalidateGlobalCache();
        }
    }

    @CacheEvict(value = "dashboardData", key = "'global'")
    public void invalidateGlobalCache() {
        log.debug("Invalidating global dashboard cache");
    }

    private BigDecimal getRevenue(LocalDateTime start, LocalDateTime end, Long branchId) {
        BigDecimal rev = saleRepository.calculateTotalRevenue(start, end, branchId);
        return rev != null ? rev : BigDecimal.ZERO;
    }

    private BigDecimal getExpense(LocalDate start, LocalDate end, Long branchId) {
        BigDecimal exp = expenseRepository.calculateTotalExpenses(start, end, branchId);
        return exp != null ? exp : BigDecimal.ZERO;
    }

    private Double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal diff = current.subtract(previous);
        return diff.divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue();
    }

    private ChartSeriesResponse fetchSalesTrends(LocalDateTime start, LocalDateTime end, Long branchId) {
        List<Object[]> rawData = saleRepository.getSalesTrends(start, end, branchId);
        List<DataPointResponse> points = new ArrayList<>();
        for (Object[] row : rawData) {
            points.add(new DataPointResponse(row[0].toString(), (BigDecimal) row[1]));
        }
        return new ChartSeriesResponse("Daily Revenue", points);
    }

    private ChartSeriesResponse fetchTopProducts(Long branchId) {
        List<Object[]> rawData = saleItemRepository.findTopProducts(branchId, PageRequest.of(0, 10)); // Top 10
        List<DataPointResponse> points = new ArrayList<>();
        for (Object[] row : rawData) {
            points.add(new DataPointResponse(row[0].toString(), new BigDecimal(((Number) row[1]).longValue())));
        }
        return new ChartSeriesResponse("Top Products", points);
    }
}
