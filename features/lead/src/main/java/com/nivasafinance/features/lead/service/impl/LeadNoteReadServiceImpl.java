package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadNoteReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class LeadNoteReadServiceImpl implements LeadNoteReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;

    @Override
    public PaginatedResponse<LeadNoteResponse> getAllLeadNotes(
            UUID leadIdentifier, PaginationRequest paginationRequest) {
        return leadRepositoryWrapper.findAllNotesByLeadIdentifier(
                leadIdentifier, paginationRequest);
    }

    @Override
    public LeadNoteResponse getLeadNoteById(UUID leadIdentifier, UUID noteIdentifier) {
        return leadRepositoryWrapper.findNoteByLeadIdentifierAndNoteIdentifier(
                leadIdentifier, noteIdentifier);
    }
}

