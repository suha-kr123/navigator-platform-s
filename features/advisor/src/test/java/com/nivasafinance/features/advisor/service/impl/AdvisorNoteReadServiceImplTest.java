package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteResponse;
import com.nivasafinance.features.advisor.repository.AdvisorNoteRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorNoteReadServiceImplTest {

    @Mock
    private AdvisorNoteRepositoryWrapper advisorNoteRepositoryWrapper;

    @InjectMocks
    private AdvisorNoteReadServiceImpl advisorNoteReadService;

    private UUID advisorIdentifier;
    private UUID noteIdentifier;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        noteIdentifier = UUID.randomUUID();
        paginationRequest = new PaginationRequest(0, 20, "createdAt", "DESC");
    }

    @Test
    void getAllAdvisorNotes_success_returnsPaginatedResponse() {
        PaginatedResponse<AdvisorNoteResponse> expected = new PaginatedResponse<>(List.of(), null);
        when(advisorNoteRepositoryWrapper.findAllNotesByAdvisorIdentifier(advisorIdentifier, paginationRequest))
                .thenReturn(expected);

        PaginatedResponse<AdvisorNoteResponse> result =
                advisorNoteReadService.getAllAdvisorNotes(advisorIdentifier, paginationRequest);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(advisorNoteRepositoryWrapper).findAllNotesByAdvisorIdentifier(advisorIdentifier, paginationRequest);
    }

    @Test
    void getAdvisorNoteById_success_returnsNote() {
        AdvisorNoteResponse expected = new AdvisorNoteResponse();
        expected.setIdentifier(noteIdentifier);
        when(advisorNoteRepositoryWrapper.findNoteByAdvisorIdentifierAndNoteIdentifier(advisorIdentifier, noteIdentifier))
                .thenReturn(expected);

        AdvisorNoteResponse result = advisorNoteReadService.getAdvisorNoteById(advisorIdentifier, noteIdentifier);

        assertNotNull(result);
        assertEquals(noteIdentifier, result.getIdentifier());
        verify(advisorNoteRepositoryWrapper).findNoteByAdvisorIdentifierAndNoteIdentifier(advisorIdentifier, noteIdentifier);
    }
}
