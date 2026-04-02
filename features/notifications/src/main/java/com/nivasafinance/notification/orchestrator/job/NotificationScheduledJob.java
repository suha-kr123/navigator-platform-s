package com.nivasafinance.notification.orchestrator.job;

import com.nivasafinance.notification.orchestrator.service.NotificationRecordService;
import com.nivasafinance.notification.orchestrator.service.NotificationReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduledJob {

    private static final long STALE_THRESHOLD_MINUTES = 30;

    private final NotificationRecordService notificationRecordService;
    private final NotificationReceiptService notificationReceiptService;

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

    @Scheduled(fixedDelay = 900_000)
    public void sweepStaleNotifications() {
        log.info("Running stale notification sweeper (threshold: {} minutes)", STALE_THRESHOLD_MINUTES);
        try {
            int records = notificationRecordService.markStaleRecordsAsFailed(STALE_THRESHOLD_MINUTES);
            int receipts = notificationReceiptService.markStaleReceiptsAsFailed(STALE_THRESHOLD_MINUTES);
            if (records > 0 || receipts > 0) {
                log.warn("Sweeper marked {} stale records and {} stale receipts as FAILED", records, receipts);
            } else {
                log.info("Sweeper found no stale notifications");
            }
        } catch (Exception ex) {
            log.error("Stale notification sweeper failed", ex);
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


