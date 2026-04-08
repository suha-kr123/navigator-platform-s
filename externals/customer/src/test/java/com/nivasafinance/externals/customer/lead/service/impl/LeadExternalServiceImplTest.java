package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadExternalServiceImplTest {

    private static final UUID LEAD_IDENTIFIER = UUID.randomUUID();

    @Mock
    private LeadWriteService leadWriteService;
    @Mock
    private LeadReadService leadReadService;
    @Mock
    private LeadContactReadService leadContactReadService;
    @Mock
    private LeadEligibilityWriteService leadEligibilityWriteService;
    @Mock
    private LeadEligibilityReadService leadEligibilityReadService;

    @InjectMocks
    private LeadExternalServiceImpl leadExternalService;

    @Test
    void patchLead_delegatesToLeadWriteService() {
        // Arrange
        PatchLeadRequest request = new PatchLeadRequest();

        // Act
        leadExternalService.patchLead(LEAD_IDENTIFIER, request);

        // Assert
        verify(leadWriteService).patchLead(LEAD_IDENTIFIER, request);
        verifyNoMoreInteractions(leadWriteService);
    }

    @Test
    void getLeadByIdentifier_delegatesToLeadReadService() {
        // Arrange
        LeadResponse expected = new LeadResponse();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        LeadResponse result = leadExternalService.getLeadByIdentifier(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadReadService");
        verify(leadReadService).getLeadByIdentifier(LEAD_IDENTIFIER);
    }

    @Test
    void getCurrentCustomerFormStep_delegatesToLeadReadService() {
        // Arrange
        CurrentCustomerFormStepResponse expected = new CurrentCustomerFormStepResponse();
        when(leadReadService.getCurrentCustomerFormStep(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        CurrentCustomerFormStepResponse result = leadExternalService.getCurrentCustomerFormStep(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadReadService");
        verify(leadReadService).getCurrentCustomerFormStep(LEAD_IDENTIFIER);
    }

    @Test
    void getContacts_delegatesToLeadContactReadService() {
        // Arrange
        List<LeadContactResponse> expected = List.of(new LeadContactResponse());
        when(leadContactReadService.getContacts(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        List<LeadContactResponse> result = leadExternalService.getContacts(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadContactReadService");
        verify(leadContactReadService).getContacts(LEAD_IDENTIFIER);
    }

    @Test
    void getPropertyDetails_delegatesToLeadReadService() {
        // Arrange
        PropertyDetailsResponse expected = new PropertyDetailsResponse();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        PropertyDetailsResponse result = leadExternalService.getPropertyDetails(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadReadService");
        verify(leadReadService).getPropertyDetails(LEAD_IDENTIFIER);
    }

    @Test
    void getIncomeObligationDetails_delegatesToLeadReadService() {
        // Arrange
        IncomeObligationDetailsResponse expected = new IncomeObligationDetailsResponse();
        when(leadReadService.getIncomeObligationDetails(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        IncomeObligationDetailsResponse result = leadExternalService.getIncomeObligationDetails(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadReadService");
        verify(leadReadService).getIncomeObligationDetails(LEAD_IDENTIFIER);
    }

    @Test
    void getDocumentChecklist_delegatesToLeadReadService() {
        // Arrange
        DocumentChecklistResponse expected = new DocumentChecklistResponse();
        when(leadReadService.getDocumentChecklist(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        DocumentChecklistResponse result = leadExternalService.getDocumentChecklist(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadReadService");
        verify(leadReadService).getDocumentChecklist(LEAD_IDENTIFIER);
    }

    @Test
    void getPreliminaryDetails_delegatesToLeadReadService() {
        // Arrange
        PreliminaryDetailsResponse expected = new PreliminaryDetailsResponse();
        when(leadReadService.getPreliminaryDetails(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        PreliminaryDetailsResponse result = leadExternalService.getPreliminaryDetails(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadReadService");
        verify(leadReadService).getPreliminaryDetails(LEAD_IDENTIFIER);
    }

    @Test
    void executeEligibility_delegatesToLeadEligibilityWriteService() {
        // Arrange
        LeadBREResultExecuteResponse expected = new LeadBREResultExecuteResponse();
        when(leadEligibilityWriteService.executeEligibility(LEAD_IDENTIFIER)).thenReturn(expected);

        // Act
        LeadBREResultExecuteResponse result = leadExternalService.executeEligibility(LEAD_IDENTIFIER);

        // Assert
        assertSame(expected, result, "Should return the response from leadEligibilityWriteService");
        verify(leadEligibilityWriteService).executeEligibility(LEAD_IDENTIFIER);
    }

    @Test
    void getLatestEligibility_whenPresent_returnsOptionalWithValue() {
        // Arrange
        LeadEligibilityResponse eligibility = new LeadEligibilityResponse();
        when(leadEligibilityReadService.getLatestEligibility(LEAD_IDENTIFIER)).thenReturn(Optional.of(eligibility));

        // Act
        Optional<LeadEligibilityResponse> result = leadExternalService.getLatestEligibility(LEAD_IDENTIFIER);

        // Assert
        assertTrue(result.isPresent(), "Should return a non-empty Optional");
        assertSame(eligibility, result.get(), "Should contain the eligibility from the service");
        verify(leadEligibilityReadService).getLatestEligibility(LEAD_IDENTIFIER);
    }

    @Test
    void getLatestEligibility_whenEmpty_returnsEmptyOptional() {
        // Arrange
        when(leadEligibilityReadService.getLatestEligibility(LEAD_IDENTIFIER)).thenReturn(Optional.empty());

        // Act
        Optional<LeadEligibilityResponse> result = leadExternalService.getLatestEligibility(LEAD_IDENTIFIER);

        // Assert
        assertTrue(result.isEmpty(), "Should return an empty Optional");
        verify(leadEligibilityReadService).getLatestEligibility(LEAD_IDENTIFIER);
    }
}
