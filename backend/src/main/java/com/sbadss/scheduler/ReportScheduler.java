package com.sbadss.scheduler;

import com.sbadss.dto.ReportRequest;
import com.sbadss.entity.Branch;
import com.sbadss.entity.User;
import com.sbadss.repository.BranchRepository;
import com.sbadss.repository.UserRepository;
import com.sbadss.service.ReportService;
import com.sbadss.service.NotificationService;
import com.sbadss.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportService reportService;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Every Monday at 01:00 AM - Weekly Sales Report for all branches
     */
    @Scheduled(cron = "0 0 1 * * MON")
    public void generateWeeklySalesReports() {
        log.info("Starting scheduled weekly sales report generation...");
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start = end.minusDays(6);

        List<Branch> branches = branchRepository.findAll();
        User systemUser = userRepository.findByUsername("admin").orElse(null);

        if (systemUser == null) {
            log.error("System user 'admin' not found. Cannot generate scheduled reports.");
            return;
        }

        for (Branch branch : branches) {
            ReportRequest request = ReportRequest.builder()
                    .reportType("SALES")
                    .format("PDF")
                    .startDate(start)
                    .endDate(end)
                    .branchId(branch.getId())
                    .build();

            try {
                reportService.generateReport(request, systemUser.getId());
                notificationService.sendNotification(
                        systemUser.getId(),
                        "Weekly Report Ready",
                        "Weekly sales report for " + branch.getName() + " has been generated.",
                        Notification.NotificationType.REPORT_READY
                );
            } catch (Exception e) {
                log.error("Failed to generate scheduled report for branch {}: {}", branch.getName(), e.getMessage());
            }
        }
    }

    /**
     * Every 1st of the month at 02:00 AM - Monthly Financial Summary
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void generateMonthlyFinancialReports() {
        log.info("Starting scheduled monthly financial report generation...");
        LocalDate end = LocalDate.now().minusDays(1);
        LocalDate start = end.withDayOfMonth(1);

        User systemUser = userRepository.findByUsername("admin").orElse(null);
        if (systemUser == null) return;

        ReportRequest request = ReportRequest.builder()
                .reportType("COMPREHENSIVE")
                .format("EXCEL")
                .startDate(start)
                .endDate(end)
                .build();

        try {
            reportService.generateReport(request, systemUser.getId());
            notificationService.sendBroadcast(
                    "Monthly Report Generated",
                    "The comprehensive financial report for " + start.getMonth() + " is now available.",
                    Notification.NotificationType.REPORT_READY
            );
        } catch (Exception e) {
            log.error("Failed to generate monthly report: {}", e.getMessage());
        }
    }
}
