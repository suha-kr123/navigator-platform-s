package com.nivasafinance.features.lead.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.CbReportStoredEventPayload;
import com.nivasafinance.features.creditbureau.repository.CbConfigRepositoryWrapper;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.RedashQuerySheetConfig;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadDocumentWriteService;
import com.nivasafinance.features.master.codemaster.SystemLeadDocumentsMaster;
import com.nivasafinance.redash.dto.RedashExcelReportRequest;
import com.nivasafinance.redash.service.RedashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Listens for CB_REPORT_STORED events. Fetches the CB Excel report from Redash and uploads it to the lead.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CbReportStoredListener {

    private static final String CB_REPORT_FILENAME = "cb-report.xlsx";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String REDASH_CB_REPORT_SHEETS_KEY = "REDASH_CB_REPORT_SHEETS";

    private final RedashService redashService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadDocumentWriteService leadDocumentWriteService;
    private final CbConfigRepositoryWrapper cbConfigRepositoryWrapper;
    private final ObjectMapper objectMapper;

    @EventListener(condition = "#event.eventType == 'CB_REPORT_STORED'")
    @Async("eventTaskExecutor")
    public void handleCbReportStored(SystemEvent<?> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof CbReportStoredEventPayload cbPayload)) {
            log.warn("CbReportStoredListener received event with unexpected payload type: {}", payload != null ? payload.getClass() : null);
            return;
        }
        Long enquiryId = cbPayload.getEnquiryId();
        if (enquiryId == null) {
            log.warn("CbReportStoredListener received null enquiryId");
            return;
        }

        try {
            List<RedashQuerySheetConfig> sheetConfigs = getRedashQuerySheetConfigsFromCbConfig();
            if (sheetConfigs == null || sheetConfigs.isEmpty()) {
                log.warn("No Redash query sheet configs found in CB config for enquiry ID: {}, skipping CB report", enquiryId);
                return;
            }
            
            List<Long> queryIds = sheetConfigs.stream()
                    .map(RedashQuerySheetConfig::getQueryId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());
            
            Map<Long, String> queryIdToSheetName = sheetConfigs.stream()
                    .filter(config -> config.getQueryId() != null && config.getSheetName() != null)
                    .collect(Collectors.toMap(RedashQuerySheetConfig::getQueryId, RedashQuerySheetConfig::getSheetName, (existing, replacement) -> existing));
            
            log.info("Processing CB report stored for enquiry ID: {}, fetching Excel from Redash for query IDs: {} with sheet names: {}", enquiryId, queryIds, queryIdToSheetName);
            
            Map<String, Object> parameters = Map.of("enquiryId", enquiryId);
            RedashExcelReportRequest redashRequest = RedashExcelReportRequest.builder()
                    .queryIds(queryIds)
                    .parameters(parameters)
                    .queryIdToSheetName(queryIdToSheetName)
                    .build();
            
            try (InputStream excelStream = redashService.generateExcelReport(redashRequest)) {
                byte[] excelBytes = excelStream.readAllBytes();
                if (excelBytes == null || excelBytes.length == 0) {
                    log.warn("Redash returned empty Excel for enquiry ID: {}", enquiryId);
                    return;
                }

                Optional<java.util.UUID> leadIdentifierOpt = leadRepositoryWrapper.findLeadIdentifierByCbEnquiryId(enquiryId);
                if (leadIdentifierOpt.isEmpty()) {
                    log.warn("No lead found for enquiry ID: {}, skipping CB report upload", enquiryId);
                    return;
                }
                java.util.UUID leadIdentifier = leadIdentifierOpt.get();

                LeadDocumentCreateRequest leadDocRequest = LeadDocumentCreateRequest.builder()
                        .name(CB_REPORT_FILENAME)
                        .tags(List.of(SystemLeadDocumentsMaster.LEAD_CREDIT_DOCUMENTS_CRIF_REPORT))
                        .build();

                leadDocumentWriteService.createLeadDocument(leadIdentifier, excelBytes, CB_REPORT_FILENAME, EXCEL_CONTENT_TYPE, leadDocRequest);
                log.info("Successfully uploaded CB report to lead {} for enquiry ID: {}", leadIdentifier, enquiryId);
            }
        } catch (Exception e) {
            log.error("Failed to fetch Redash Excel or upload CB report for enquiry ID: {}", enquiryId, e);
        }
    }

    private List<RedashQuerySheetConfig> getRedashQuerySheetConfigsFromCbConfig() {
        return cbConfigRepositoryWrapper.findByConfigKey(REDASH_CB_REPORT_SHEETS_KEY)
                .map(config -> {
                    try {
                        String json = config.getConfigValue();
                        if (json == null || json.isBlank()) {
                            log.warn("CB config {} has empty config_value", REDASH_CB_REPORT_SHEETS_KEY);
                            return List.<RedashQuerySheetConfig>of();
                        }
                        List<RedashQuerySheetConfig> sheetConfigs = objectMapper.readValue(json, new TypeReference<List<RedashQuerySheetConfig>>() { });
                        return sheetConfigs != null ? sheetConfigs : List.<RedashQuerySheetConfig>of();
                    } catch (Exception e) {
                        log.error("Failed to parse {} config_value", REDASH_CB_REPORT_SHEETS_KEY, e);
                        return List.<RedashQuerySheetConfig>of();
                    }
                })
                .orElse(List.of());
    }
}
