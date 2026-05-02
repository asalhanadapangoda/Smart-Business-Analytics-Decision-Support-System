package com.sbadss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequest {

    @NotNull(message = "Report type is required")
    private String reportType;

    @NotNull(message = "Report format is required")
    private String format;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long branchId;
    private String emailRecipients;
}
