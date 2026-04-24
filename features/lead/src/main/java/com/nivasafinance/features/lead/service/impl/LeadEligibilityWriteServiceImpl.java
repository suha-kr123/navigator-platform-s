package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadEligibilityWriteServiceImpl implements LeadEligibilityWriteService {

    private static final String ELIGIBILITY_CONFIG = "eligibility";
    private static final String BRE_ELIGIBILITY_TRIGGER_PROVIDER = "lead_bre_eligibility_trigger";
    private static final String SHOULD_EXECUTE_FIELD = "should_execute";

    private final LeadBREResultWriteService leadBREResultWriteService;
    private final DataProviderExecutor dataProviderExecutor;

    @Override
    public LeadBREResultExecuteResponse executeEligibility(UUID leadId) {
        return leadBREResultWriteService.executeBre(leadId, ELIGIBILITY_CONFIG);
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
        leadBREResultWriteService.executeBre(leadIdentifier, ELIGIBILITY_CONFIG);
    }
}
