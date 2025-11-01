package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.LeadNoteResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadNoteReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class LeadNoteReadServiceImpl implements LeadNoteReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;

    @Override
    public List<LeadNoteResponse> getAllLeadNotes(UUID leadIdentifier) {
        return leadRepositoryWrapper.findAllNotesByLeadIdentifier(leadIdentifier);
    }

    @Override
    public LeadNoteResponse getLeadNoteById(UUID leadIdentifier, UUID noteIdentifier) {
        return leadRepositoryWrapper.findNoteByLeadIdentifierAndNoteIdentifier(
                leadIdentifier, noteIdentifier);
    }
}

