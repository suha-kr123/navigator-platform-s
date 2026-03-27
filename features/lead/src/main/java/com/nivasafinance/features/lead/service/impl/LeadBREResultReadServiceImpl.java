package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.leadbre.entity.LeadBREResult;
import com.nivasafinance.features.leadbre.repository.LeadBREResultRepositoryWrapper;
import com.nivasafinance.features.lead.dto.LeadBREResultResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadBREResultReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadBREResultReadServiceImpl implements LeadBREResultReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadBREResultRepositoryWrapper leadBREResultRepositoryWrapper;

    @Override
    public List<LeadBREResultResponse> getResults(UUID leadId) {
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        return leadBREResultRepositoryWrapper.findByLeadIdWithException(lead.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LeadBREResultResponse> getResults(UUID leadId, String config) {
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        return leadBREResultRepositoryWrapper.findByLeadIdAndConfigNameWithException(lead.getId(), config).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LeadBREResultResponse getResultByIdentifier(UUID leadId, UUID identifier) {
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadId);
        LeadBREResult entity = leadBREResultRepositoryWrapper.findByLeadIdAndIdentifierWithException(
                lead.getId(), identifier);
        return toResponse(entity);
    }

    private LeadBREResultResponse toResponse(LeadBREResult entity) {
        return LeadBREResultResponse.builder()
                .identifier(entity.getIdentifier())
                .status(entity.getStatus())
                .input(entity.getInput())
                .output(entity.getOutput())
                .build();
    }
}
