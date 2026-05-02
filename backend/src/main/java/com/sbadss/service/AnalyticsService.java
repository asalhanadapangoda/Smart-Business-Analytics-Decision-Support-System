package com.sbadss.service;

import com.sbadss.dto.DashboardResponse;

public interface AnalyticsService {
    DashboardResponse getDashboardData(Long branchId);
    void invalidateDashboardCache(Long branchId);
}
