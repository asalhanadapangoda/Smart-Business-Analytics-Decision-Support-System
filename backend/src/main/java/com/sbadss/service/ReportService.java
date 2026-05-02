package com.sbadss.service;

import com.sbadss.dto.ReportRequest;
import com.sbadss.dto.ReportResponse;
import org.springframework.core.io.Resource;

import java.util.List;

public interface ReportService {
    ReportResponse generateReport(ReportRequest request, Long userId);
    Resource downloadReport(Long reportId);
    List<ReportResponse> getReportHistory(Long branchId);
}
