package com.nivasafinance.features.advisor.service;

import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateResponse;
import com.nivasafinance.features.advisor.dto.AdvisorNoteUpdateRequest;

import java.util.UUID;

public interface AdvisorNoteWriteService {

    AdvisorNoteCreateResponse createAdvisorNote(UUID advisorIdentifier, AdvisorNoteCreateRequest request);

  
    void updateAdvisorNote(UUID advisorIdentifier, UUID noteIdentifier, AdvisorNoteUpdateRequest request);

    
    void deleteAdvisorNote(UUID advisorIdentifier, UUID noteIdentifier);
}

