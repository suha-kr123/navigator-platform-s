package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class LeadReadServiceImpl implements LeadReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;

    public LeadReadServiceImpl(LeadRepositoryWrapper leadRepositoryWrapper) {
        this.leadRepositoryWrapper = leadRepositoryWrapper;
    }

    @Override
    public LeadResponse getLeadByIdentifier(UUID leadIdentifier) {
        return leadRepositoryWrapper.findLeadResponseByIdentifierWithException(leadIdentifier);
    }
}
