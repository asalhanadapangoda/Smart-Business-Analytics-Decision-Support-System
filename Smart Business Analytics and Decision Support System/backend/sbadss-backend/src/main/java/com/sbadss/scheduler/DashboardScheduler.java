package com.sbadss.scheduler;

import com.sbadss.dto.DashboardResponse;
import com.sbadss.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardScheduler {

    private final AnalyticsService analyticsService;
    private final SimpMessagingTemplate messagingTemplate;

    // Broadcast global dashboard data every 30 seconds
    @Scheduled(fixedRate = 30000)
    public void broadcastDashboardUpdate() {
        log.debug("Broadcasting scheduled real-time dashboard update");
        try {
            DashboardResponse globalData = analyticsService.getDashboardData(null);
            messagingTemplate.convertAndSend("/topic/dashboard/global", globalData);
            
            // Note: In a production scenario with many branches, we wouldn't loop through all branches here.
            // Instead, clients would subscribe to a specific branch topic and the backend would only compute/push
            // if there are active subscribers to that topic. For this MVP, we broadcast the global data.
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard update", e);
        }
    }
}
