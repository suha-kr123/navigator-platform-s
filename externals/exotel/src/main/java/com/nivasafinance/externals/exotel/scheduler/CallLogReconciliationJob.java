package com.nivasafinance.externals.exotel.scheduler;

import com.nivasafinance.externals.exotel.service.ExotelReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "exotel.reconciliation", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CallLogReconciliationJob {

    private static final String JOB_FAILED = "Call log reconciliation job failed: {}";

    private final ExotelReconciliationService exotelReconciliationService;

    /**
     * Daily reconciliation of recent Exotel rows ({@code exotel.reconciliation.lookback-days}).
     * Default: 02:00 in {@code exotel.reconciliation.cron-zone} (default Asia/Kolkata).
     */
    @Scheduled(
            cron = "${exotel.reconciliation.cron:0 0 2 * * *}",
            zone = "${exotel.reconciliation.cron-zone:Asia/Kolkata}")
    public void reconcileRecentWindow() {
        try {
            exotelReconciliationService.reconcilePreviousDay();
        } catch (Exception e) {
            log.error(JOB_FAILED, e.getMessage(), e);
        }
    }
}
