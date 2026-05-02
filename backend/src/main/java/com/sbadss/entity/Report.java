package com.sbadss.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_report_branch", columnList = "branch_id"),
        @Index(name = "idx_report_type", columnList = "report_type")
})
public class Report extends BaseEntity {

    public enum ReportType { SALES, EXPENSES, CUSTOMERS, PRODUCTS, COMPREHENSIVE }
    public enum ReportFormat { PDF, EXCEL }
    public enum ReportStatus { PENDING, GENERATING, COMPLETED, FAILED }
    public enum ReportFrequency { ONE_TIME, DAILY, WEEKLY, MONTHLY }

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private ReportFormat format;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", nullable = false)
    private User generatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "date_range_start")
    private LocalDate dateRangeStart;

    @Column(name = "date_range_end")
    private LocalDate dateRangeEnd;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency")
    @Builder.Default
    private ReportFrequency frequency = ReportFrequency.ONE_TIME;

    @Column(name = "email_recipients", columnDefinition = "TEXT")
    private String emailRecipients;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
