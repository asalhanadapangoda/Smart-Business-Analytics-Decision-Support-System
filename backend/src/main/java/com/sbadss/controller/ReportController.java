package com.sbadss.controller;

import com.sbadss.common.ApiResponse;
import com.sbadss.dto.ReportRequest;
import com.sbadss.dto.ReportResponse;
import com.sbadss.entity.User;
import com.sbadss.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/v1/reports/generate - type: {}, format: {}", request.getReportType(), request.getFormat());
        return ResponseEntity.ok(ApiResponse.success(
                reportService.generateReport(request, user.getId()), "Report generation started"));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadReport(@PathVariable Long id) {
        log.info("GET /api/v1/reports/{}/download", id);
        Resource resource = reportService.downloadReport(id);
        String contentType = resource.getFilename() != null && resource.getFilename().endsWith(".xlsx")
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportHistory(
            @RequestParam(required = false) Long branchId) {
        log.info("GET /api/v1/reports/history - branch: {}", branchId);
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportHistory(branchId), "Report history fetched"));
    }
}
