package com.nivasafinance.externals.exotel.scheduler;

import com.nivasafinance.externals.exotel.service.ExotelReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * One-off full historical Exotel reconciliation (all {@code n_call_log} rows for provider EXOTEL).
 * <p>
 * Requires {@code exotel.reconciliation.enabled=true} (default) <strong>and</strong>
 * {@code exotel.reconciliation.historical-backfill.enabled=true}.
 * Enable only for a single deployment, verify logs, then set historical-backfill back to {@code false}.
 * If left enabled, this runs again on every application restart.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@Conditional(ExotelHistoricalReconciliationRunner.OnExotelHistoricalReconciliationEnabled.class)
public class ExotelHistoricalReconciliationRunner implements ApplicationRunner {

    private static final String START = "Exotel historical backfill starting (all rows); disable exotel.reconciliation.historical-backfill.enabled after this run";
    private static final String DONE = "Exotel historical backfill finished";
    private static final String FAILED = "Exotel historical backfill failed: {}";

    private final ExotelReconciliationService exotelReconciliationService;

    @Override
    public void run(ApplicationArguments args) {
        log.warn(START);
        try {
            exotelReconciliationService.reconcileAllHistorical();
            log.info(DONE);
        } catch (Exception e) {
            log.error(FAILED, e.getMessage(), e);
        }
    }

    /**
     * Both properties must match (AND). Multiple {@link ConditionalOnProperty} on one type are not reliably
     * composed; {@link AllNestedConditions} applies every nested condition.
     */
    static final class OnExotelHistoricalReconciliationEnabled extends AllNestedConditions {

        OnExotelHistoricalReconciliationEnabled() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = "exotel.reconciliation", name = "enabled", havingValue = "true", matchIfMissing = true)
        static class ReconciliationEnabled {
        }

        @ConditionalOnProperty(prefix = "exotel.reconciliation.historical-backfill", name = "enabled", havingValue = "true")
        static class HistoricalBackfillEnabled {
        }
    }
}
