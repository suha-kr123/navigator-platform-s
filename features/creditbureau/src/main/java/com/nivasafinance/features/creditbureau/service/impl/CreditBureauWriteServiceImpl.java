package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.SystemEntities;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.CbReportStoredEventPayload;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.repository.CreditBureauRepositoryWrapper;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.service.CreditBureauWriteService;
import com.nivasafinance.features.creditbureau.service.CreditBureauReportParser;
import com.nivasafinance.features.creditbureau.service.CreditBureauReportParserFactory;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.integrations.framework.core.service.ThirdPartyServiceConfigReadService;
import com.nivasafinance.services.creditbureau.dto.CreditBureauProviderResponse;
import org.springframework.context.MessageSource;
import com.nivasafinance.services.creditbureau.CreditBureauHandler;
import com.nivasafinance.services.creditbureau.dto.CreditBureauPersonData;
import com.nivasafinance.services.creditbureau.dto.PullEnquiryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditBureauWriteServiceImpl implements CreditBureauWriteService {

    private final CreditBureauRepositoryWrapper creditBureauRepositoryWrapper;
    private final ServiceFactory<CreditBureauHandler> serviceFactory;
    private final ObjectMapper objectMapper;
    private final ThirdPartyServiceConfigReadService thirdPartyServiceConfigReadService;
    private final MessageSource messageSource;
    private final CreditBureauReportParserFactory creditBureauReportParserFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public CreditBureauEnquiryResponse initiateEnquiry(Long personId) {
        CreditBureauEnquiry creditBureauEnquiry = new CreditBureauEnquiry();
        creditBureauEnquiry.setStatus(CreditBureauEnquiryStatus.INITIATED);

        CreditBureauEnquiry savedEnquiry = creditBureauRepositoryWrapper.saveWithException(creditBureauEnquiry);

        return CreditBureauEnquiryResponse.toCbEnquiryResponse(savedEnquiry);
    }

    @Override
    public void linkConsentToEnquiry(Long enquiryId, Long consentId) {
        CreditBureauEnquiry enquiry = creditBureauRepositoryWrapper.findByIdWithException(enquiryId);
        enquiry.setConsentId(consentId);
        creditBureauRepositoryWrapper.saveWithException(enquiry);
    }

    @Override
    public CompletableFuture<CreditBureauEnquiryResponse> executeCreditBureauFlowAsync(CreditBureauEnquiry enquiry, Long personId, CreditBureauPersonData personData) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting async credit bureau flow execution for enquiry ID: {}, person ID: {}", enquiry.getId(), personId);

            if (personData == null) {
                throw new IllegalArgumentException("Person data cannot be null");
            }

            if (enquiry == null) {
                throw new IllegalArgumentException("Enquiry cannot be null");
            }

            try {
                BusinessContext businessContext = new BusinessContext(
                    SystemEntities.LEAD.name(),
                    null,
                    "Credit bureau enquiry pull"
                );

                var runConfig = thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU);
                String providerName = runConfig.getPrimaryConfig().getProvider();

                CreditBureauHandler handler = serviceFactory.getHandler(ThirdPartyServiceList.CREDIT_BUREAU);
                PullEnquiryRequest pullRequest = PullEnquiryRequest.builder()
                        .personId(personId)
                        .enquiryId(enquiry.getId())
                        .enquiryIdentifier(enquiry.getIdentifier().toString())
                        .personData(personData)
                        .build();

                CreditBureauProviderResponse response =
                    handler.pullEnquiry(pullRequest, businessContext, providerName);

                // Reload entity to get latest version
                CreditBureauEnquiry enquiryToUpdate = creditBureauRepositoryWrapper.findByIdWithException(enquiry.getId());
                updateEnquiryWithResult(enquiryToUpdate, response);
                creditBureauRepositoryWrapper.saveWithException(enquiryToUpdate);

                CreditBureauEnquiryStatus enquiryStatus = CreditBureauEnquiryStatus.fromCreditBureauEnquiryStatus(response.getStatus());
                log.info("Async credit bureau flow execution completed for enquiry ID: {}, status: {}", enquiry.getId(), enquiryStatus);
                // Reload to get latest state after update
                CreditBureauEnquiry updatedEnquiry = creditBureauRepositoryWrapper.findByIdWithException(enquiry.getId());
                return CreditBureauEnquiryResponse.toCbEnquiryResponse(updatedEnquiry);

            } catch (Exception e) {
                log.error("Async credit bureau flow execution failed for enquiry ID: {}", enquiry.getId(), e);
                Long enquiryId = enquiry.getId();
                if (enquiryId != null) {
                    try {
                        CreditBureauEnquiry enquiryToUpdate = creditBureauRepositoryWrapper.findByIdWithException(enquiryId);
                        enquiryToUpdate.setStatus(CreditBureauEnquiryStatus.FAILED);
                        enquiryToUpdate.setError(e.getMessage());
                        creditBureauRepositoryWrapper.saveWithException(enquiryToUpdate);
                    } catch (Exception saveException) {
                        log.error("Failed to save error status for enquiry ID: {}", enquiryId, saveException);
                    }
                }
                throw CreditBureauExceptionFactory.executeFlowFailed(enquiry.getId(), e, messageSource);
            }
        });
    }

    private void updateEnquiryWithResult(CreditBureauEnquiry enquiry, CreditBureauProviderResponse response) {
        enquiry.setStatus(CreditBureauEnquiryStatus.fromCreditBureauEnquiryStatus(response.getStatus()));
        enquiry.setReportId(response.getReportId());
        
        // Convert Map to ReportDetails
        CreditBureauEnquiry.ReportDetails reportDetails = null;
        if (response.getReportDetails() != null && !response.getReportDetails().isEmpty()) {
            reportDetails = CreditBureauEnquiry.ReportDetails.builder()
                    .reportReceivedTime(LocalDateTime.now()) // Set current time when report is received
                    .build();
        }
        enquiry.setReportDetails(reportDetails);
        enquiry.setError(response.getError());

        // Store only Stage-I initial request and credit bureau report JSON
        String requestJson = response.getStage1Request() != null && !response.getStage1Request().trim().isEmpty() 
            ? response.getStage1Request() 
            : "";
        String creditBureauReportJson = response.getCreditBureauReportJson() != null && !response.getCreditBureauReportJson().trim().isEmpty() 
            ? response.getCreditBureauReportJson() 
            : "";

        log.debug("Updating enquiry ID: {} - requestJson length: {}, creditBureauReportJson length: {}", 
            enquiry.getId(), requestJson.length(), creditBureauReportJson.length());
        
        enquiry.setRequestJson(requestJson);
        enquiry.setResponseJson(creditBureauReportJson);
        
        // Set provider name
        String providerName = null;
        try {
            var runConfig = thirdPartyServiceConfigReadService.findByService(ThirdPartyServiceList.CREDIT_BUREAU);
            providerName = runConfig.getPrimaryConfig().getProvider();
            enquiry.setProvider(providerName);
        } catch (Exception e) {
            log.warn("Failed to get provider name for enquiry ID: {}", enquiry.getId(), e);
        }
        
        // Parse and store structured data ONLY if status is SUCCESS
        if (CreditBureauEnquiryStatus.SUCCESS == enquiry.getStatus() 
            && creditBureauReportJson != null && !creditBureauReportJson.trim().isEmpty()) {
            try {
                JsonNode reportJsonNode = objectMapper.readTree(creditBureauReportJson);
                CreditBureauReportParser parser = creditBureauReportParserFactory.getParser(providerName);
                boolean dataFound = parser.parse(enquiry.getId(), reportJsonNode);

                // Update status based on whether data was found
                if (!dataFound) {
                    enquiry.setStatus(CreditBureauEnquiryStatus.NO_HIT);
                    log.info("No meaningful data found in credit bureau report for enquiry ID: {}, status updated to NO_HIT", enquiry.getId());
                } else {
                    // Publish event so Lead module can fetch Redash Excel and upload to lead
                    applicationEventPublisher.publishEvent(
                            new SystemEvent<>(BusinessEvent.CB_REPORT_STORED.toString(),
                                    CbReportStoredEventPayload.builder().enquiryId(enquiry.getId()).build()));
                }
            } catch (Exception e) {
                log.error("Failed to parse and store credit bureau report for enquiry ID: {}",
                        enquiry.getId(), e);
                // Don't fail the entire flow, just log the error
            }
        }
    }
}

