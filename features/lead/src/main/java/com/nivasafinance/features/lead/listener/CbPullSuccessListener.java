package com.nivasafinance.features.lead.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadCbSuccessEventPayload;
import com.nivasafinance.features.creditbureau.dto.DemographicVariationResponse;
import com.nivasafinance.features.creditbureau.repository.CbConfigRepositoryWrapper;
import com.nivasafinance.features.creditbureau.service.CreditBureauDerivedAttributeWriteService;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.RedashQuerySheetConfig;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadDocumentWriteService;
import com.nivasafinance.features.master.codemaster.SystemLeadDocumentsMaster;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.redash.dto.RedashExcelReportRequest;
import com.nivasafinance.redash.service.RedashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Listens for LEAD_CB_PULL_SUCCESS events. Fetches the CB Excel report from Redash and uploads it to the lead;
 * persists Redash-derived attributes per {@code REDASH_CB_DERIVED_QUERIES} config.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CbPullSuccessListener {

    private static final String CB_REPORT_FILENAME = "cb-report.xlsx";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String REDASH_CB_REPORT_SHEETS_KEY = "REDASH_CB_REPORT_SHEETS";
    private static final String ADDRESS_VARIATION_TYPE = "ADDRESS-VARIATIONS";
    private static final Pattern PINCODE_PATTERN = Pattern.compile("\\b[0-9]{6}\\b");

    private final RedashService redashService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadDocumentWriteService leadDocumentWriteService;
    private final CbConfigRepositoryWrapper cbConfigRepositoryWrapper;
    private final CreditBureauDerivedAttributeWriteService cbDerivedAttributeWriteService;
    private final CreditBureauReadService creditBureauReadService;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final PersonWriteService personWriteService;
    private final ObjectMapper objectMapper;

    @EventListener(condition = "#event.eventType == 'LEAD_CB_PULL_SUCCESS'")
    @Async("eventTaskExecutor")
    public void handleCbReportStored(SystemEvent<?> event) {
        Object payload = event.getPayload();
        if (!(payload instanceof LeadCbSuccessEventPayload cbPayload)) {
            log.warn("CbReportStoredListener received event with unexpected payload type: {}", payload != null ? payload.getClass() : null);
            return;
        }
        Long enquiryId = cbPayload.getEnquiryId();
        if (enquiryId == null) {
            log.warn("CbReportStoredListener received null enquiryId");
            return;
        }

        Long leadDbId = resolveLeadDbId(cbPayload, enquiryId);

        try {
            uploadCbExcelReportIfConfigured(enquiryId);
        } catch (Exception e) {
            log.error("Failed to fetch Redash Excel or upload CB report for enquiry ID: {}", enquiryId, e);
        }

        try {
            cbDerivedAttributeWriteService.saveDerivedAttributesForEnquiry(enquiryId, leadDbId);
        } catch (Exception e) {
            log.error("Failed to persist CB derived attributes for enquiry ID: {}", enquiryId, e);
        }

        try {
            addCbReportedAddressToContact(enquiryId);
        } catch (Exception e) {
            log.error("Failed to add CB reported address for enquiry ID: {}", enquiryId, e);
        }
    }

    private void addCbReportedAddressToContact(Long enquiryId) {
        List<DemographicVariationResponse> variations = creditBureauReadService.getDemographicVariationsByEnquiryId(enquiryId);

        Optional<DemographicVariationResponse> latestAddressVariation = variations.stream()
                .filter(v -> ADDRESS_VARIATION_TYPE.equalsIgnoreCase(v.getVariationType()))
                .filter(v -> v.getVariationValue() != null && !v.getVariationValue().isBlank())
                .sorted(Comparator.comparing(DemographicVariationResponse::getReportedDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst();

        if (latestAddressVariation.isEmpty()) {
            log.info("No ADDRESS variation found for enquiry ID: {}, skipping CB reported address", enquiryId);
            return;
        }

        String variationValue = latestAddressVariation.get().getVariationValue();
        String pincode = extractPincode(variationValue);
        if (pincode == null) {
            log.warn("No 6-digit pincode found in ADDRESS variation for enquiry ID: {}, skipping", enquiryId);
            return;
        }

        Optional<Contact> contactOpt = contactRepositoryWrapper.findByCbEnquiryId(enquiryId);
        if (contactOpt.isEmpty()) {
            log.warn("No contact found for enquiry ID: {}, skipping CB reported address", enquiryId);
            return;
        }

        Long personId = contactOpt.get().getPersonId();

        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setAddressType(AddressType.CB_REPORTED);
        addressRequest.setAddress(variationValue);
        addressRequest.setPincode(new AddressRequest.PincodeRequest(pincode, null, null));

        personWriteService.addAddress(personId, addressRequest);
        log.info("Added CB reported address to person ID: {} for enquiry ID: {}", personId, enquiryId);
    }

    private String extractPincode(String value) {
        Matcher matcher = PINCODE_PATTERN.matcher(value);
        String lastMatch = null;
        while (matcher.find()) {
            lastMatch = matcher.group();
        }
        return lastMatch;
    }

    private Long resolveLeadDbId(LeadCbSuccessEventPayload cbPayload, Long enquiryId) {
        if (cbPayload.getLeadId() != null) {
            return cbPayload.getLeadId();
        }
        Optional<UUID> leadIdentifierOpt = leadRepositoryWrapper.findLeadIdentifierByCbEnquiryId(enquiryId);
        if (leadIdentifierOpt.isEmpty()) {
            return null;
        }
        return leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifierOpt.get()).getId();
    }

    private void uploadCbExcelReportIfConfigured(Long enquiryId) throws Exception {
        List<RedashQuerySheetConfig> sheetConfigs = getRedashQuerySheetConfigsFromCbConfig();
        if (sheetConfigs == null || sheetConfigs.isEmpty()) {
            log.warn("No Redash query sheet configs found in CB config for enquiry ID: {}, skipping CB Excel report", enquiryId);
            return;
        }

        List<Long> queryIds = sheetConfigs.stream()
                .map(RedashQuerySheetConfig::getQueryId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        Map<Long, String> queryIdToSheetName = sheetConfigs.stream()
                .filter(config -> config.getQueryId() != null && config.getSheetName() != null)
                .collect(Collectors.toMap(RedashQuerySheetConfig::getQueryId, RedashQuerySheetConfig::getSheetName, (existing, replacement) -> existing));

        log.info("Processing CB report for enquiry ID: {}, fetching Excel from Redash for query IDs: {} with sheet names: {}", enquiryId, queryIds, queryIdToSheetName);

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

            Optional<UUID> leadIdentifierOpt = leadRepositoryWrapper.findLeadIdentifierByCbEnquiryId(enquiryId);
            if (leadIdentifierOpt.isEmpty()) {
                log.warn("No lead found for enquiry ID: {}, skipping CB report upload", enquiryId);
                return;
            }
            UUID leadIdentifier = leadIdentifierOpt.get();

            LeadDocumentCreateRequest leadDocRequest = LeadDocumentCreateRequest.builder()
                    .name(CB_REPORT_FILENAME)
                    .tags(List.of(SystemLeadDocumentsMaster.LEAD_CREDIT_DOCUMENTS_CRIF_REPORT))
                    .build();

            leadDocumentWriteService.createLeadDocument(leadIdentifier, excelBytes, CB_REPORT_FILENAME, EXCEL_CONTENT_TYPE, leadDocRequest);
            log.info("Successfully uploaded CB report to lead {} for enquiry ID: {}", leadIdentifier, enquiryId);
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
