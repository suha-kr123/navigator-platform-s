package com.nivasafinance.externals.exotel.service.impl;

import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.externals.exotel.dto.ExotelCallReconciliationSnapshot;
import com.nivasafinance.externals.exotel.dto.ExotelReconciliationSummary;
import com.nivasafinance.externals.exotel.service.ExotelReconciliationService;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.reconciliation.ReconciliationCorrectionSources;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.voice.VoiceHandler;
import com.nivasafinance.services.voice.dto.VoiceGetCallStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExotelReconciliationServiceImpl implements ExotelReconciliationService {

    private static final String LOG_WINDOW = "Exotel call reconciliation started for window [{}, {})";
    private static final String LOG_BATCH = "Reconciliation batch page {} size {}";
    private static final String LOG_SUMMARY =
            "Exotel call reconciliation finished: rowsFetched={}, rowsUpdated={}, statusCorrections={}, "
                    + "durationCorrections={}, apiFailures={}, skippedInvalidRows={}";
    private static final String LOG_ROW_WARN = "Reconciliation skipped for call_log id={}, provider_id={}: {}";
    private static final String LOG_API_WARN = "Exotel getCallStatus failed for provider_id={}: {}";
    private static final String LOG_ALERT =
            "EXOTEL_RECONCILIATION_ALERT: apiFailures={} rowsFetched={} statusCorrections={} "
                    + "durationCorrections={} skippedInvalidRows={}";

    private final CallReadService callReadService;
    private final ReconciliationRowProcessor rowProcessor;
    private final ServiceFactory<VoiceHandler> voiceServiceFactory;

    @Value("${exotel.reconciliation.batch-size:500}")
    private int batchSize;

    @Value("${exotel.reconciliation.zone:}")
    private String reconciliationZone;

    @Value("${exotel.reconciliation.historical-backfill.lookback-days:30}")
    private int historicalBackfillLookbackDays;

    @Value("${exotel.reconciliation.manual-max-range-days:366}")
    private int manualMaxRangeDays;

    @Override
    public ExotelReconciliationSummary reconcilePreviousDay() {
        ZoneId zone = zoneOrSystemDefault();

        LocalDate today = LocalDate.now(zone);
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = today.atStartOfDay();

        log.info(LOG_WINDOW, start, end);
        return runPagedReconciliation(
                pageable -> callReadService.findByProviderAndCreatedAtRange(
                        CallProvider.EXOTEL, start, end, pageable),
                ReconciliationCorrectionSources.RECONCILIATION_JOB);
    }

    @Override
    public ExotelReconciliationSummary reconcileAllHistorical() {
        ZoneId zone = zoneOrSystemDefault();
        int days = Math.max(1, historicalBackfillLookbackDays);
        LocalDate today = LocalDate.now(zone);
        LocalDateTime start = today.minusDays(days).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        log.info(LOG_WINDOW, start, end);
        return runPagedReconciliation(
                pageable -> callReadService.findByProviderAndCreatedAtRange(
                        CallProvider.EXOTEL, start, end, pageable),
                ReconciliationCorrectionSources.RECONCILIATION_HISTORICAL_BACKFILL);
    }

    @Override
    public ExotelReconciliationSummary reconcileDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        long spanDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int maxDays = Math.max(1, manualMaxRangeDays);
        if (spanDays > maxDays) {
            throw new IllegalArgumentException(
                    "Date range exceeds exotel.reconciliation.manual-max-range-days (" + maxDays + " days)");
        }

        ZoneId zone = zoneOrSystemDefault();
        LocalDateTime start = startDate.atStartOfDay(zone).toLocalDateTime();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay(zone).toLocalDateTime();

        log.info(LOG_WINDOW, start, end);
        return runPagedReconciliation(
                pageable -> callReadService.findByProviderAndCreatedAtRange(
                        CallProvider.EXOTEL, start, end, pageable),
                ReconciliationCorrectionSources.RECONCILIATION_ADMIN_API);
    }

    private ZoneId zoneOrSystemDefault() {
        return reconciliationZone == null || reconciliationZone.isBlank()
                ? ZoneId.systemDefault()
                : ZoneId.of(reconciliationZone);
    }

    private ExotelReconciliationSummary runPagedReconciliation(
            Function<Pageable, Page<CallLog>> pageQuery, String correctionSource) {
        int rowsFetched = 0;
        int rowsUpdated = 0;
        int statusCorrections = 0;
        int durationCorrections = 0;
        int apiFailures = 0;
        int skippedInvalidRows = 0;

        int pageNum = 0;
        while (true) {
            int size = Math.max(1, batchSize);
            Pageable pageable = PageRequest.of(pageNum, size);
            Page<CallLog> page = pageQuery.apply(pageable);

            if (page.isEmpty()) {
                break;
            }

            log.info(LOG_BATCH, pageNum, page.getNumberOfElements());
            rowsFetched += page.getNumberOfElements();

            for (CallLog row : page.getContent()) {
                try {
                    ReconcileOutcome outcome = reconcileOne(row, correctionSource);
                    if (outcome.skippedInvalid) {
                        skippedInvalidRows++;
                    }
                    if (outcome.apiFailure) {
                        apiFailures++;
                    }
                    if (outcome.updated) {
                        rowsUpdated++;
                    }
                    if (outcome.statusChanged) {
                        statusCorrections++;
                    }
                    if (outcome.durationChanged) {
                        durationCorrections++;
                    }
                } catch (Exception e) {
                    apiFailures++;
                    log.warn(LOG_ROW_WARN, row.getId(), row.getProviderId(), e.getMessage());
                }
            }

            if (!page.hasNext()) {
                break;
            }
            pageNum++;
        }

        log.info(LOG_SUMMARY, rowsFetched, rowsUpdated, statusCorrections, durationCorrections, apiFailures, skippedInvalidRows);

        ExotelReconciliationSummary summary = new ExotelReconciliationSummary(
                rowsFetched, rowsUpdated, statusCorrections, durationCorrections, apiFailures, skippedInvalidRows);
        if (apiFailures > 0) {
            log.error(
                    LOG_ALERT,
                    apiFailures,
                    rowsFetched,
                    statusCorrections,
                    durationCorrections,
                    skippedInvalidRows);
        }
        return summary;
    }

    private ReconcileOutcome reconcileOne(CallLog callLog, String correctionSource) {
        if (callLog.getProviderId() == null || callLog.getProviderId().isBlank()) {
            log.debug(LOG_ROW_WARN, callLog.getId(), null, "blank provider_id");
            return ReconcileOutcome.invalid();
        }

        ExotelCallReconciliationSnapshot snap = fetchSnapshot(callLog.getProviderId());
        if (snap == null) {
            return ReconcileOutcome.apiFailureOnly();
        }

        ReconciliationRowProcessor.ReconcileResult result =
                rowProcessor.processRow(callLog, snap.getStatus(), snap.getDurationSeconds(), correctionSource);

        return new ReconcileOutcome(result.updated, result.statusChanged, result.durationChanged, false, false);
    }

    private ExotelCallReconciliationSnapshot fetchSnapshot(String providerCallId) {
        try {
            VoiceHandler voiceHandler = voiceServiceFactory.getHandler(ThirdPartyServiceList.VOICE);
            BusinessContext businessContext = new BusinessContext(
                    SystemEntities.LEAD.name(),
                    null,
                    "EXOTEL_CALL_RECONCILIATION"
            );
            VoiceGetCallStatusResponse response = voiceHandler.getCallStatus(providerCallId, businessContext);
            if (response == null || response.getStatus() == null) {
                return null;
            }
            CallStatus status = CallStatus.fromVoiceStatus(response.getStatus());
            Long durationSeconds = null;
            if (response.getCallDetails() != null && response.getCallDetails().getTotalDuration() != null) {
                int td = response.getCallDetails().getTotalDuration();
                if (td > 0) {
                    durationSeconds = (long) td;
                }
            }
            return ExotelCallReconciliationSnapshot.builder()
                    .status(status)
                    .durationSeconds(durationSeconds)
                    .build();
        } catch (Exception e) {
            log.warn(LOG_API_WARN, providerCallId, e.getMessage());
            return null;
        }
    }

    private static final class ReconcileOutcome {
        final boolean updated;
        final boolean statusChanged;
        final boolean durationChanged;
        final boolean apiFailure;
        final boolean skippedInvalid;

        private ReconcileOutcome(
                boolean updated,
                boolean statusChanged,
                boolean durationChanged,
                boolean apiFailure,
                boolean skippedInvalid) {
            this.updated = updated;
            this.statusChanged = statusChanged;
            this.durationChanged = durationChanged;
            this.apiFailure = apiFailure;
            this.skippedInvalid = skippedInvalid;
        }

        static ReconcileOutcome invalid() {
            return new ReconcileOutcome(false, false, false, false, true);
        }

        static ReconcileOutcome apiFailureOnly() {
            return new ReconcileOutcome(false, false, false, true, false);
        }
    }
}
