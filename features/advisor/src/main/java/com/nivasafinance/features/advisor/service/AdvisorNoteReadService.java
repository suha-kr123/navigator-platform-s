package com.nivasafinance.features.advisor.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteResponse;

import java.util.UUID;

public interface AdvisorNoteReadService {

    PaginatedResponse<AdvisorNoteResponse> getAllAdvisorNotes(
            UUID advisorIdentifier, PaginationRequest paginationRequest);

    AdvisorNoteResponse getAdvisorNoteById(UUID advisorIdentifier, UUID noteIdentifier);
}

