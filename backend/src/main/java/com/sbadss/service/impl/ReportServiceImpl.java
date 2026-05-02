package com.sbadss.service.impl;

import com.sbadss.dto.ReportRequest;
import com.sbadss.dto.ReportResponse;
import com.sbadss.entity.Branch;
import com.sbadss.entity.Report;
import com.sbadss.entity.User;
import com.sbadss.exception.BusinessException;
import com.sbadss.exception.ResourceNotFoundException;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.ExpenseRepository;
import com.sbadss.repository.ReportRepository;
import com.sbadss.repository.SaleRepository;
import com.sbadss.repository.UserRepository;
import com.sbadss.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${sbadss.report.output.dir:./reports}")
    private String reportOutputDir;

    @Value("${sbadss.report.email.enabled:false}")
    private boolean emailEnabled;

    @Override
    @Transactional
    public ReportResponse generateReport(ReportRequest request, Long userId) {
        log.info("Generating {} report in {} format for user: {}", request.getReportType(), request.getFormat(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Branch branch = request.getBranchId() != null ?
                branchRepository.findById(request.getBranchId()).orElse(null) : null;

        Report.ReportType type = Report.ReportType.valueOf(request.getReportType().toUpperCase());
        Report.ReportFormat format = Report.ReportFormat.valueOf(request.getFormat().toUpperCase());

        // Create report record
        Report report = Report.builder()
                .reportType(type)
                .format(format)
                .generatedBy(user)
                .branch(branch)
                .dateRangeStart(request.getStartDate() != null ? request.getStartDate() : LocalDate.now().withDayOfMonth(1))
                .dateRangeEnd(request.getEndDate() != null ? request.getEndDate() : LocalDate.now())
                .status(Report.ReportStatus.GENERATING)
                .emailRecipients(request.getEmailRecipients())
                .build();
        report = reportRepository.save(report);

        try {
            String filePath = generateFile(report, format);
            report.setFilePath(filePath);
            report.setFileName(new File(filePath).getName());
            report.setStatus(Report.ReportStatus.COMPLETED);
            reportRepository.save(report);

            // Send email if recipients are specified
            if (emailEnabled && request.getEmailRecipients() != null && !request.getEmailRecipients().isBlank()) {
                sendReportEmail(report, filePath, request.getEmailRecipients());
            }

            log.info("Report generated successfully: {}", report.getFileName());
            return toResponse(report);
        } catch (Exception e) {
            log.error("Report generation failed: ", e);
            report.setStatus(Report.ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            reportRepository.save(report);
            throw new BusinessException("Report generation failed: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadReport(Long reportId) {
        log.info("Downloading report: {}", reportId);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (report.getStatus() != Report.ReportStatus.COMPLETED) {
            throw new BusinessException("Report is not yet available for download");
        }

        FileSystemResource resource = new FileSystemResource(report.getFilePath());
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Report file not found on server");
        }
        return resource;
    }

    @Override
    public List<ReportResponse> getReportHistory(Long branchId) {
        log.info("Fetching report history for branch: {}", branchId);
        List<Report> reports = branchId != null ?
                reportRepository.findByBranchId(branchId) : reportRepository.findAll();
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private String generateFile(Report report, Report.ReportFormat format) throws IOException {
        new File(reportOutputDir).mkdirs();
        String fileName = String.format("%s_%s_%s.%s",
                report.getReportType().name().toLowerCase(),
                report.getId(),
                DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(LocalDateTime.now()),
                format == Report.ReportFormat.EXCEL ? "xlsx" : "pdf"
        );
        String filePath = reportOutputDir + File.separator + fileName;

        if (format == Report.ReportFormat.EXCEL) {
            generateExcelReport(report, filePath);
        } else {
            generatePdfReport(report, filePath);
        }
        return filePath;
    }

    private void generateExcelReport(Report report, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet(report.getReportType().name());

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SBADSS — " + report.getReportType().name() + " Report");
            titleCell.setCellStyle(headerStyle);

            // Date range row
            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Period: " + report.getDateRangeStart() + " to " + report.getDateRangeEnd());

            // Data headers
            Row headerRow = sheet.createRow(3);
            String[] headers = getHeadersForType(report.getReportType());
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows (summary)
            int rowNum = 4;
            if (report.getReportType() == Report.ReportType.SALES) {
                BigDecimal totalRev = saleRepository.calculateTotalRevenue(
                        report.getDateRangeStart().atStartOfDay(),
                        report.getDateRangeEnd().atTime(23, 59),
                        report.getBranch() != null ? report.getBranch().getId() : null
                );
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue("Total Revenue");
                dataRow.createCell(1).setCellValue(totalRev != null ? totalRev.doubleValue() : 0.0);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(fos);
        }
        log.info("Excel report written to: {}", filePath);
    }

    private void generatePdfReport(Report report, String filePath) throws IOException {
        // iText PDF generation
        try (com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(filePath);
             com.itextpdf.kernel.pdf.PdfDocument pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
             com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdf)) {

            document.add(new com.itextpdf.layout.element.Paragraph("SBADSS — " + report.getReportType().name() + " Report")
                    .setFontSize(18).setBold());
            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Period: " + report.getDateRangeStart() + " to " + report.getDateRangeEnd()));
            document.add(new com.itextpdf.layout.element.Paragraph(
                    "Generated At: " + LocalDateTime.now()));
            document.add(new com.itextpdf.layout.element.Paragraph("\n"));

            if (report.getReportType() == Report.ReportType.SALES) {
                BigDecimal totalRev = saleRepository.calculateTotalRevenue(
                        report.getDateRangeStart().atStartOfDay(),
                        report.getDateRangeEnd().atTime(23, 59),
                        report.getBranch() != null ? report.getBranch().getId() : null
                );
                document.add(new com.itextpdf.layout.element.Paragraph(
                        "Total Revenue: " + (totalRev != null ? totalRev : BigDecimal.ZERO)));
            }
        }
        log.info("PDF report written to: {}", filePath);
    }

    private void sendReportEmail(Report report, String filePath, String recipients) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(Arrays.stream(recipients.split(",")).map(String::trim).toArray(String[]::new));
            message.setSubject("SBADSS Report: " + report.getReportType().name());
            message.setText("Please find the attached " + report.getReportType().name() + " report for the period " +
                    report.getDateRangeStart() + " to " + report.getDateRangeEnd() +
                    ".\n\nDownload the report from the SBADSS Reports section.");
            mailSender.send(message);
            log.info("Report notification email sent to: {}", recipients);
        } catch (Exception e) {
            log.warn("Failed to send report email: {}", e.getMessage());
        }
    }

    private String[] getHeadersForType(Report.ReportType type) {
        return switch (type) {
            case SALES -> new String[]{"Metric", "Value"};
            case EXPENSES -> new String[]{"Category", "Amount"};
            case CUSTOMERS -> new String[]{"Customer", "Total Purchases"};
            case PRODUCTS -> new String[]{"Product", "Quantity Sold", "Revenue"};
            default -> new String[]{"Metric", "Value"};
        };
    }

    private ReportResponse toResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportType(report.getReportType().name())
                .format(report.getFormat().name())
                .status(report.getStatus().name())
                .fileName(report.getFileName())
                .downloadUrl("/api/v1/reports/" + report.getId() + "/download")
                .dateRangeStart(report.getDateRangeStart())
                .dateRangeEnd(report.getDateRangeEnd())
                .branchName(report.getBranch() != null ? report.getBranch().getName() : "All Branches")
                .generatedAt(report.getCreatedAt())
                .build();
    }
}
