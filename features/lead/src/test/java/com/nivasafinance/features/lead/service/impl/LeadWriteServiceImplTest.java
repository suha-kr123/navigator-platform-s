package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.analytics.AnalyticsHelper;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.GeoData;
import com.nivasafinance.common.dto.PatchAddressData;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.common.enums.TenureType;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.ActiveLeadAlreadyExistsException;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadWriteServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AddressDataService addressDataService;

    @Mock
    private SourcingChannelWriteService sourcingChannelWriteService;

    @Mock
    private ProductReadService productReadService;

    @Mock
    private CodeValueMasterService codeValueMasterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private LeadContactWriteService contactWriteService;

    @Mock
    private WorkflowConfigRepositoryWrapper workflowConfigRepositoryWrapper;

    @Mock
    private LeadStageHistoryWriteService leadStageHistoryWriteService;

    @Mock
    private AnalyticsHelper analyticsHelper;

    @InjectMocks
    private LeadWriteServiceImpl leadWriteService;

    private UUID leadIdentifier;
    private Lead lead;

    @BeforeEach
    void setUp() {
        UserContext.setUsername("test-user");
        leadIdentifier = UUID.randomUUID();
        lead = new Lead();
        lead.setId(1L);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setStatus(LeadStatus.ACTIVE);
        lead.setOfficeKey("HQ");
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ==================== createLead() Tests ====================

    @Test
    void createLead_withValidRequest_createsLeadAndReturnsResponse() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setRequestedLoanAmount(BigDecimal.valueOf(1000000));
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails("9876543210", true));
        request.setProduct("HL");
        request.setOfficeKey("BRANCH-001");

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigKey("default_wf");

        UUID contactId = UUID.randomUUID();
        CreateLeadContactResponse contactResponse = CreateLeadContactResponse.builder().identifier(contactId).build();

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenAnswer(invocation -> {
            Lead l = invocation.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(contactWriteService.createContact(any(UUID.class), any(CreateLeadContactRequest.class)))
                .thenReturn(contactResponse);
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(anyString())).thenReturn(workflowConfig);

        // Act
        CreateLeadResponse result = leadWriteService.createLead(request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertNotNull(result.getLeadIdentifier(), "Lead identifier should be generated");
        assertEquals(contactId, result.getContactIdentifier(), "Contact identifier should match");
        verify(productReadService).getProductByCode("HL");
        verify(leadStageHistoryWriteService).createInitialStage(any(UUID.class), eq("default_wf"));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createLead_withNullOfficeKey_defaultsToHQ() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails("9876543210", false));

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigKey("default_wf");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenAnswer(invocation -> {
            Lead l = invocation.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(contactWriteService.createContact(any(UUID.class), any(CreateLeadContactRequest.class)))
                .thenReturn(CreateLeadContactResponse.builder().identifier(UUID.randomUUID()).build());
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(anyString())).thenReturn(workflowConfig);

        // Act
        leadWriteService.createLead(request);

        // Assert
        ArgumentCaptor<Lead> leadCaptor = ArgumentCaptor.forClass(Lead.class);
        verify(leadRepositoryWrapper).saveWithException(leadCaptor.capture());
        assertEquals("HQ", leadCaptor.getValue().getOfficeKey(), "Office key should default to HQ when not provided");
    }

    @Test
    void createLead_withExistingActiveLead_throwsActiveLeadAlreadyExistsException() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails("9876543210", false));

        Person existingPerson = new Person();
        existingPerson.setId(10L);

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.of(existingPerson));
        when(leadRepositoryWrapper.findActiveLeadByContactPersonId(10L)).thenReturn(Optional.of(lead));

        // Act & Assert
        assertThrows(ActiveLeadAlreadyExistsException.class,
                () -> leadWriteService.createLead(request),
                "Should throw when active lead already exists for the phone number");
    }

    // ==================== updateLead() Tests ====================

    @Test
    void updateLead_withValidRequest_updatesAllFields() {
        // Arrange
        UpdateLeadRequest request = UpdateLeadRequest.builder()
                .requestedAmount(BigDecimal.valueOf(2000000))
                .officeKey("BRANCH-002")
                .owner("manager-1")
                .purpose("HOME_PURCHASE")
                .customerConvinceStatus("INTERESTED")
                .productCode("HL")
                .preferredCallStartTime(LocalTime.of(9, 0))
                .preferredCallEndTime(LocalTime.of(17, 0))
                .priority("HIGH")
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.updateLead(leadIdentifier, request);

        // Assert
        assertEquals(BigDecimal.valueOf(2000000), lead.getRequestedAmount(), "Requested amount should be updated");
        assertEquals("BRANCH-002", lead.getOfficeKey(), "Office key should be updated");
        assertEquals("manager-1", lead.getOwner(), "Owner should be updated");
        assertEquals("HOME_PURCHASE", lead.getPurpose(), "Purpose should be validated and updated");
        verify(leadRepositoryWrapper).saveWithException(lead);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void updateLead_withNullOfficeKey_throwsBadRequestException() {
        // Arrange
        UpdateLeadRequest request = UpdateLeadRequest.builder().officeKey(null).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.updateLead(leadIdentifier, request),
                "Should throw when office key is null");
    }

    @Test
    void updateLead_withCallStartAfterEnd_throwsBadRequestException() {
        // Arrange
        UpdateLeadRequest request = UpdateLeadRequest.builder()
                .officeKey("HQ")
                .preferredCallStartTime(LocalTime.of(18, 0))
                .preferredCallEndTime(LocalTime.of(9, 0))
                .build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.updateLead(leadIdentifier, request),
                "Should throw when preferred call start time is after end time");
    }

    // ==================== touchLead() Tests ====================

    @Test
    void touchLead_withValidIdentifier_savesLead() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(lead)).thenReturn(lead);

        // Act
        leadWriteService.touchLead(leadIdentifier);

        // Assert
        verify(leadRepositoryWrapper).saveWithException(lead);
    }

    // ==================== updatePreliminaryDetails() Tests ====================

    @Test
    void updatePreliminaryDetails_withValidRequest_updatesDetails() {
        // Arrange
        UpdatePreliminaryDetailsRequest request = new UpdatePreliminaryDetailsRequest();
        request.setIsWhatsAppDIYFormCompleted(true);
        request.setMonthlyFamilyIncome(BigDecimal.valueOf(60000));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updatePreliminaryDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getPreliminaryDetails(), "Preliminary details should be set");
        assertEquals(true, lead.getPreliminaryDetails().getIsWhatsAppDIYFormCompleted(),
                "WhatsApp form completed flag should be updated");
        assertEquals(BigDecimal.valueOf(60000), lead.getPreliminaryDetails().getMonthlyFamilyIncome(),
                "Monthly family income should be updated");
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void updatePreliminaryDetails_withExistingDetails_mergesFields() {
        // Arrange
        Lead.PreliminaryDetails existing = new Lead.PreliminaryDetails();
        existing.setIsWhatsAppDIYFormCompleted(false);
        lead.setPreliminaryDetails(existing);

        UpdatePreliminaryDetailsRequest request = new UpdatePreliminaryDetailsRequest();
        request.setIsWhatsAppDIYFormCompleted(true);
        request.setMonthlyFamilyIncome(BigDecimal.valueOf(50000));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updatePreliminaryDetails(leadIdentifier, request);

        // Assert
        assertEquals(true, lead.getPreliminaryDetails().getIsWhatsAppDIYFormCompleted(),
                "Existing preliminary details should be updated in place");
    }

    // ==================== updateCreditDetails() Tests ====================

    @Test
    void updateCreditDetails_withValidRequest_setsUnderwriterOnFirstTime() {
        // Arrange
        UpdateCreditDetailsRequest request = UpdateCreditDetailsRequest.builder()
                .eligibleLoanAmount(BigDecimal.valueOf(3000000))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateCreditDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getCreditRatingDetails(), "Credit details should be set");
        assertEquals("test-user", lead.getCreditRatingDetails().getUnderwriter(),
                "Underwriter should be set to current user on first creation");
        assertEquals(BigDecimal.valueOf(3000000), lead.getCreditRatingDetails().getEligibleLoanAmount(),
                "Eligible loan amount should be updated");
    }

    @Test
    void updateCreditDetails_withExistingUnderwriter_doesNotOverwrite() {
        // Arrange
        Lead.CreditRatingDetails existingDetails = Lead.CreditRatingDetails.builder()
                .underwriter("original-user").build();
        lead.setCreditRatingDetails(existingDetails);

        UpdateCreditDetailsRequest request = UpdateCreditDetailsRequest.builder()
                .eligibleLoanAmount(BigDecimal.valueOf(5000000))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateCreditDetails(leadIdentifier, request);

        // Assert
        assertEquals("original-user", lead.getCreditRatingDetails().getUnderwriter(),
                "Existing underwriter should not be overwritten");
    }

    // ==================== updateProposedDetails() Tests ====================

    @Test
    void updateProposedDetails_withValidRequest_updatesAllFields() {
        // Arrange
        UpdateProposedDetailsRequest request = UpdateProposedDetailsRequest.builder()
                .proposedLoanAmount(BigDecimal.valueOf(2500000))
                .roi(BigDecimal.valueOf(9.5))
                .tenureValue(240)
                .tenureType(TenureType.MONTH)
                .emi(BigDecimal.valueOf(22000))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateProposedDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getProposedDetails(), "Proposed details should be set");
        assertEquals(BigDecimal.valueOf(2500000), lead.getProposedDetails().getProposedLoanAmount(),
                "Proposed loan amount should be updated");
        assertEquals(TenureType.MONTH, lead.getProposedDetails().getTenureType(),
                "Tenure type should be updated");
    }

    // ==================== updatePropertyDetails() Tests ====================

    @Test
    void updatePropertyDetails_withValidRequest_setsAddress() {
        // Arrange
        AddressRequest addressRequest = new AddressRequest();
        AddressData addressData = new AddressData();
        UpdatePropertyDetailsRequest request = UpdatePropertyDetailsRequest.builder().address(addressRequest).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(addressDataService.createAddressData(addressRequest)).thenReturn(addressData);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updatePropertyDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getOtherDetails(), "Other details should be initialized");
        assertNotNull(lead.getOtherDetails().getPropertyDetails(), "Property details should be initialized");
        assertEquals(addressData, lead.getOtherDetails().getPropertyDetails().getAddress(),
                "Address should be set from address data service");
    }

    // ==================== updateSourcingDetails() Tests ====================

    @Test
    void updateSourcingDetails_withNewSourcingChannel_createsChannel() {
        // Arrange
        lead.setSourcingChannelId(null);
        UpdateSourcingDetailsRequest request = new UpdateSourcingDetailsRequest();
        request.setSourcingChannel("ONLINE");
        request.setMarketingSource("GOOGLE");

        SourcingChannelResponse channelResponse = new SourcingChannelResponse();
        channelResponse.setId(100L);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(sourcingChannelWriteService.create(any(SourcingChannelRequest.class))).thenReturn(channelResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateSourcingDetails(leadIdentifier, request);

        // Assert
        assertEquals(100L, lead.getSourcingChannelId(), "Sourcing channel ID should be set from created channel");
        verify(sourcingChannelWriteService).create(any(SourcingChannelRequest.class));
    }

    @Test
    void updateSourcingDetails_withExistingSourcingChannel_updatesChannel() {
        // Arrange
        lead.setSourcingChannelId(50L);
        UpdateSourcingDetailsRequest request = new UpdateSourcingDetailsRequest();
        request.setSourcingChannel("OFFLINE");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateSourcingDetails(leadIdentifier, request);

        // Assert
        verify(sourcingChannelWriteService).update(eq(50L), any(SourcingChannelRequest.class));
    }

    // ==================== updateCallDetails() Tests ====================

    @Test
    void updateCallDetails_withValidRequest_updatesNoOfCampaignCalls() {
        // Arrange
        UpdateCallDetailsRequest request = UpdateCallDetailsRequest.builder().noOfCampaignCalls(5L).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateCallDetails(leadIdentifier, request);

        // Assert
        assertEquals(5L, lead.getOtherDetails().getNoOfCampaignCalls(), "Number of campaign calls should be updated");
    }

    // ==================== updateDisbursementDetails() Tests ====================

    @Test
    void updateDisbursementDetails_withValidRequest_preservesExistingTranches() {
        // Arrange
        Lead.Tranche existingTranche = Lead.Tranche.builder()
                .identifier(UUID.randomUUID()).amount(BigDecimal.valueOf(500000)).build();
        Lead.DisbursementDetails existing = Lead.DisbursementDetails.builder()
                .tranches(List.of(existingTranche)).build();
        lead.setDisbursementDetails(existing);

        UpdateDisbursementDetailsRequest request = UpdateDisbursementDetailsRequest.builder()
                .disbursedAmount(BigDecimal.valueOf(2000000))
                .roi(BigDecimal.valueOf(9.0))
                .tenureValue(240).tenureType(TenureType.MONTH)
                .disbursedDate(LocalDate.of(2025, 1, 15))
                .processingFees(BigDecimal.valueOf(10000))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateDisbursementDetails(leadIdentifier, request);

        // Assert
        assertEquals(BigDecimal.valueOf(2000000), lead.getDisbursementDetails().getDisbursedAmount(),
                "Disbursed amount should be updated");
        assertEquals(1, lead.getDisbursementDetails().getTranches().size(),
                "Existing tranches should be preserved");
    }

    // ==================== createTranche() Tests ====================

    @Test
    void createTranche_withValidRequest_addsTranche() {
        // Arrange
        lead.setDisbursementDetails(Lead.DisbursementDetails.builder().tranches(new ArrayList<>()).build());
        CreateTrancheRequest request = CreateTrancheRequest.builder()
                .amount(BigDecimal.valueOf(300000)).date(LocalDate.of(2025, 6, 1)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.createTranche(leadIdentifier, request);

        // Assert
        assertEquals(1, lead.getDisbursementDetails().getTranches().size(), "One tranche should be added");
        assertEquals(BigDecimal.valueOf(300000), lead.getDisbursementDetails().getTranches().get(0).getAmount(),
                "Tranche amount should match request");
    }

    @Test
    void createTranche_withNullDisbursementDetails_throwsBadRequestException() {
        // Arrange
        lead.setDisbursementDetails(null);
        CreateTrancheRequest request = CreateTrancheRequest.builder()
                .amount(BigDecimal.valueOf(300000)).date(LocalDate.of(2025, 6, 1)).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.createTranche(leadIdentifier, request),
                "Should throw when disbursement details do not exist");
    }

    // ==================== updateTranche() Tests ====================

    @Test
    void updateTranche_withMatchingTranche_updatesFields() {
        // Arrange
        UUID trancheId = UUID.randomUUID();
        Lead.Tranche tranche = Lead.Tranche.builder()
                .identifier(trancheId).amount(BigDecimal.valueOf(100000)).date(LocalDate.of(2025, 1, 1)).build();
        lead.setDisbursementDetails(Lead.DisbursementDetails.builder()
                .tranches(new ArrayList<>(List.of(tranche))).build());

        UpdateTrancheRequest request = UpdateTrancheRequest.builder()
                .amount(BigDecimal.valueOf(200000)).date(LocalDate.of(2025, 7, 1)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateTranche(leadIdentifier, trancheId, request);

        // Assert
        assertEquals(BigDecimal.valueOf(200000), tranche.getAmount(), "Tranche amount should be updated");
        assertEquals(LocalDate.of(2025, 7, 1), tranche.getDate(), "Tranche date should be updated");
    }

    @Test
    void updateTranche_withNonMatchingTranche_throwsResourceNotFoundException() {
        // Arrange
        lead.setDisbursementDetails(Lead.DisbursementDetails.builder()
                .tranches(new ArrayList<>(List.of(Lead.Tranche.builder().identifier(UUID.randomUUID()).build()))).build());
        UUID nonExisting = UUID.randomUUID();
        UpdateTrancheRequest request = UpdateTrancheRequest.builder()
                .amount(BigDecimal.TEN).date(LocalDate.now()).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> leadWriteService.updateTranche(leadIdentifier, nonExisting, request),
                "Should throw when tranche identifier does not match");
    }

    // ==================== deleteTranche() Tests ====================

    @Test
    void deleteTranche_withMatchingTranche_removesTranche() {
        // Arrange
        UUID trancheId = UUID.randomUUID();
        Lead.Tranche tranche = Lead.Tranche.builder().identifier(trancheId).build();
        lead.setDisbursementDetails(Lead.DisbursementDetails.builder()
                .tranches(new ArrayList<>(List.of(tranche))).build());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.deleteTranche(leadIdentifier, trancheId);

        // Assert
        assertTrue(lead.getDisbursementDetails().getTranches().isEmpty(), "Tranche should be removed from list");
    }

    @Test
    void deleteTranche_withNonMatchingTranche_throwsResourceNotFoundException() {
        // Arrange
        lead.setDisbursementDetails(Lead.DisbursementDetails.builder()
                .tranches(new ArrayList<>(List.of(Lead.Tranche.builder().identifier(UUID.randomUUID()).build()))).build());
        UUID nonExisting = UUID.randomUUID();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> leadWriteService.deleteTranche(leadIdentifier, nonExisting),
                "Should throw when tranche identifier does not match");
    }

    // ==================== rejectLead() Tests ====================

    @Test
    void rejectLead_withValidRequest_setsStatusToRejected() {
        // Arrange
        RejectLeadRequest request = RejectLeadRequest.builder().reasonCode("LOW_INCOME").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.rejectLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadStatus.REJECTED, lead.getStatus(), "Lead status should be REJECTED");
        assertNull(lead.getSubstatus(), "Substatus should be cleared on rejection");
        assertNotNull(lead.getRejectionDetails(), "Rejection details should be set");
        assertEquals("test-user", lead.getRejectionDetails().getRejectedBy(), "Rejected by should be current user");
        assertEquals("LOW_INCOME", lead.getReasons().getReject(), "Reject reason should be stored");
    }

    // ==================== undoRejectLead() Tests ====================

    @Test
    void undoRejectLead_withRejectedLead_revertsToActive() {
        // Arrange
        lead.setStatus(LeadStatus.REJECTED);
        lead.setReasons(Lead.ReasonDetails.builder().reject("LOW_INCOME").build());
        lead.setRejectionDetails(new Lead.RejectionDetails());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.undoRejectLead(leadIdentifier);

        // Assert
        assertEquals(LeadStatus.ACTIVE, lead.getStatus(), "Lead status should revert to ACTIVE");
        assertNull(lead.getSubstatus(), "Substatus should remain null");
        assertNull(lead.getReasons().getReject(), "Reject reason should be cleared");
        assertNull(lead.getRejectionDetails(), "Rejection details should be cleared");
    }

    @Test
    void undoRejectLead_withNonRejectedLead_throwsBadRequestException() {
        // Arrange
        lead.setStatus(LeadStatus.ACTIVE);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.undoRejectLead(leadIdentifier),
                "Should throw when lead is not in REJECTED status");
    }

    // ==================== withdrawLead() Tests ====================

    @Test
    void withdrawLead_withValidRequest_setsStatusToWithdrawn() {
        // Arrange
        WithdrawLeadRequest request = WithdrawLeadRequest.builder().reasonCode("CUSTOMER_REQUEST").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.withdrawLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadStatus.WITHDRAWN, lead.getStatus(), "Lead status should be WITHDRAWN");
        assertNull(lead.getSubstatus(), "Substatus should be cleared on withdrawal");
        assertNotNull(lead.getWithdrawnDetails(), "Withdrawn details should be set");
        assertEquals("test-user", lead.getWithdrawnDetails().getWithdrawnBy(), "Withdrawn by should be current user");
    }

    // ==================== completeLead() Tests ====================

    @Test
    void completeLead_withActiveLead_setsStatusToCompleted() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.completeLead(leadIdentifier);

        // Assert
        assertEquals(LeadStatus.COMPLETED, lead.getStatus(), "Lead status should be COMPLETED");
    }

    @Test
    void completeLead_withOnHoldLead_throwsBadRequestException() {
        // Arrange
        lead.setSubstatus(LeadSubStatus.ONHOLD);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.completeLead(leadIdentifier),
                "Should throw when lead is on hold");
    }

    // ==================== onholdLead() Tests ====================

    @Test
    void onholdLead_withActiveLead_setsSubstatusToOnhold() {
        // Arrange
        OnholdLeadRequest request = OnholdLeadRequest.builder()
                .reasonCode("DOCUMENTS_PENDING")
                .holdFollowUpDate(LocalDate.of(2025, 7, 1))
                .build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.onholdLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadSubStatus.ONHOLD, lead.getSubstatus(), "Substatus should be ONHOLD");
        assertNotNull(lead.getOnHoldDetails(), "OnHold details should be set");
        assertEquals("test-user", lead.getOnHoldDetails().getOnHoldBy(), "OnHold by should be current user");
        assertEquals(LocalDate.of(2025, 7, 1), lead.getOnHoldDetails().getHoldFollowUpDate(),
                "Follow-up date should be set from request");
    }

    @Test
    void onholdLead_whenAlreadyOnHold_updatesFollowUpDateOnly() {
        // Arrange
        lead.setSubstatus(LeadSubStatus.ONHOLD);
        Lead.OnHoldDetails existingDetails = Lead.OnHoldDetails.builder()
                .onHoldBy("original-user").holdFollowUpDate(LocalDate.of(2025, 5, 1)).build();
        lead.setOnHoldDetails(existingDetails);

        OnholdLeadRequest request = OnholdLeadRequest.builder()
                .holdFollowUpDate(LocalDate.of(2025, 8, 1)).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.onholdLead(leadIdentifier, request);

        // Assert
        assertEquals("original-user", lead.getOnHoldDetails().getOnHoldBy(),
                "OnHold by should not change when already on hold");
        assertEquals(LocalDate.of(2025, 8, 1), lead.getOnHoldDetails().getHoldFollowUpDate(),
                "Follow-up date should be updated");
    }

    @Test
    void onholdLead_withRejectedLead_throwsBadRequestException() {
        // Arrange
        lead.setStatus(LeadStatus.REJECTED);
        OnholdLeadRequest request = OnholdLeadRequest.builder()
                .holdFollowUpDate(LocalDate.of(2025, 7, 1)).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.onholdLead(leadIdentifier, request),
                "Should throw when lead is rejected");
    }

    // ==================== resumeLead() Tests ====================

    @Test
    void resumeLead_withOnHoldLead_clearsSubstatusAndReason() {
        // Arrange
        lead.setSubstatus(LeadSubStatus.ONHOLD);
        lead.setReasons(Lead.ReasonDetails.builder().onhold("DOCUMENTS_PENDING").build());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.resumeLead(leadIdentifier);

        // Assert
        assertNull(lead.getSubstatus(), "Substatus should be cleared");
        assertNull(lead.getReasons().getOnhold(), "Onhold reason should be cleared");
    }

    // ==================== dropoffLead() Tests ====================

    @Test
    void dropoffLead_withActiveLead_setsSubstatusToDropoff() {
        // Arrange
        DropoffLeadRequest request = DropoffLeadRequest.builder().reasonCode("NO_RESPONSE").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.dropoffLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadSubStatus.DROPOFF, lead.getSubstatus(), "Substatus should be DROPOFF");
        assertNotNull(lead.getDropoffDetails(), "Dropoff details should be set");
        assertEquals("test-user", lead.getDropoffDetails().getDropoffBy(), "Dropoff by should be current user");
    }

    @Test
    void dropoffLead_withNonActiveLead_throwsBadRequestException() {
        // Arrange
        lead.setStatus(LeadStatus.REJECTED);
        DropoffLeadRequest request = DropoffLeadRequest.builder().reasonCode("NO_RESPONSE").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.dropoffLead(leadIdentifier, request),
                "Should throw when lead is not in ACTIVE status");
    }

    // ==================== createLead() – additional coverage ====================

    @Test
    void createLead_withNullProduct_skipsProductValidation() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setProduct(null);
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails("9876543210", false));
        request.setOfficeKey("BRANCH-001");

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigKey("default_wf");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenAnswer(inv -> {
            Lead l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(contactWriteService.createContact(any(UUID.class), any(CreateLeadContactRequest.class)))
                .thenReturn(CreateLeadContactResponse.builder().identifier(UUID.randomUUID()).build());
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(anyString())).thenReturn(workflowConfig);

        // Act
        CreateLeadResponse result = leadWriteService.createLead(request);

        // Assert
        assertNotNull(result, "Should create lead even with null product");
        verify(productReadService, never()).getProductByCode(anyString());
    }

    @Test
    void createLead_withExistingPersonButNoActiveLead_createsLeadSuccessfully() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails("9876543210", false));

        Person existingPerson = new Person();
        existingPerson.setId(10L);

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigKey("default_wf");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.of(existingPerson));
        when(leadRepositoryWrapper.findActiveLeadByContactPersonId(10L)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenAnswer(inv -> {
            Lead l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(contactWriteService.createContact(any(UUID.class), any(CreateLeadContactRequest.class)))
                .thenReturn(CreateLeadContactResponse.builder().identifier(UUID.randomUUID()).build());
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(anyString())).thenReturn(workflowConfig);

        // Act
        CreateLeadResponse result = leadWriteService.createLead(request);

        // Assert
        assertNotNull(result, "Should create lead when person exists but has no active lead");
    }

    // ==================== updateLead() – additional coverage ====================

    @Test
    void updateLead_withNullPurpose_setsPurposeToNull() {
        // Arrange
        lead.setPurpose("OLD_PURPOSE");
        UpdateLeadRequest request = UpdateLeadRequest.builder()
                .officeKey("HQ").purpose(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateLead(leadIdentifier, request);

        // Assert
        assertNull(lead.getPurpose(), "Purpose should be set to null when request has null purpose");
    }

    @Test
    void updateLead_withNullCustomerConvinceStatus_setsToNull() {
        // Arrange
        lead.setCustomerConvinceStatus("INTERESTED");
        UpdateLeadRequest request = UpdateLeadRequest.builder()
                .officeKey("HQ").customerConvinceStatus(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateLead(leadIdentifier, request);

        // Assert
        assertNull(lead.getCustomerConvinceStatus(),
                "Customer convince status should be set to null when not provided");
    }

    @Test
    void updateLead_withNullCallTimes_clearsCallTimes() {
        // Arrange
        UpdateLeadRequest request = UpdateLeadRequest.builder()
                .officeKey("HQ")
                .preferredCallStartTime(null)
                .preferredCallEndTime(null)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateLead(leadIdentifier, request);

        // Assert
        assertNull(lead.getOtherDetails().getPreferredCallStartTime(),
                "Call start time should be cleared when not provided");
        assertNull(lead.getOtherDetails().getPreferredCallEndTime(),
                "Call end time should be cleared when not provided");
    }

    @Test
    void updateLead_withNullOtherDetails_initializesOtherDetails() {
        // Arrange
        lead.setOtherDetails(null);
        UpdateLeadRequest request = UpdateLeadRequest.builder().officeKey("HQ").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateLead(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getOtherDetails(), "OtherDetails should be initialized when null");
    }

    @Test
    void updateLead_withProductCode_validatesAndSetsProduct() {
        // Arrange
        UpdateLeadRequest request = UpdateLeadRequest.builder()
                .officeKey("HQ").productCode("PL").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateLead(leadIdentifier, request);

        // Assert
        assertEquals("PL", lead.getProductCode(), "Product code should be updated");
        verify(productReadService).getProductByCode("PL");
    }

    // ==================== updateCreditDetails() – additional coverage ====================

    @Test
    void updateCreditDetails_withEmptyUnderwriter_setsCurrentUser() {
        // Arrange
        Lead.CreditRatingDetails existingDetails = Lead.CreditRatingDetails.builder()
                .underwriter("   ").build();
        lead.setCreditRatingDetails(existingDetails);

        UpdateCreditDetailsRequest request = UpdateCreditDetailsRequest.builder()
                .eligibleLoanAmount(BigDecimal.valueOf(1000000)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateCreditDetails(leadIdentifier, request);

        // Assert
        assertEquals("test-user", lead.getCreditRatingDetails().getUnderwriter(),
                "Empty underwriter should be replaced with current user");
    }

    @Test
    void updateCreditDetails_withOccupationProfile_validatesAndSets() {
        // Arrange
        UpdateCreditDetailsRequest request = UpdateCreditDetailsRequest.builder()
                .occupationProfile("SALARIED").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateCreditDetails(leadIdentifier, request);

        // Assert
        assertEquals("SALARIED", lead.getCreditRatingDetails().getOccupationProfile(),
                "Occupation profile should be validated and set");
    }

    // ==================== updateDisbursementDetails() – additional coverage ====================

    @Test
    void updateDisbursementDetails_withNullExistingDetails_createsNew() {
        // Arrange
        lead.setDisbursementDetails(null);

        UpdateDisbursementDetailsRequest request = UpdateDisbursementDetailsRequest.builder()
                .disbursedAmount(BigDecimal.valueOf(1000000))
                .roi(BigDecimal.valueOf(9.5))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updateDisbursementDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getDisbursementDetails(), "Disbursement details should be initialized");
        assertEquals(BigDecimal.valueOf(1000000), lead.getDisbursementDetails().getDisbursedAmount(),
                "Disbursed amount should be set");
    }

    // ==================== createTranche() – additional coverage ====================

    @Test
    void createTranche_withNullTranchesList_initializesListAndAdds() {
        // Arrange
        lead.setDisbursementDetails(Lead.DisbursementDetails.builder().tranches(null).build());
        CreateTrancheRequest request = CreateTrancheRequest.builder()
                .amount(BigDecimal.valueOf(200000)).date(LocalDate.of(2025, 3, 1)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.createTranche(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getDisbursementDetails().getTranches(), "Tranches list should be initialized");
        assertEquals(1, lead.getDisbursementDetails().getTranches().size(), "One tranche should be added");
    }

    // ==================== updateTranche() / deleteTranche() – additional coverage ====================

    @Test
    void updateTranche_withNullDisbursementDetails_throwsResourceNotFoundException() {
        // Arrange
        lead.setDisbursementDetails(null);
        UUID trancheId = UUID.randomUUID();
        UpdateTrancheRequest request = UpdateTrancheRequest.builder()
                .amount(BigDecimal.TEN).date(LocalDate.now()).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> leadWriteService.updateTranche(leadIdentifier, trancheId, request),
                "Should throw when disbursement details are null");
    }

    @Test
    void deleteTranche_withNullDisbursementDetails_throwsResourceNotFoundException() {
        // Arrange
        lead.setDisbursementDetails(null);
        UUID trancheId = UUID.randomUUID();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> leadWriteService.deleteTranche(leadIdentifier, trancheId),
                "Should throw when disbursement details are null");
    }

    // ==================== rejectLead() / withdrawLead() – additional coverage ====================

    @Test
    void rejectLead_withNullReasonCode_skipsReasonValidation() {
        // Arrange
        RejectLeadRequest request = RejectLeadRequest.builder().reasonCode(null).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.rejectLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadStatus.REJECTED, lead.getStatus(), "Status should be REJECTED");
        verifyNoInteractions(codeValueMasterService);
    }

    @Test
    void rejectLead_withNullExistingReasons_createsNewReasonDetails() {
        // Arrange
        lead.setReasons(null);
        RejectLeadRequest request = RejectLeadRequest.builder().reasonCode("LOW_INCOME").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.rejectLead(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getReasons(), "Reasons should be initialized when null");
        assertEquals("LOW_INCOME", lead.getReasons().getReject(), "Reject reason should be set");
        assertNull(lead.getReasons().getOnhold(), "Onhold reason should be cleared on rejection");
    }

    @Test
    void withdrawLead_withNullReasonCode_skipsReasonValidation() {
        // Arrange
        WithdrawLeadRequest request = WithdrawLeadRequest.builder().reasonCode(null).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.withdrawLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadStatus.WITHDRAWN, lead.getStatus(), "Status should be WITHDRAWN");
        verifyNoInteractions(codeValueMasterService);
    }

    @Test
    void withdrawLead_withNullExistingReasons_createsNewReasonDetails() {
        // Arrange
        lead.setReasons(null);
        WithdrawLeadRequest request = WithdrawLeadRequest.builder().reasonCode("CUSTOMER_REQUEST").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.withdrawLead(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getReasons(), "Reasons should be initialized when null");
        assertEquals("CUSTOMER_REQUEST", lead.getReasons().getWithdrawn(), "Withdrawn reason should be set");
    }

    // ==================== onholdLead() – additional coverage ====================

    @Test
    void onholdLead_withWithdrawnLead_throwsBadRequestException() {
        // Arrange
        lead.setStatus(LeadStatus.WITHDRAWN);
        OnholdLeadRequest request = OnholdLeadRequest.builder()
                .holdFollowUpDate(LocalDate.of(2025, 7, 1)).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.onholdLead(leadIdentifier, request),
                "Should throw when lead is withdrawn");
    }

    @Test
    void onholdLead_withNullReasonCode_skipsReasonValidation() {
        // Arrange
        OnholdLeadRequest request = OnholdLeadRequest.builder()
                .reasonCode(null).holdFollowUpDate(LocalDate.of(2025, 7, 1)).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.onholdLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadSubStatus.ONHOLD, lead.getSubstatus(), "Substatus should be ONHOLD");
        verifyNoInteractions(codeValueMasterService);
    }

    @Test
    void onholdLead_withNullExistingReasons_createsNewReasonDetails() {
        // Arrange
        lead.setReasons(null);
        OnholdLeadRequest request = OnholdLeadRequest.builder()
                .reasonCode("DOCUMENTS_PENDING").holdFollowUpDate(LocalDate.of(2025, 7, 1)).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.onholdLead(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getReasons(), "Reasons should be initialized when null");
        assertEquals("DOCUMENTS_PENDING", lead.getReasons().getOnhold(), "Onhold reason should be set");
    }

    // ==================== resumeLead() – additional coverage ====================

    @Test
    void resumeLead_withNullReasons_skipsReasonClearing() {
        // Arrange
        lead.setSubstatus(LeadSubStatus.ONHOLD);
        lead.setReasons(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.resumeLead(leadIdentifier);

        // Assert
        assertNull(lead.getSubstatus(), "Substatus should be cleared");
        assertNull(lead.getReasons(), "Reasons should remain null");
    }

    // ==================== dropoffLead() – additional coverage ====================

    @Test
    void dropoffLead_withNullReasonCode_skipsReasonValidation() {
        // Arrange
        DropoffLeadRequest request = DropoffLeadRequest.builder().reasonCode(null).build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.dropoffLead(leadIdentifier, request);

        // Assert
        assertEquals(LeadSubStatus.DROPOFF, lead.getSubstatus(), "Substatus should be DROPOFF");
        verifyNoInteractions(codeValueMasterService);
    }

    @Test
    void dropoffLead_withNullExistingReasons_createsNewReasonDetails() {
        // Arrange
        lead.setReasons(null);
        DropoffLeadRequest request = DropoffLeadRequest.builder().reasonCode("NO_RESPONSE").build();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());

        // Act
        leadWriteService.dropoffLead(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getReasons(), "Reasons should be initialized when null");
        assertEquals("NO_RESPONSE", lead.getReasons().getDropoff(), "Dropoff reason should be set");
    }

    // ==================== undoRejectLead() – additional coverage ====================

    @Test
    void undoRejectLead_withNullReasons_skipsReasonClearing() {
        // Arrange
        lead.setStatus(LeadStatus.REJECTED);
        lead.setReasons(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.undoRejectLead(leadIdentifier);

        // Assert
        assertEquals(LeadStatus.ACTIVE, lead.getStatus(), "Status should revert to ACTIVE");
        assertNull(lead.getReasons(), "Reasons should remain null when not set");
    }

    // ==================== updatePreliminaryDetails() – additional coverage ====================

    @Test
    void updatePreliminaryDetails_withWhatsAppFormDetails_setsFormDetails() {
        // Arrange
        Map<String, String> formDetails = Map.of("step1", "completed");
        UpdatePreliminaryDetailsRequest request = new UpdatePreliminaryDetailsRequest();
        request.setWhatsAppFormDetails(formDetails);
        request.setIsWhatsAppDIYFormCompleted(true);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.updatePreliminaryDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getPreliminaryDetails().getWhatsAppDIYForm(),
                "WhatsApp form details should be set when provided");
    }

    // ==================== completeLead() – additional coverage ====================

    @Test
    void completeLead_withDropoffLead_completesSuccessfully() {
        // Arrange
        lead.setSubstatus(LeadSubStatus.DROPOFF);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.completeLead(leadIdentifier);

        // Assert
        assertEquals(LeadStatus.COMPLETED, lead.getStatus(),
                "Should complete even when substatus is DROPOFF (only ONHOLD is blocked)");
    }

    // ==================== createLead() – sourcing channel coverage ====================

    @Test
    void createLead_withSourcingChannelRequest_createsSourcingChannel() {
        // Arrange
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails("9876543210", false));
        request.setSourcingChannelRequest(new SourcingChannelRequest("ONLINE", "GOOGLE", null));

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigKey("default_wf");

        SourcingChannelResponse channelResponse = new SourcingChannelResponse();
        channelResponse.setId(200L);

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210")).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenAnswer(inv -> {
            Lead l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });
        when(contactWriteService.createContact(any(UUID.class), any(CreateLeadContactRequest.class)))
                .thenReturn(CreateLeadContactResponse.builder().identifier(UUID.randomUUID()).build());
        when(workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(anyString())).thenReturn(workflowConfig);
        when(sourcingChannelWriteService.create(any(SourcingChannelRequest.class))).thenReturn(channelResponse);

        // Act
        CreateLeadResponse result = leadWriteService.createLead(request);

        // Assert
        assertNotNull(result, "Should create lead with sourcing channel");
        verify(sourcingChannelWriteService).create(any(SourcingChannelRequest.class));
    }

    // ==================== patchPropertyDetails() Tests ====================

    @Test
    void patchPropertyDetails_withAllFields_mergesIntoLead() {
        // Arrange
        PatchAddressData patchAddr = PatchAddressData.builder()
                .address(Optional.of("123 Main St"))
                .pincode(Optional.of("560001"))
                .district(Optional.of("Bangalore"))
                .state(Optional.of("Karnataka"))
                .country(Optional.of("India"))
                .region(Optional.of("North Karnataka"))
                .taluka(Optional.of("South"))
                .districtCode(Optional.of("BLR"))
                .regionCode(Optional.of("REG01"))
                .stateCode(Optional.of("KA"))
                .countryCode(Optional.of("IN"))
                .talukaCode(Optional.of("S"))
                .districtId(Optional.of(1L))
                .regionId(Optional.of(6L))
                .stateId(Optional.of(2L))
                .countryId(Optional.of(3L))
                .talukaId(Optional.of(4L))
                .villageCode(Optional.of("V001"))
                .villageId(Optional.of(5L))
                .villageName(Optional.of("TestVillage"))
                .isServiceable(Optional.of(true))
                .addressType(Optional.of(AddressType.CURRENT))
                .id(Optional.of("addr-123"))
                .build();

        GeoData geoData = new GeoData();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .address(Optional.of(patchAddr))
                .geoData(Optional.of(geoData))
                .propertyType(Optional.of("A_KHATA"))
                .propertyConstructionStage(Optional.of("COMPLETED"))
                .owner(Optional.of("John"))
                .ownerRelation(Optional.of("SELF"))
                .propertyMeasurementDetails(Optional.of(
                        PatchPropertyDetailsRequest.PropertyMeasurementDetailsData.builder()
                                .buildUpArea("1200").siteArea("1500").build()))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(anyString(), anyString())).thenReturn(new CodeValueResponse());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getOtherDetails().getPropertyDetails(), "Property details should be set");
        assertNotNull(lead.getOtherDetails().getPropertyDetails().getAddress(), "Address should be merged");
        assertEquals("123 Main St", lead.getOtherDetails().getPropertyDetails().getAddress().getAddress(),
                "Street address should be set from patch");
        assertEquals("560001", lead.getOtherDetails().getPropertyDetails().getAddress().getPincode(),
                "Pincode should be set from patch");
        assertEquals("Bangalore", lead.getOtherDetails().getPropertyDetails().getAddress().getDistrict(),
                "District name should be set from patch");
        assertEquals("North Karnataka", lead.getOtherDetails().getPropertyDetails().getAddress().getRegion(),
                "Region name should be set from patch");
        assertEquals("REG01", lead.getOtherDetails().getPropertyDetails().getAddress().getRegionCode(),
                "Region code should be set from patch");
        assertEquals(6L, lead.getOtherDetails().getPropertyDetails().getAddress().getRegionId(),
                "Region id should be set from patch");
        assertEquals("A_KHATA", lead.getOtherDetails().getPropertyDetails().getPropertyType(),
                "Property type should be set from patch");
        assertEquals("COMPLETED", lead.getOtherDetails().getPropertyDetails().getPropertyConstructionStage(),
                "Property construction stage should be set from patch");
        assertEquals("John", lead.getOtherDetails().getPropertyDetails().getOwner(),
                "Owner should be set from patch");
        assertEquals("SELF", lead.getOtherDetails().getPropertyDetails().getOwnerRelation(),
                "Owner relation should be set from patch");
        assertNotNull(lead.getOtherDetails().getPropertyDetails().getPropertyMeasurementDetails(),
                "Property measurement details should be set from patch");
        assertEquals("1200", lead.getOtherDetails().getPropertyDetails().getPropertyMeasurementDetails().getBuildUpArea(),
                "Build-up area should be set from patch");
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void patchPropertyDetails_withNullOtherDetails_initializesOtherDetails() {
        // Arrange
        lead.setOtherDetails(null);
        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .owner(Optional.of("Jane")).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getOtherDetails(), "OtherDetails should be initialized");
        assertEquals("Jane", lead.getOtherDetails().getPropertyDetails().getOwner());
    }

    @Test
    void patchPropertyDetails_withNullMeasurementData_clearsDetails() {
        // Arrange
        Lead.PropertyDetails existing = new Lead.PropertyDetails();
        existing.setPropertyMeasurementDetails(
                Lead.PropertyDetails.PropertyMeasurementDetails.builder()
                        .buildUpArea("1000").siteArea("1200").build());
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(existing).build();
        lead.setOtherDetails(otherDetails);

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .propertyMeasurementDetails(Optional.empty())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        assertNull(lead.getOtherDetails().getPropertyDetails().getPropertyMeasurementDetails(),
                "Measurement details should be cleared when Optional is empty");
    }

    @Test
    void patchPropertyDetails_withDocumentChecklist_setsFields() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .ekhataType(Optional.of("A_KHATA"))
                .ekhataStatus(Optional.of("AVAILABLE"))
                .saleDeed(Optional.of("AVAILABLE"))
                .propertyTax(Optional.of("NOT_AVAILABLE"))
                .statementOfAccounts(Optional.of("AVAILABLE"))
                .otherDocs(Optional.of("NOT_AVAILABLE"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        Lead.DocumentChecklist result = lead.getOtherDetails().getPropertyDetails().getDocumentChecklist();
        assertNotNull(result, "Document checklist should be set");
        assertEquals("A_KHATA", result.getEkhataType());
        assertEquals("AVAILABLE", result.getEkhataStatus());
        assertEquals("AVAILABLE", result.getSaleDeed());
        assertEquals("NOT_AVAILABLE", result.getPropertyTax());
    }

    @Test
    void patchPropertyDetails_withInvalidEkhataType_throwsBadRequestException() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .ekhataType(Optional.of("INVALID_TYPE"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchPropertyDetails(leadIdentifier, request),
                "Should throw when ekhataType is invalid");
    }

    @Test
    void patchPropertyDetails_withInvalidSaleDeed_throwsBadRequestException() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .saleDeed(Optional.of("INVALID_STATUS"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchPropertyDetails(leadIdentifier, request),
                "Should throw when saleDeed status is invalid");
    }

    @Test
    void patchPropertyDetails_withInvalidEkhataStatus_throwsBadRequestException() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .ekhataStatus(Optional.of("INVALID"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchPropertyDetails(leadIdentifier, request),
                "Should throw when ekhataStatus is invalid");
    }

    @Test
    void patchPropertyDetails_withClearingDocumentChecklistFields_setsNulls() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .ekhataType(Optional.empty())
                .ekhataStatus(Optional.empty())
                .saleDeed(Optional.empty())
                .propertyTax(Optional.empty())
                .statementOfAccounts(Optional.empty())
                .otherDocs(Optional.empty())
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        Lead.DocumentChecklist result = lead.getOtherDetails().getPropertyDetails().getDocumentChecklist();
        assertNull(result.getEkhataType(), "EkhataType should be cleared");
        assertNull(result.getEkhataStatus(), "EkhataStatus should be cleared");
        assertNull(result.getSaleDeed(), "SaleDeed should be cleared");
    }

    @Test
    void patchPropertyDetails_withExistingAddress_mergesIntoExisting() {
        // Arrange
        AddressData existingAddr = new AddressData();
        existingAddr.setAddress("Old address");
        existingAddr.setPincode("500001");
        Lead.PropertyDetails existing = new Lead.PropertyDetails();
        existing.setAddress(existingAddr);
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(existing).build();
        lead.setOtherDetails(otherDetails);

        PatchAddressData patchAddr = PatchAddressData.builder()
                .pincode(Optional.of("560001"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .address(Optional.of(patchAddr)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        assertEquals("Old address", lead.getOtherDetails().getPropertyDetails().getAddress().getAddress(),
                "Existing address should be preserved");
        assertEquals("560001", lead.getOtherDetails().getPropertyDetails().getAddress().getPincode(),
                "Pincode should be updated");
    }

    @Test
    void patchPropertyDetails_withClearingGeoDataAndPropertyType_setsNulls() {
        // Arrange
        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .geoData(Optional.empty())
                .propertyType(Optional.empty())
                .propertyConstructionStage(Optional.empty())
                .owner(Optional.empty())
                .ownerRelation(Optional.empty())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        assertNull(lead.getOtherDetails().getPropertyDetails().getGeoData(), "GeoData should be cleared");
        assertNull(lead.getOtherDetails().getPropertyDetails().getPropertyType(), "PropertyType should be cleared");
        assertNull(lead.getOtherDetails().getPropertyDetails().getOwner(), "Owner should be cleared");
    }

    @Test
    void patchPropertyDetails_withRegionFields_setsRegionData() {
        // Arrange
        PatchAddressData patchAddr = PatchAddressData.builder()
                .region(Optional.of("North Karnataka"))
                .regionCode(Optional.of("REG01"))
                .regionId(Optional.of(50L))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .address(Optional.of(patchAddr))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        AddressData addr = lead.getOtherDetails().getPropertyDetails().getAddress();
        assertEquals("North Karnataka", addr.getRegion(), "Region name should be set from patch");
        assertEquals("REG01", addr.getRegionCode(), "Region code should be set from patch");
        assertEquals(50L, addr.getRegionId(), "Region id should be set from patch");
    }

    @Test
    void patchPropertyDetails_withNullRegionFields_doesNotOverwrite() {
        // Arrange
        AddressData existingAddr = new AddressData();
        existingAddr.setRegion("Existing Region");
        existingAddr.setRegionCode("EX_REG");
        existingAddr.setRegionId(99L);
        Lead.PropertyDetails existing = new Lead.PropertyDetails();
        existing.setAddress(existingAddr);
        lead.setOtherDetails(Lead.OtherDetails.builder().propertyDetails(existing).build());

        PatchAddressData patchAddr = PatchAddressData.builder()
                .pincode(Optional.of("560001"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .address(Optional.of(patchAddr))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchPropertyDetails(leadIdentifier, request);

        // Assert
        AddressData addr = lead.getOtherDetails().getPropertyDetails().getAddress();
        assertEquals("Existing Region", addr.getRegion(), "Region name should not be overwritten when not patched");
        assertEquals("EX_REG", addr.getRegionCode(), "Region code should not be overwritten when not patched");
        assertEquals(99L, addr.getRegionId(), "Region id should not be overwritten when not patched");
    }

    @Test
    void patchPropertyDetails_withInvalidPropertyTax_throwsBadRequestException() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .propertyTax(Optional.of("INVALID"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchPropertyDetails(leadIdentifier, request),
                "Should throw when propertyTax status is invalid");
    }

    @Test
    void patchPropertyDetails_withInvalidStatementOfAccounts_throwsBadRequestException() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .statementOfAccounts(Optional.of("INVALID"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchPropertyDetails(leadIdentifier, request),
                "Should throw when statementOfAccounts status is invalid");
    }

    @Test
    void patchPropertyDetails_withInvalidOtherDocs_throwsBadRequestException() {
        // Arrange
        PatchDocumentChecklistRequest checklist = PatchDocumentChecklistRequest.builder()
                .otherDocs(Optional.of("INVALID"))
                .build();

        PatchPropertyDetailsRequest request = PatchPropertyDetailsRequest.builder()
                .documentChecklist(Optional.of(checklist)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchPropertyDetails(leadIdentifier, request),
                "Should throw when otherDocs status is invalid");
    }

    // ==================== patchIncomeObligationDetails() Tests ====================

    @Test
    void patchIncomeObligationDetails_withIncomeDetails_setsAndPublishes() {
        // Arrange
        PatchIncomeAndObligationRequest.IncomeDetailsData incomeData =
                PatchIncomeAndObligationRequest.IncomeDetailsData.builder()
                        .incomeSource("SALARY").amount(BigDecimal.valueOf(50000)).build();

        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.of(List.of(incomeData)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey("SALARY")).thenReturn(new CodeValueResponse());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchIncomeObligationDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getIncomeObligationDetails(), "Income obligation details should be set");
        assertEquals(1, lead.getIncomeObligationDetails().getIncomeDetails().size());
        assertEquals("SALARY", lead.getIncomeObligationDetails().getIncomeDetails().get(0).getIncomeSource());
        verify(analyticsHelper).captureLead(any());
    }

    @Test
    void patchIncomeObligationDetails_withObligationDetails_setsObligation() {
        // Arrange
        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.empty())
                .obligationDetails(Optional.of(
                        PatchIncomeAndObligationRequest.ObligationData.builder()
                                .existingEmi(BigDecimal.valueOf(15000)).build()))
                .monthlyFamilyIncome(Optional.of(BigDecimal.valueOf(80000)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchIncomeObligationDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getIncomeObligationDetails().getObligationDetails());
        assertEquals(BigDecimal.valueOf(15000), lead.getIncomeObligationDetails().getObligationDetails().getExistingEmi());
        assertEquals(BigDecimal.valueOf(80000), lead.getIncomeObligationDetails().getMonthlyFamilyIncome());
    }

    @Test
    void patchIncomeObligationDetails_withExistingDetailsPreserved_keepsExisting() {
        // Arrange
        Lead.IncomeObligationDetails existing = Lead.IncomeObligationDetails.builder()
                .incomeDetails(List.of(Lead.IncomeDetails.builder().incomeSource("BUSINESS").amount(BigDecimal.valueOf(100000)).build()))
                .obligationDetails(Lead.ObligationDetails.builder().existingEmi(BigDecimal.valueOf(5000)).build())
                .monthlyFamilyIncome(BigDecimal.valueOf(120000))
                .build();
        lead.setIncomeObligationDetails(existing);

        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.empty())
                .obligationDetails(Optional.empty())
                .monthlyFamilyIncome(Optional.empty())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchIncomeObligationDetails(leadIdentifier, request);

        // Assert
        assertEquals("BUSINESS", lead.getIncomeObligationDetails().getIncomeDetails().get(0).getIncomeSource(),
                "Existing income details should be preserved when Optional.empty()");
        assertEquals(BigDecimal.valueOf(5000), lead.getIncomeObligationDetails().getObligationDetails().getExistingEmi(),
                "Existing obligation should be preserved when Optional.empty()");
        assertEquals(BigDecimal.valueOf(120000), lead.getIncomeObligationDetails().getMonthlyFamilyIncome(),
                "Existing monthly family income should be preserved when Optional.empty()");
    }

    @Test
    void patchIncomeObligationDetails_withIncomeDocumentChecklist_validatesAndSets() {
        // Arrange
        PatchIncomeAndObligationRequest.IncomeDocumentChecklistData checklistItem =
                PatchIncomeAndObligationRequest.IncomeDocumentChecklistData.builder()
                        .documentType("SALARY_SLIP").status("AVAILABLE").build();

        PatchIncomeAndObligationRequest.IncomeDetailsData incomeData =
                PatchIncomeAndObligationRequest.IncomeDetailsData.builder()
                        .incomeSource("SALARY").amount(BigDecimal.valueOf(50000))
                        .documentChecklist(List.of(checklistItem)).build();

        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.of(List.of(incomeData))).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey(anyString())).thenReturn(new CodeValueResponse());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchIncomeObligationDetails(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getIncomeObligationDetails().getIncomeDetails().get(0).getDocumentChecklist(),
                "Income document checklist should be mapped");
    }

    @Test
    void patchIncomeObligationDetails_withInvalidDocumentChecklistStatus_throwsBadRequest() {
        // Arrange
        PatchIncomeAndObligationRequest.IncomeDocumentChecklistData checklistItem =
                PatchIncomeAndObligationRequest.IncomeDocumentChecklistData.builder()
                        .documentType("SALARY_SLIP").status("INVALID_STATUS").build();

        PatchIncomeAndObligationRequest.IncomeDetailsData incomeData =
                PatchIncomeAndObligationRequest.IncomeDetailsData.builder()
                        .incomeSource("SALARY").documentChecklist(List.of(checklistItem)).build();

        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.of(List.of(incomeData))).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey(anyString())).thenReturn(new CodeValueResponse());

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchIncomeObligationDetails(leadIdentifier, request),
                "Should throw when document checklist status is invalid");
    }

    @Test
    void patchIncomeObligationDetails_withNullDocumentType_throwsBadRequest() {
        // Arrange
        PatchIncomeAndObligationRequest.IncomeDocumentChecklistData checklistItem =
                PatchIncomeAndObligationRequest.IncomeDocumentChecklistData.builder()
                        .documentType(null).status("AVAILABLE").build();

        PatchIncomeAndObligationRequest.IncomeDetailsData incomeData =
                PatchIncomeAndObligationRequest.IncomeDetailsData.builder()
                        .incomeSource("SALARY").documentChecklist(List.of(checklistItem)).build();

        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.of(List.of(incomeData))).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey(anyString())).thenReturn(new CodeValueResponse());

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchIncomeObligationDetails(leadIdentifier, request),
                "Should throw when document type is null");
    }

    @Test
    void patchIncomeObligationDetails_withNullDocumentStatus_throwsBadRequest() {
        // Arrange
        PatchIncomeAndObligationRequest.IncomeDocumentChecklistData checklistItem =
                PatchIncomeAndObligationRequest.IncomeDocumentChecklistData.builder()
                        .documentType("SALARY_SLIP").status(null).build();

        PatchIncomeAndObligationRequest.IncomeDetailsData incomeData =
                PatchIncomeAndObligationRequest.IncomeDetailsData.builder()
                        .incomeSource("SALARY").documentChecklist(List.of(checklistItem)).build();

        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.of(List.of(incomeData))).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey(anyString())).thenReturn(new CodeValueResponse());

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchIncomeObligationDetails(leadIdentifier, request),
                "Should throw when document status is null");
    }

    @Test
    void patchIncomeObligationDetails_withClearingObligationDetails_setsNull() {
        // Arrange
        PatchIncomeAndObligationRequest request = PatchIncomeAndObligationRequest.builder()
                .incomeDetails(Optional.empty())
                .obligationDetails(Optional.empty())
                .monthlyFamilyIncome(Optional.empty())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchIncomeObligationDetails(leadIdentifier, request);

        // Assert
        assertNull(lead.getIncomeObligationDetails().getObligationDetails(),
                "Obligation details should be null when cleared");
        assertNull(lead.getIncomeObligationDetails().getMonthlyFamilyIncome(),
                "Monthly family income should be null when cleared");
    }

    // ==================== patchLead() Tests ====================

    @Test
    void patchLead_withPropertyAndIncomeDetails_updatesLead() {
        // Arrange
        PatchPropertyDetailsRequest propRequest = PatchPropertyDetailsRequest.builder()
                .owner(Optional.of("Owner1")).build();

        PatchIncomeAndObligationRequest incomeRequest = PatchIncomeAndObligationRequest.builder()
                .monthlyFamilyIncome(Optional.of(BigDecimal.valueOf(60000))).build();

        PatchLeadRequest request = PatchLeadRequest.builder()
                .propertyDetails(Optional.of(propRequest))
                .incomeAndObligationDetails(Optional.of(incomeRequest))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertEquals("Owner1", lead.getOtherDetails().getPropertyDetails().getOwner());
        assertEquals(BigDecimal.valueOf(60000), lead.getIncomeObligationDetails().getMonthlyFamilyIncome());
    }

    @Test
    void patchLead_withValidCustomerFormStep_setsStep() {
        // Arrange
        PatchLeadRequest request = PatchLeadRequest.builder()
                .currentCustomerFormStep(Optional.of("basicInfo"))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertEquals("basicInfo", lead.getOtherDetails().getCurrentCustomerFormStep());
    }

    @Test
    void patchLead_withInvalidCustomerFormStep_throwsBadRequest() {
        // Arrange
        PatchLeadRequest request = PatchLeadRequest.builder()
                .currentCustomerFormStep(Optional.of("INVALID_STEP"))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchLead(leadIdentifier, request),
                "Should throw when customer form step is invalid");
    }

    @Test
    void patchLead_withNullCustomerFormStep_clearsStep() {
        // Arrange
        PatchLeadRequest request = PatchLeadRequest.builder()
                .currentCustomerFormStep(Optional.empty())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertNull(lead.getOtherDetails().getCurrentCustomerFormStep(),
                "Customer form step should be cleared");
    }

    @Test
    void patchLead_withProductCode_validatesAndSets() {
        // Arrange
        PatchLeadRequest request = PatchLeadRequest.builder()
                .productCode(Optional.of("HL"))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertEquals("HL", lead.getProductCode(), "Product code should be set");
        verify(productReadService).getProductByCode("HL");
    }

    @Test
    void patchLead_withNullProductCode_clearsProductCode() {
        // Arrange
        lead.setProductCode("OLD_PRODUCT");
        PatchLeadRequest request = PatchLeadRequest.builder()
                .productCode(Optional.empty())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertNull(lead.getProductCode(), "Product code should be cleared when Optional is empty");
        verify(productReadService, never()).getProductByCode(anyString());
    }

    @Test
    void patchLead_withRequestedAmount_setsAmount() {
        // Arrange
        PatchLeadRequest request = PatchLeadRequest.builder()
                .requestedAmount(Optional.of(BigDecimal.valueOf(5000000)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertEquals(BigDecimal.valueOf(5000000), lead.getRequestedAmount());
    }

    @Test
    void patchLead_withPreferredCallTimes_setsBothTimes() {
        // Arrange
        PatchLeadRequest request = PatchLeadRequest.builder()
                .preferredCallStartTime(Optional.of(LocalTime.of(10, 0)))
                .preferredCallEndTime(Optional.of(LocalTime.of(18, 0)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertEquals(LocalTime.of(10, 0), lead.getOtherDetails().getPreferredCallStartTime(),
                "Preferred call start time should be updated from patch request");
        assertEquals(LocalTime.of(18, 0), lead.getOtherDetails().getPreferredCallEndTime(),
                "Preferred call end time should be updated from patch request");
    }

    @Test
    void patchLead_withOnlyPreferredCallStartTime_retainsExistingEndTime() {
        // Arrange
        lead.setOtherDetails(Lead.OtherDetails.builder().build());
        lead.getOtherDetails().setPreferredCallEndTime(LocalTime.of(17, 0));
        PatchLeadRequest request = PatchLeadRequest.builder()
                .preferredCallStartTime(Optional.of(LocalTime.of(9, 30)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertEquals(LocalTime.of(9, 30), lead.getOtherDetails().getPreferredCallStartTime(),
                "Patch should update preferred call start time when it is provided");
        assertEquals(LocalTime.of(17, 0), lead.getOtherDetails().getPreferredCallEndTime(),
                "Patch should retain preferred call end time when it is not provided");
    }

    @Test
    void patchLead_withInvalidPreferredCallTimesAfterMerge_throwsBadRequest() {
        // Arrange
        lead.setOtherDetails(Lead.OtherDetails.builder().build());
        lead.getOtherDetails().setPreferredCallEndTime(LocalTime.of(9, 0));
        PatchLeadRequest request = PatchLeadRequest.builder()
                .preferredCallStartTime(Optional.of(LocalTime.of(10, 0)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> leadWriteService.patchLead(leadIdentifier, request),
                "Patch should reject preferred call times when merged start time is after end time");
    }

    @Test
    void patchLead_withNullOtherDetails_initializesOtherDetails() {
        // Arrange
        lead.setOtherDetails(null);
        PatchLeadRequest request = PatchLeadRequest.builder()
                .requestedAmount(Optional.of(BigDecimal.valueOf(1000000)))
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadWriteService.patchLead(leadIdentifier, request);

        // Assert
        assertNotNull(lead.getOtherDetails(), "OtherDetails should be initialized");
    }
}
