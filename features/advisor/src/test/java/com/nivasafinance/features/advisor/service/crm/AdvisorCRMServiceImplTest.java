package com.nivasafinance.features.advisor.service.crm;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
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
class AdvisorCRMServiceImplTest {

    @Mock
    private AdvisorReadService advisorReadService;

    @Mock
    private AdvisorWriteService advisorWriteService;

    @InjectMocks
    private AdvisorCRMServiceImpl advisorCRMService;

    private final UUID id = UUID.randomUUID();
    private final PaginationRequest page = new PaginationRequest(0, 10, "id", "ASC");

    @Test
    void delegatesToReadAndWriteServices() {
        when(advisorReadService.getAdvisorTemplate()).thenReturn(AdvisorTemplateResponse.builder().build());
        assertNotNull(advisorCRMService.getAdvisorTemplate());

        when(advisorReadService.getAllAdvisors(page, "n", "m")).thenReturn(new PaginatedResponse<>(List.of(), null));
        assertNotNull(advisorCRMService.getAllAdvisors(page, "n", "m"));

        when(advisorWriteService.createAdvisor(any())).thenReturn(id);
        assertEquals(id, advisorCRMService.createAdvisor(new CreateAdvisorRequest()));

        when(advisorReadService.getAdvisorByIdentifier(id)).thenReturn(new AdvisorResponse());
        assertNotNull(advisorCRMService.getAdvisorByIdentifier(id));

        when(advisorReadService.searchAdvisors(eq(page), any(AdvisorSearchRequest.class)))
                .thenReturn(new PaginatedResponse<>(List.of(), null));
        assertNotNull(advisorCRMService.searchAdvisors(page, new AdvisorSearchRequest()));

        advisorCRMService.updateAdvisor(id, new UpdateAdvisorRequest());
        verify(advisorWriteService).updateAdvisor(eq(id), any());

        advisorCRMService.updateQualificationDetails(id, new UpdateQualificationDetailsRequest());
        verify(advisorWriteService).updateQualificationDetails(eq(id), any());

        advisorCRMService.updateOccupationDetails(id, new UpdateOccupationDetailsRequest());
        verify(advisorWriteService).updateOccupationDetails(eq(id), any());

        advisorCRMService.updateSegmentationDetails(id, new UpdateSegmentationDetailsRequest());
        verify(advisorWriteService).updateSegmentationDetails(eq(id), any());

        advisorCRMService.rejectAdvisor(id, RejectAdvisorRequest.builder().build());
        verify(advisorWriteService).rejectAdvisor(eq(id), any());

        advisorCRMService.dormantAdvisor(id, new DormantAdvisorRequest());
        verify(advisorWriteService).dormantAdvisor(eq(id), any());

        advisorCRMService.activateAdvisor(id);
        verify(advisorWriteService).activateAdvisor(id);

        advisorCRMService.outOfGeoAdvisor(id, new OutOfGeoAdvisorRequest());
        verify(advisorWriteService).outOfGeoAdvisor(eq(id), any());

        when(advisorReadService.getAdvisorDashboard(eq(page), any(AdvisorDashboardFilters.class)))
                .thenReturn(new PaginatedResponse<>(List.of(), null));
        assertNotNull(advisorCRMService.getAdvisorDashboard(page, new AdvisorDashboardFilters()));

        when(advisorReadService.getMyAdvisors(page)).thenReturn(new PaginatedResponse<>(List.of(), null));
        assertNotNull(advisorCRMService.getMyAdvisors(page));

        when(advisorReadService.getAdvisorsByReferralCode("R", page)).thenReturn(new PaginatedResponse<>(List.of(), null));
        assertNotNull(advisorCRMService.getAdvisorsByReferralCode("R", page));
    }
}
