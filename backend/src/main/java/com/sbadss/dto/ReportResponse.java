package com.sbadss.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private Long id;
    private String reportType;
    private String format;
    private String status;
    private String fileName;
    private String downloadUrl;
    private LocalDate dateRangeStart;
    private LocalDate dateRangeEnd;
    private String branchName;
    private LocalDateTime generatedAt;
}
