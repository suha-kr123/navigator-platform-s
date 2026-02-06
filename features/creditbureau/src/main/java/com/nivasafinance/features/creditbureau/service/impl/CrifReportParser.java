package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.service.CreditBureauReportParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifReportParser implements CreditBureauReportParser {

    private final CrifSummaryService crifSummaryService;
    private final CrifAttributeService crifAttributeService;
    private final CrifTradelineService crifTradelineService;
    private final CrifCustomerEnquiryService crifCustomerEnquiryService;
    private final CrifDemographicVariationService crifDemographicVariationService;
    private final CrifTrendsService crifTrendsService;

    @Override
    public boolean parse(Long enquiryId, JsonNode creditBureauReportJson) {
        try {
            log.info("Parsing credit bureau report for enquiry ID: {}", enquiryId);

            JsonNode b2cReport = creditBureauReportJson.path("B2C-REPORT");
            if (b2cReport.isMissingNode()) {
                log.warn("B2C-REPORT not found in response for enquiry ID: {}", enquiryId);
                return false;
            }

            JsonNode reportData = b2cReport.path("REPORT-DATA");
            if (reportData.isMissingNode()) {
                log.warn("REPORT-DATA not found for enquiry ID: {}", enquiryId);
                return false;
            }

            JsonNode standardData = reportData.path("STANDARD-DATA");
            JsonNode accountsSummary = reportData.path("ACCOUNTS-SUMMARY");

            boolean dataFound = false;

            // Check for SCORE or summary data
            if (!standardData.isMissingNode() || !accountsSummary.isMissingNode()) {
                crifSummaryService.parseAndStore(enquiryId, reportData);
                // Check if summary data was actually found
                JsonNode scoreArray = standardData.path("SCORE");
                if (scoreArray.isArray() && scoreArray.size() > 0) {
                    dataFound = true;
                }
            }

            // Parse attributes from ACCOUNTS-SUMMARY (ADDITIONAL-SUMMARY, PERFORM-ATTRIBUTES)
            if (!accountsSummary.isMissingNode()) {
                crifAttributeService.parseAndStore(enquiryId, reportData);
            }

            if (!standardData.isMissingNode()) {
                // Check for TRADELINES
                JsonNode tradelines = standardData.path("TRADELINES");
                if (tradelines.isArray() && tradelines.size() > 0) {
                    crifTradelineService.parseAndStore(enquiryId, reportData);
                    dataFound = true;
                }

                // Check for INQUIRY-HISTORY
                JsonNode inquiryHistory = standardData.path("INQUIRY-HISTORY");
                if (inquiryHistory.isArray() && inquiryHistory.size() > 0) {
                    crifCustomerEnquiryService.parseAndStore(enquiryId, reportData);
                    dataFound = true;
                }

                // Check for DEMOGS or EMPLOYMENT-DETAILS
                JsonNode demogs = standardData.path("DEMOGS");
                JsonNode employmentDetails = standardData.path("EMPLOYMENT-DETAILS");
                if (!demogs.isMissingNode() || (employmentDetails.isArray() && employmentDetails.size() > 0)) {
                    crifDemographicVariationService.parseAndStore(enquiryId, reportData);
                    dataFound = true;
                }
            }

            // Check for TRENDS (directly under REPORT-DATA, not under STANDARD-DATA)
            JsonNode trends = reportData.path("TRENDS");
            if (!trends.isMissingNode()) {
                // Check if TRENDS has actual data (not just empty strings)
                String datesString = trends.path("DATES").asText("");
                String valuesString = trends.path("VALUES").asText("");
                if (!datesString.trim().isEmpty() && !valuesString.trim().isEmpty()) {
                    crifTrendsService.parseAndStore(enquiryId, reportData);
                    dataFound = true;
                }
            }

            // Also check accounts summary for meaningful data
            if (!accountsSummary.isMissingNode() && !dataFound) {
                // If accounts summary exists, consider it as data found
                dataFound = true;
            }

            if (dataFound) {
                log.info("Successfully parsed credit bureau report for enquiry ID: {} - data found", enquiryId);
            } else {
                log.info("Credit bureau report parsed for enquiry ID: {} - no meaningful data found (NO_HIT)", enquiryId);
            }

            return dataFound;

        } catch (Exception e) {
            log.error("Failed to parse credit bureau report for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse credit bureau report for enquiry ID: " + enquiryId, e);
        }
    }
}
