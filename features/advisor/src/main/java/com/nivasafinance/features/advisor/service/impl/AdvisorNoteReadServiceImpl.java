package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteResponse;
import com.nivasafinance.features.advisor.repository.AdvisorNoteRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorNoteReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdvisorNoteReadServiceImpl implements AdvisorNoteReadService {

    private final AdvisorNoteRepositoryWrapper advisorNoteRepositoryWrapper;

    @Override
    public PaginatedResponse<AdvisorNoteResponse> getAllAdvisorNotes(
            UUID advisorIdentifier, PaginationRequest paginationRequest) {
        return advisorNoteRepositoryWrapper.findAllNotesByAdvisorIdentifier(
                advisorIdentifier, paginationRequest);
    }

    @Override
    public AdvisorNoteResponse getAdvisorNoteById(UUID advisorIdentifier, UUID noteIdentifier) {
        return advisorNoteRepositoryWrapper.findNoteByAdvisorIdentifierAndNoteIdentifier(
                advisorIdentifier, noteIdentifier);
    }
}

