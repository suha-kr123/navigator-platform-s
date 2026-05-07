package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.externals.customer.lead.dto.LeadSearchMinimalResponse;
import com.nivasafinance.externals.customer.lead.dto.ScheduleVisitRequest;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.externals.customer.lead.dto.LeadEligibilityEvaluateResponse;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.master.location.service.LocationMasterService;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.task.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.service.TaskReadService;
import com.nivasafinance.features.task.service.TaskWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    @Mock
    private LeadStageHistoryWriteService leadStageHistoryWriteService;
    @Mock
    private LocationMasterService locationMasterService;
    @Mock
    private TaskWriteService taskWriteService;
    @Mock
    private TaskReadService taskReadService;

    @InjectMocks
    private LeadExternalServiceImpl leadExternalService;

    @Test
    void createLead_whenLeadAlreadyExists_returnsExistingLeadIdentifier() {
        // Arrange
        CreateLeadRequest.MobileNumberDetails phone = new CreateLeadRequest.MobileNumberDetails("9876543210", false);
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(phone);
        LeadBasicResponse existing = LeadBasicResponse.builder().leadIdentifier(LEAD_IDENTIFIER).build();
        when(leadReadService.findLeadByPhoneNumber("9876543210")).thenReturn(Optional.of(existing));

        // Act
        CreateLeadResponse result = leadExternalService.createLead(request);

        // Assert
        assertEquals(LEAD_IDENTIFIER, result.getLeadIdentifier(), "Should return existing lead identifier");
        verify(leadWriteService, never()).createLead(any());
    }

    @Test
    void createLead_whenLeadDoesNotExist_delegatesToLeadWriteService() {
        // Arrange
        CreateLeadRequest.MobileNumberDetails phone = new CreateLeadRequest.MobileNumberDetails("9876543210", false);
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(phone);
        CreateLeadResponse expected = CreateLeadResponse.builder().leadIdentifier(LEAD_IDENTIFIER).build();
        when(leadReadService.findLeadByPhoneNumber("9876543210")).thenReturn(Optional.empty());
        when(leadWriteService.createLead(request)).thenReturn(expected);

        // Act
        CreateLeadResponse result = leadExternalService.createLead(request);

        // Assert
        assertSame(expected, result, "Should return response from leadWriteService");
        verify(leadWriteService).createLead(request);
    }

    @Test
    void createLead_whenPhoneNumberIsNull_skipsLookupAndCreatesLead() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        // phoneNumber is null
        CreateLeadResponse expected = CreateLeadResponse.builder().leadIdentifier(LEAD_IDENTIFIER).build();
        when(leadWriteService.createLead(request)).thenReturn(expected);

        // Act
        CreateLeadResponse result = leadExternalService.createLead(request);

        // Assert
        assertSame(expected, result, "Should delegate to leadWriteService without a lookup");
        verify(leadReadService, never()).findLeadByPhoneNumber(any());
        verify(leadWriteService).createLead(request);
    }

    @Test
    void createLead_whenMobileNumberIsNull_skipsLookupAndCreatesLead() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails(null, false));
        CreateLeadResponse expected = CreateLeadResponse.builder().leadIdentifier(LEAD_IDENTIFIER).build();
        when(leadWriteService.createLead(request)).thenReturn(expected);

        // Act
        CreateLeadResponse result = leadExternalService.createLead(request);

        // Assert
        assertSame(expected, result, "Should delegate to leadWriteService without a lookup");
        verify(leadReadService, never()).findLeadByPhoneNumber(any());
        verify(leadWriteService).createLead(request);
    }

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

    @Test
    void transitionToExpertScreening_createsStageEntryAndReturnsResponse() {
        // Arrange
        LeadStageHistory stageHistory = LeadStageHistory.builder()
                .leadId(1L)
                .stageKey("Expert Screening")
                .movedBy("system")
                .enteredAt(LocalDateTime.now())
                .build();
        when(leadStageHistoryWriteService.createStageEntry(eq(LEAD_IDENTIFIER), any(StageTransitionRequest.class)))
                .thenReturn(stageHistory);

        // Act
        LeadStageHistoryResponse result = leadExternalService.transitionToExpertScreening(LEAD_IDENTIFIER);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals("Expert Screening", result.getStageKey(), "Stage key should be Expert Screening");
        verify(leadStageHistoryWriteService).createStageEntry(eq(LEAD_IDENTIFIER),
                argThat(req -> "Expert Screening".equals(req.getStageKey())));
    }

    // ==================== evaluateEligibility() Tests ====================

    @Test
    void evaluateEligibility_whenPropertyDetailsIsNull_rejectsLeadAndReturnsStatus() {
        // Arrange
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(null);
        LeadResponse eligibilityLead = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .status(LeadStatus.ACTIVE)
                .build();
        LeadBasicResponse rejectedLead = LeadBasicResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER).status(LeadStatus.REJECTED).build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(eligibilityLead);
        when(leadReadService.getLeadBasicByIdentifier(LEAD_IDENTIFIER)).thenReturn(rejectedLead);

        // Act
        LeadEligibilityEvaluateResponse result = leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService).rejectLead(eq(LEAD_IDENTIFIER),
                argThat(r -> "NOT_IN_SERVICABLE_LOCATION".equals(r.getReasonCode())));
        assertEquals(LEAD_IDENTIFIER, result.getLeadIdentifier());
        assertEquals(LeadStatus.REJECTED, result.getLeadStatus());
    }

    @Test
    void evaluateEligibility_whenAddressIsNull_rejectsLead() {
        // Arrange
        PropertyDetailsResponse propertyDetails = PropertyDetailsResponse.builder().address(null).build();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(propertyDetails);
        LeadResponse eligibilityLead = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .status(LeadStatus.ACTIVE)
                .build();
        LeadBasicResponse lead = LeadBasicResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER).status(LeadStatus.REJECTED).build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(eligibilityLead);
        when(leadReadService.getLeadBasicByIdentifier(LEAD_IDENTIFIER)).thenReturn(lead);

        // Act
        leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService).rejectLead(eq(LEAD_IDENTIFIER), any());
    }

    @Test
    void evaluateEligibility_whenDistrictCodeIsBlank_rejectsLead() {
        // Arrange
        AddressData address = AddressData.builder().districtCode("  ").build();
        PropertyDetailsResponse propertyDetails = PropertyDetailsResponse.builder().address(address).build();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(propertyDetails);
        LeadResponse eligibilityLead = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .status(LeadStatus.ACTIVE)
                .build();
        LeadBasicResponse lead = LeadBasicResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER).status(LeadStatus.REJECTED).build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(eligibilityLead);
        when(leadReadService.getLeadBasicByIdentifier(LEAD_IDENTIFIER)).thenReturn(lead);

        // Act
        leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService).rejectLead(eq(LEAD_IDENTIFIER), any());
        verify(locationMasterService, never()).isDistrictServiceable(any());
    }

    @Test
    void evaluateEligibility_whenDistrictNotServiceable_rejectsLead() {
        // Arrange
        AddressData address = AddressData.builder().districtCode("BLR").build();
        PropertyDetailsResponse propertyDetails = PropertyDetailsResponse.builder().address(address).build();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(propertyDetails);
        when(locationMasterService.isDistrictServiceable("BLR")).thenReturn(false);
        LeadResponse eligibilityLead = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .status(LeadStatus.ACTIVE)
                .build();
        LeadBasicResponse lead = LeadBasicResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER).status(LeadStatus.REJECTED).build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(eligibilityLead);
        when(leadReadService.getLeadBasicByIdentifier(LEAD_IDENTIFIER)).thenReturn(lead);

        // Act
        leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService).rejectLead(eq(LEAD_IDENTIFIER),
                argThat(r -> "NOT_IN_SERVICABLE_LOCATION".equals(r.getReasonCode())));
    }

    @Test
    void evaluateEligibility_whenPersonalLoanAmountIsBelowThreshold_rejectsLeadForPersonalLoan() {
        // Arrange
        AddressData address = AddressData.builder().districtCode("BLR").build();
        PropertyDetailsResponse propertyDetails = PropertyDetailsResponse.builder().address(address).build();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(propertyDetails);
        LeadResponse eligibilityLead = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .productCode("PERSONAL_LOAN")
                .requestedAmount(BigDecimal.valueOf(399999))
                .status(LeadStatus.ACTIVE)
                .build();
        LeadBasicResponse rejectedLead = LeadBasicResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .productCode("PERSONAL_LOAN")
                .requestedAmount(BigDecimal.valueOf(399999))
                .status(LeadStatus.REJECTED)
                .build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(eligibilityLead);
        when(leadReadService.getLeadBasicByIdentifier(LEAD_IDENTIFIER)).thenReturn(rejectedLead);

        // Act
        LeadEligibilityEvaluateResponse result = leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService).rejectLead(eq(LEAD_IDENTIFIER),
                argThat(r -> "PERSONAL_LOAN".equals(r.getReasonCode())));
        verify(locationMasterService, never()).isDistrictServiceable(any());
        assertEquals(LeadStatus.REJECTED, result.getLeadStatus());
    }

    @Test
    void evaluateEligibility_whenPersonalLoanAmountIsAtThreshold_doesNotRejectForPersonalLoan() {
        // Arrange
        AddressData address = AddressData.builder().districtCode("BLR").build();
        PropertyDetailsResponse propertyDetails = PropertyDetailsResponse.builder().address(address).build();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(propertyDetails);
        when(locationMasterService.isDistrictServiceable("BLR")).thenReturn(true);
        LeadResponse lead = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .productCode("PL")
                .requestedAmount(BigDecimal.valueOf(500000))
                .status(LeadStatus.ACTIVE)
                .build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(lead);

        // Act
        LeadEligibilityEvaluateResponse result = leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService, never()).rejectLead(any(), any());
        verify(locationMasterService).isDistrictServiceable("BLR");
        assertEquals(LEAD_IDENTIFIER, result.getLeadIdentifier());
        assertEquals(LeadStatus.ACTIVE, result.getLeadStatus());
    }

    @Test
    void evaluateEligibility_whenDistrictIsServiceable_doesNotRejectAndReturnsStatus() {
        // Arrange
        AddressData address = AddressData.builder().districtCode("BLR").build();
        PropertyDetailsResponse propertyDetails = PropertyDetailsResponse.builder().address(address).build();
        when(leadReadService.getPropertyDetails(LEAD_IDENTIFIER)).thenReturn(propertyDetails);
        when(locationMasterService.isDistrictServiceable("BLR")).thenReturn(true);
        LeadResponse leadResponse = LeadResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .status(LeadStatus.ACTIVE)
                .build();
        when(leadReadService.getLeadByIdentifier(LEAD_IDENTIFIER)).thenReturn(leadResponse);

        // Act
        LeadEligibilityEvaluateResponse result = leadExternalService.evaluateEligibility(LEAD_IDENTIFIER);

        // Assert
        verify(leadWriteService, never()).rejectLead(any(), any());
        assertEquals(LEAD_IDENTIFIER, result.getLeadIdentifier());
        assertEquals(LeadStatus.ACTIVE, result.getLeadStatus());
    }

    @Test
    void getVisitTasks_returnsOnlyVisitCustomerTasks() {
        // Arrange
        TaskResponse visitTask = TaskResponse.builder()
                .taskIdentifier(UUID.randomUUID())
                .taskConfigKey("VISIT_CUSTOMER")
                .dueAt(LocalDateTime.of(2026, 4, 10, 9, 0))
                .build();
        TaskResponse otherTask = TaskResponse.builder()
                .taskIdentifier(UUID.randomUUID())
                .taskConfigKey("SOME_OTHER_TASK")
                .build();
        when(taskReadService.findAllTasksForLead(LEAD_IDENTIFIER, true))
                .thenReturn(List.of(visitTask, otherTask));

        // Act
        List<TaskResponse> result = leadExternalService.getVisitTasks(LEAD_IDENTIFIER);

        // Assert
        assertEquals(1, result.size(), "Should return only VISIT_CUSTOMER tasks");
        assertEquals("VISIT_CUSTOMER", result.get(0).getTaskConfigKey());
        verify(taskReadService).findAllTasksForLead(LEAD_IDENTIFIER, true);
    }

    @Test
    void getVisitTasks_whenNoVisitTasksExist_returnsEmptyList() {
        // Arrange
        when(taskReadService.findAllTasksForLead(LEAD_IDENTIFIER, true)).thenReturn(List.of());

        // Act
        List<TaskResponse> result = leadExternalService.getVisitTasks(LEAD_IDENTIFIER);

        // Assert
        assertTrue(result.isEmpty(), "Should return an empty list when no tasks exist");
    }

    @Test
    void scheduleVisit_createsAdhocTaskWithCorrectDetails() {
        // Arrange
        LocalDate visitDate = LocalDate.of(2026, 4, 10);
        LocalTime visitTime = LocalTime.of(9, 0);
        ScheduleVisitRequest request = ScheduleVisitRequest.builder()
                .visitDate(visitDate)
                .visitTime(visitTime)
                .build();

        // Act
        leadExternalService.scheduleVisit(LEAD_IDENTIFIER, request);

        // Assert
        verify(taskWriteService).createAdhocTask(argThat(task ->
                "VISIT_CUSTOMER".equals(task.getTaskConfigKey())
                && task.getAssignedTo() == null
                && visitDate.atTime(visitTime).equals(task.getDueAt())
                && LEAD_IDENTIFIER.equals(task.getTaskDetails().getEntityId())
                && EntityType.LEAD.equals(task.getTaskDetails().getEntityType())
        ));
    }

    @Test
    void searchLeads_mapsLeadSearchResponseToMinimalResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadSearchRequest searchRequest = new LeadSearchRequest();
        LeadSearchResponse searchResponse = LeadSearchResponse.builder()
                .leadIdentifier(LEAD_IDENTIFIER)
                .primaryPersonName("John Doe")
                .status(LeadStatus.ACTIVE)
                .subStatus(LeadSubStatus.ONHOLD)
                .build();
        PaginatedResponse<LeadSearchResponse> serviceResult = PaginatedResponse.<LeadSearchResponse>builder()
                .content(List.of(searchResponse))
                .pagination(PaginationInfo.builder().totalElements(1L).build())
                .build();
        when(leadReadService.searchLeads(paginationRequest, searchRequest)).thenReturn(serviceResult);

        // Act
        PaginatedResponse<LeadSearchMinimalResponse> result =
                leadExternalService.searchLeads(paginationRequest, searchRequest);

        // Assert
        assertEquals(1, result.getContent().size(), "Should have one result");
        LeadSearchMinimalResponse minimal = result.getContent().get(0);
        assertEquals(LEAD_IDENTIFIER, minimal.getLeadIdentifier(), "Lead identifier should be mapped");
        assertEquals("John Doe", minimal.getPrimaryPersonName(), "Primary person name should be mapped");
        assertEquals(LeadStatus.ACTIVE, minimal.getStatus(), "Status should be mapped");
        assertEquals(LeadSubStatus.ONHOLD, minimal.getSubStatus(), "Sub-status should be mapped");
    }
}
