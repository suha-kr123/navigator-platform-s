package com.nivasafinance.features.advisoractivity.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisoractivity.dto.AdvisorActivityResponse;
import com.nivasafinance.features.advisoractivity.repository.AdvisorActivityRepositoryWrapper;
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
class AdvisorActivityReadServiceImplTest {

    @Mock
    private AdvisorActivityRepositoryWrapper advisorActivityRepositoryWrapper;

    @Mock
    private AdvisorReadService advisorReadService;

    @InjectMocks
    private AdvisorActivityReadServiceImpl advisorActivityReadService;

    private UUID advisorIdentifier;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        paginationRequest = new PaginationRequest(0, 20, "createdAt", "DESC");
    }

    @Test
    void getActivities_success_delegatesToWrapper() {
        AdvisorResponse advisorResponse = new AdvisorResponse();
        advisorResponse.setId(1L);
        PaginatedResponse<AdvisorActivityResponse> expected = new PaginatedResponse<>(List.of(), null);

        when(advisorReadService.getAdvisorByIdentifier(advisorIdentifier)).thenReturn(advisorResponse);
        when(advisorActivityRepositoryWrapper.findAllByAdvisorIdWithException(1L, paginationRequest)).thenReturn(expected);

        PaginatedResponse<AdvisorActivityResponse> result =
                advisorActivityReadService.getActivities(advisorIdentifier, paginationRequest);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(advisorReadService).getAdvisorByIdentifier(advisorIdentifier);
        verify(advisorActivityRepositoryWrapper).findAllByAdvisorIdWithException(1L, paginationRequest);
    }
}
