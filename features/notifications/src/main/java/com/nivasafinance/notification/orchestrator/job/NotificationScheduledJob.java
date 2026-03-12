package com.nivasafinance.notification.orchestrator.job;

import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduledJob {

    private final NotificationRecordService notificationRecordService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        log.info("Running daily reminder notification job");
        try {
            notificationRecordService.createManualNotification(
                    1L,
                    "DAILY_REMINDER_" + System.currentTimeMillis(),
                    Map.of("jobType", "DAILY_REMINDER")
            );
        } catch (Exception ex) {
            log.error("Failed to trigger daily reminder notification", ex);
        }
    }

    /**
     * Placeholder for retrying failed notification receipts. Not scheduled until implemented.
     * When ready: add @Scheduled and implement logic (e.g. re-publish FAILED receipts to executor queue).
     */
    public void retryFailedNotifications() {
        log.debug("Retry job placeholder - implement retry logic");
    }
}


