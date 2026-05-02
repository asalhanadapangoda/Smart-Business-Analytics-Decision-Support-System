package com.sbadss.service;

import com.sbadss.dto.DashboardResponse;

public interface AnalyticsService {
    DashboardResponse getDashboardData(Long branchId);
    com.sbadss.dto.ProfitLossResponse getProfitLossData(Long branchId);
    void invalidateDashboardCache(Long branchId);
}
