package com.nivasafinance.features.lead.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadEligibilityWriteServiceImpl implements LeadEligibilityWriteService {

    private static final String ELIGIBILITY_CONFIG = "eligibility";
    private static final String BRE_ELIGIBILITY_TRIGGER_PROVIDER = "lead_bre_eligibility_trigger";
    private static final String SHOULD_EXECUTE_FIELD = "should_execute";
    private static final String BUREAU_REJECT_PROFILE_NAME = "bureau_reject";
    private static final String CREDIT_BUREAU_LOW_SCORE_REASON_CODE = "CREDIT_BUREAU_LOW_SCORE";
    private static final String REJECTED_BY_BRE_REMARKS = "REJECTED_BY_BRE";

    private final LeadBREResultWriteService leadBREResultWriteService;
    private final DataProviderExecutor dataProviderExecutor;
    private final LeadWriteService leadWriteService;
    private final ObjectMapper objectMapper;

    @Override
    public LeadBREResultExecuteResponse executeEligibility(UUID leadId) {
        UUID identifier = UUID.randomUUID();
        CompletableFuture<BREExecutionResponse> future = leadBREResultWriteService.executeBre(leadId, ELIGIBILITY_CONFIG, identifier);
        future.whenComplete((response, throwable) -> {
            if (throwable == null && response != null && response.getError() == null) {
                try {
                    evaluateBureauRejectOnEligibilityResult(leadId, objectMapper.writeValueAsString(response.getResponse()));
                } catch (Exception e) {
                    log.warn("Failed to evaluate bureau reject for lead {}", leadId, e);
                }
            }
        });
        return LeadBREResultExecuteResponse.builder().identifier(identifier).build();
    }

    private void evaluateBureauRejectOnEligibilityResult(UUID leadIdentifier, String output) {
        try {
            JsonNode root = objectMapper.readTree(output);
            String profileName = root.path("profile_match").path("profile_name").asText(null);
            if (BUREAU_REJECT_PROFILE_NAME.equals(profileName)) {
                leadWriteService.rejectLead(leadIdentifier, RejectLeadRequest.builder()
                        .reasonCode(CREDIT_BUREAU_LOW_SCORE_REASON_CODE)
                        .remarks(REJECTED_BY_BRE_REMARKS)
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to evaluate bureau reject for lead {}", leadIdentifier, e);
        }
    }

    @Override
    public void executeEligibilityOnStageTransition(UUID leadIdentifier) {
        Map<String, String> result = dataProviderExecutor.executeDataProvider(
                BRE_ELIGIBILITY_TRIGGER_PROVIDER,
                Map.of("leadIdentifier", leadIdentifier)
        );
        if (!Boolean.parseBoolean(result.get(SHOULD_EXECUTE_FIELD))) {
            log.info("BRE eligibility trigger skipped for lead {}", leadIdentifier);
            return;
        }
        executeEligibility(leadIdentifier);
    }
}
