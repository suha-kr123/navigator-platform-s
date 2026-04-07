package com.nivasafinance.features.lead.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.leadbre.entity.LeadBREResult;
import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import com.nivasafinance.features.leadbre.repository.LeadBREResultRepositoryWrapper;
import com.nivasafinance.features.bre.service.BREExecutionService;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadBREResultWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadBREResultWriteServiceImpl implements LeadBREResultWriteService {

    private static final String ELIGIBILITY_CONFIG = "eligibility";
    private static final String LOG_PERSIST_FAILED = "Failed to persist lead BRE result after execution identifier={}";

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadBREResultRepositoryWrapper leadBREResultRepositoryWrapper;
    private final BREExecutionService breExecutionService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public LeadBREResultExecuteResponse executeBre(UUID leadId, String config) {
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);

        LeadBREResult pending = LeadBREResult.builder()
                .leadId(lead.getId())
                .identifier(UUID.randomUUID())
                .configName(config)
                .status(LeadBREResultStatus.IN_PROGRESS)
                .build();
        LeadBREResult saved = leadBREResultRepositoryWrapper.saveWithException(pending);
        UUID resultIdentifier = saved.getIdentifier();

        CompletableFuture<BREExecutionResponse> executionFuture =
                breExecutionService.execute(config, BREExecutionRequest.builder()
                        .customData("{\"leadId : "+ leadId +"\"}")
                        .params(Map.of("lead_id", lead.getId()))
                        .build());

        executionFuture.whenComplete((breResponse, throwable) -> {
            try {
                LeadBREResult toUpdate = leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(
                        lead.getId(), resultIdentifier);
                if (throwable != null) {
                    String message = throwable.getCause() != null
                            ? throwable.getCause().getMessage()
                            : throwable.getMessage();
                    toUpdate.setOutput(toErrorJson(message));
                    toUpdate.setStatus(LeadBREResultStatus.FAILED);
                } else if (breResponse != null) {
                    toUpdate.setInput(mapToJson(breResponse.getRequest()));
                    if (breResponse.getError() != null) {
                        toUpdate.setOutput(toErrorJson(breResponse.getError()));
                        toUpdate.setStatus(LeadBREResultStatus.FAILED);
                    } else {
                        toUpdate.setOutput(mapToJson(breResponse.getResponse()));
                        toUpdate.setStatus(LeadBREResultStatus.SUCCESS);
                    }
                } else {
                    toUpdate.setStatus(LeadBREResultStatus.FAILED);
                }
                leadBREResultRepositoryWrapper.saveWithException(toUpdate);
                updateLeadBREExecution(lead.getId(), config, toUpdate.getStatus().name(), resultIdentifier);
            } catch (Exception e) {
                log.error(LOG_PERSIST_FAILED, resultIdentifier, e);
            }
        });

        return LeadBREResultExecuteResponse.builder()
                .identifier(resultIdentifier)
                .build();
    }

    private void updateLeadBREExecution(Long leadDbId, String config, String status, UUID resultIdentifier) {
        try {
            var lead = leadRepositoryWrapper.findByIdWithException(leadDbId);
            Lead.BREExecutionSummary summary = Lead.BREExecutionSummary.builder()
                    .status(status)
                    .resultIdentifier(resultIdentifier)
                    .build();

            Lead.BREExecutions executions = lead.getBreExecutions() != null
                    ? lead.getBreExecutions()
                    : new Lead.BREExecutions();

            if (ELIGIBILITY_CONFIG.equals(config)) {
                executions.setEligibility(summary);
            }

            lead.setBreExecutions(executions);
            leadRepositoryWrapper.saveWithException(lead);
        } catch (Exception e) {
            log.error("Failed to update BRE execution summary on lead for config={}, resultIdentifier={}", config, resultIdentifier, e);
        }
    }

    private String mapToJson(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize map for lead BRE result", e);
            return null;
        }
    }

    private String toErrorJson(String message) {
        try {
            return objectMapper.writeValueAsString(Map.of("error", message != null ? message : ""));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize error for lead BRE result", e);
            return null;
        }
    }
}
