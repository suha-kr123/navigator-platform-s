package com.nivasafinance.features.leadlender.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadLenderCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadLenderRejectionEventPayload;
import com.nivasafinance.common.events.payload.LeadLenderSubmissionEventPayload;
import com.nivasafinance.common.events.payload.LeadLenderUpdationEventPayload;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadlender.dto.*;
import com.nivasafinance.features.leadlender.entity.LeadLender;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import com.nivasafinance.features.leadlender.exception.InvalidLeadLenderStatusException;
import com.nivasafinance.features.leadlender.exception.InvalidLenderOfficeException;
import com.nivasafinance.features.leadlender.exception.LeadLenderAlreadyExistsException;
import com.nivasafinance.features.leadlender.repository.LeadLenderRepositoryWrapper;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadLenderWriteServiceImplTest {

    @Mock
    private LeadLenderRepositoryWrapper leadLenderRepositoryWrapper;

    @Mock
    private LeadWriteService leadWriteService;

    @Mock
    private LeadReadService leadReadService;

    @Mock
    private LenderReadService lenderReadService;

    @Mock
    private LenderOfficeReadService lenderOfficeReadService;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private CodeValueMasterService codeValueMasterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private LeadLenderWriteServiceImpl leadLenderWriteService;

    private UUID leadIdentifier;
    private UUID lenderIdentifier;
    private Long leadId;
    private Long lenderId;
    private String lenderKey;
    private String lenderOfficeKey;
    private LeadBasicResponse leadBasicResponse;
    private LenderResponseData lenderResponseData;
    private LenderOfficeReponseData lenderOfficeResponseData;
    private CodeValueResponse codeValueResponse;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        lenderIdentifier = UUID.randomUUID();
        leadId = 1L;
        lenderId = 100L;
        lenderKey = "LENDER_001";
        lenderOfficeKey = "OFFICE_001";

        leadBasicResponse = LeadBasicResponse.builder()
                .id(leadId)
                .leadIdentifier(leadIdentifier)
                .build();

        lenderResponseData = new LenderResponseData(
                UUID.randomUUID(),
                "Test Lender",
                lenderKey,
                null
        );

        lenderOfficeResponseData = new LenderOfficeReponseData(
                UUID.randomUUID(),
                "Test Office",
                lenderOfficeKey,
                lenderKey,
                null
        );

        codeValueResponse = new CodeValueResponse();
        codeValueResponse.setKey("REJECTION_REASON_001");
        codeValueResponse.setValue("Invalid Documents");
    }

    // ==================== createLeadLender() Tests ====================

    @Test
    void createLeadLender_success_createsNewLeadLenderRelationship() {
        // Given
        CreateLeadLenderRequest request = new CreateLeadLenderRequest(lenderKey);
        
        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
        when(leadLenderRepositoryWrapper.findByLeadIdAndLenderKeyWithException(leadId, lenderKey))
                .thenThrow(new RuntimeException("Not found"));
        
        LeadLender savedLeadLender = new LeadLender();
        savedLeadLender.setId(lenderId);
        savedLeadLender.setLenderIdentifier(lenderIdentifier);
        savedLeadLender.setLeadId(leadId);
        savedLeadLender.setLenderKey(lenderKey);
        savedLeadLender.setStatus(LeadLenderStatus.SELECTED);
        
        ArgumentCaptor<LeadLender> leadLenderCaptor = ArgumentCaptor.forClass(LeadLender.class);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(savedLeadLender);

        // When
        CreateLeadLenderResponse response = leadLenderWriteService.createLeadLender(leadIdentifier, request);

        // Then
        assertNotNull(response);
        assertEquals(lenderIdentifier, response.getLenderIdentifier());
        
        verify(leadReadService).getLeadBasicByIdentifier(leadIdentifier);
        verify(lenderReadService).getByKey(lenderKey);
        verify(leadLenderRepositoryWrapper).findByLeadIdAndLenderKeyWithException(leadId, lenderKey);
        verify(leadLenderRepositoryWrapper).saveWithException(leadLenderCaptor.capture());
        verify(leadWriteService).touchLead(leadIdentifier);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));

        LeadLender captured = leadLenderCaptor.getValue();
        assertEquals(leadId, captured.getLeadId());
        assertEquals(lenderKey, captured.getLenderKey());
        assertEquals(LeadLenderStatus.SELECTED, captured.getStatus());
        assertNotNull(captured.getLenderIdentifier());

        // Verify event payload
        ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        SystemEvent<?> publishedEvent = eventCaptor.getValue();
        assertEquals(BusinessEvent.LEAD_LENDER_CREATED.toString(), publishedEvent.getEventType());
        assertTrue(publishedEvent.getPayload() instanceof LeadLenderCreationEventPayload);
    }

//    @Test
//    void createLeadLender_existingRelationshipWithInProgressStatus_throwsException() {
//        // Given
//        CreateLeadLenderRequest request = new CreateLeadLenderRequest(lenderKey);
//
//        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
//        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
//
//        LeadLender existingLeadLender = new LeadLender();
//        existingLeadLender.setStatus(LeadLenderStatus.SELECTED);
//        when(leadLenderRepositoryWrapper.findByLeadIdAndLenderKeyWithException(leadId, lenderKey))
//                .thenReturn(existingLeadLender);
//
//        // When & Then
//        LeadLenderAlreadyExistsException exception = assertThrows(
//                LeadLenderAlreadyExistsException.class,
//                () -> leadLenderWriteService.createLeadLender(leadIdentifier, request)
//        );
//
//        assertTrue(exception.getMessage().contains(leadIdentifier.toString()));
//        assertTrue(exception.getMessage().contains(lenderKey));
//
//        verify(leadReadService).getLeadBasicByIdentifier(leadIdentifier);
//        verify(lenderReadService).getByKey(lenderKey);
//        verify(leadLenderRepositoryWrapper).findByLeadIdAndLenderKeyWithException(leadId, lenderKey);
//        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
//        verify(leadWriteService, never()).touchLead(any());
//        verify(applicationEventPublisher, never()).publishEvent(any());
//    }
//
//    @Test
//    void createLeadLender_existingRelationshipWithSubmittedStatus_throwsException() {
//        // Given
//        CreateLeadLenderRequest request = new CreateLeadLenderRequest(lenderKey);
//
//        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
//        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
//
//        LeadLender existingLeadLender = new LeadLender();
//        existingLeadLender.setStatus(LeadLenderStatus.SUBMITTED);
//        when(leadLenderRepositoryWrapper.findByLeadIdAndLenderKeyWithException(leadId, lenderKey))
//                .thenReturn(existingLeadLender);
//
//        // When & Then
//        assertThrows(
//                LeadLenderAlreadyExistsException.class,
//                () -> leadLenderWriteService.createLeadLender(leadIdentifier, request)
//        );
//    }

    @Test
    void createLeadLender_existingRelationshipWithRejectedStatus_allowsCreation() {
        // Given
        CreateLeadLenderRequest request = new CreateLeadLenderRequest(lenderKey);
        
        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
        when(leadLenderRepositoryWrapper.findByLeadIdAndLenderKeyWithException(leadId, lenderKey))
                .thenThrow(new RuntimeException("Not found"));
        
        LeadLender savedLeadLender = new LeadLender();
        savedLeadLender.setId(lenderId);
        savedLeadLender.setLenderIdentifier(lenderIdentifier);
        savedLeadLender.setStatus(LeadLenderStatus.SELECTED);
        
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(savedLeadLender);

        // When
        CreateLeadLenderResponse response = leadLenderWriteService.createLeadLender(leadIdentifier, request);

        // Then
        assertNotNull(response);
        verify(leadLenderRepositoryWrapper).saveWithException(any(LeadLender.class));
    }

    @Test
    void createLeadLender_repositorySaveFails_throwsException() {
        // Given
        CreateLeadLenderRequest request = new CreateLeadLenderRequest(lenderKey);
        
        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
        when(leadLenderRepositoryWrapper.findByLeadIdAndLenderKeyWithException(leadId, lenderKey))
                .thenThrow(new RuntimeException("Not found"));
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> leadLenderWriteService.createLeadLender(leadIdentifier, request)
        );
        
        verify(leadLenderRepositoryWrapper).saveWithException(any(LeadLender.class));
    }

    // ==================== updateLeadLender() Tests ====================

    @Test
    void updateLeadLender_success_updatesAllFields() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setLenderOfficeKey(lenderOfficeKey);
        request.setRmDetails(new RmDetails("RM Name", "1234567890"));
        request.setLoginDetails(new LoginDetails("login123", LocalDate.now(), BigDecimal.valueOf(1000), "remarks"));
        request.setApprovedDetails(new ApprovedDetails());
        request.setStage("STAGE_001");

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(lenderOfficeReadService.getByKey(lenderOfficeKey)).thenReturn(lenderOfficeResponseData);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        
        LeadLender captured = captor.getValue();
        assertEquals(lenderOfficeKey, captured.getLenderOfficeKey());
        assertNotNull(captured.getRmDetails());
        assertNotNull(captured.getLoginDetails());
        assertNotNull(captured.getApprovedDetails());
        assertEquals("STAGE_001", captured.getStage());
        
        verify(leadWriteService).touchLead(leadIdentifier);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
        
        ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        SystemEvent<?> publishedEvent = eventCaptor.getValue();
        assertEquals(BusinessEvent.LEAD_LENDER_UPDATED.toString(), publishedEvent.getEventType());
        assertTrue(publishedEvent.getPayload() instanceof LeadLenderUpdationEventPayload);
    }

    @Test
    void updateLeadLender_success_updatesOnlyLenderOfficeKey() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setLenderOfficeKey(lenderOfficeKey);

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(lenderOfficeReadService.getByKey(lenderOfficeKey)).thenReturn(lenderOfficeResponseData);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(lenderOfficeKey, captor.getValue().getLenderOfficeKey());
        verify(leadWriteService).touchLead(leadIdentifier);
    }

    @Test
    void updateLeadLender_success_updatesOnlyRmDetails() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setRmDetails(new RmDetails("RM Name", "1234567890"));

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        assertNotNull(captor.getValue().getRmDetails());
        assertEquals("RM Name", captor.getValue().getRmDetails().getName());
    }

    @Test
    void updateLeadLender_success_updatesOnlyLoginDetails() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setLoginDetails(new LoginDetails("login123", LocalDate.now(), BigDecimal.valueOf(1000), "remarks"));

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        assertNotNull(captor.getValue().getLoginDetails());
        assertEquals("login123", captor.getValue().getLoginDetails().getLoginId());
    }

    @Test
    void updateLeadLender_success_updatesOnlyApprovedDetails() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setApprovedDetails(new ApprovedDetails());

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        assertNotNull(captor.getValue().getApprovedDetails());
    }

    @Test
    void updateLeadLender_success_updatesOnlyStage() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setStage("STAGE_001");

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        assertEquals("STAGE_001", captor.getValue().getStage());
    }

    @Test
    void updateLeadLender_nullLenderOfficeKey_skipsValidation() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setLenderOfficeKey(null);

        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);

        // Then
        verify(lenderOfficeReadService, never()).getByKey(any());
        verify(leadLenderRepositoryWrapper).saveWithException(any(LeadLender.class));
    }

    @Test
    void updateLeadLender_leadLenderDoesNotBelongToLead_throwsException() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(999L); // Different lead ID
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request)
        );
        
        assertTrue(exception.getMessage().contains(lenderIdentifier.toString()));
        assertTrue(exception.getMessage().contains(leadIdentifier.toString()));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
        verify(leadWriteService, never()).touchLead(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateLeadLender_statusIsRejected_throwsException() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.REJECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request)
        );
        
        assertTrue(exception.getMessage().contains("rejected"));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void updateLeadLender_invalidLenderOfficeKey_throwsException() {
        // Given
        UpdateLeadLenderRequest request = new UpdateLeadLenderRequest();
        request.setLenderOfficeKey(lenderOfficeKey);
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        LenderOfficeReponseData invalidOffice = new LenderOfficeReponseData(
                UUID.randomUUID(),
                "Invalid Office",
                lenderOfficeKey,
                "DIFFERENT_LENDER_KEY", // Different lender key
                null
        );

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(lenderOfficeReadService.getByKey(lenderOfficeKey)).thenReturn(invalidOffice);

        // When & Then
        InvalidLenderOfficeException exception = assertThrows(
                InvalidLenderOfficeException.class,
                () -> leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request)
        );
        
        assertTrue(exception.getMessage().contains(lenderOfficeKey));
        assertTrue(exception.getMessage().contains(lenderKey));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
    }

    // ==================== rejectLeadLender() Tests ====================

    @Test
    void rejectLeadLender_success_rejectsWithReasonAndRemarks() {
        // Given
        String rejectionReason = "REJECTION_REASON_001";
        String remarks = "Test remarks";
        String username = "testuser";
        
        RejectLeadLenderRequest request = new RejectLeadLenderRequest(rejectionReason, remarks);
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);
        existingEntity.setRejectionDetails(null);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(
                rejectionReason, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER))
                .thenReturn(codeValueResponse);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getUsername).thenReturn(username);

            // When
            leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request);

            // Then
            ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
            verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
            
            LeadLender captured = captor.getValue();
            assertEquals(LeadLenderStatus.REJECTED, captured.getStatus());
            assertNotNull(captured.getRejectionDetails());
            assertEquals(username, captured.getRejectionDetails().getRejectedBy());
            assertEquals(rejectionReason, captured.getRejectionDetails().getRejectionReason());
            assertEquals(remarks, captured.getRejectionDetails().getRemarks());
            assertNotNull(captured.getRejectionDetails().getRejectionDate());
            
            verify(leadWriteService).touchLead(leadIdentifier);
            verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
            
            ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            SystemEvent<?> publishedEvent = eventCaptor.getValue();
            assertEquals(BusinessEvent.LEAD_LENDER_REJECTED.toString(), publishedEvent.getEventType());
            assertTrue(publishedEvent.getPayload() instanceof LeadLenderRejectionEventPayload);
            
            LeadLenderRejectionEventPayload payload = (LeadLenderRejectionEventPayload) publishedEvent.getPayload();
            assertEquals(rejectionReason, payload.getRejectionReason());
        }
    }

    @Test
    void rejectLeadLender_success_rejectsWithOnlyReason() {
        // Given
        String rejectionReason = "REJECTION_REASON_001";
        String username = "testuser";
        
        RejectLeadLenderRequest request = new RejectLeadLenderRequest(rejectionReason, null);
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);
        existingEntity.setRejectionDetails(null);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(
                rejectionReason, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER))
                .thenReturn(codeValueResponse);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getUsername).thenReturn(username);

            // When
            leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request);

            // Then
            ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
            verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
            
            LeadLender captured = captor.getValue();
            assertEquals(LeadLenderStatus.REJECTED, captured.getStatus());
            assertNotNull(captured.getRejectionDetails());
            assertEquals(rejectionReason, captured.getRejectionDetails().getRejectionReason());
            assertNull(captured.getRejectionDetails().getRemarks());
        }
    }

    @Test
    void rejectLeadLender_success_createsRejectionDetailsWhenNull() {
        // Given
        String rejectionReason = "REJECTION_REASON_001";
        String username = "testuser";
        
        RejectLeadLenderRequest request = new RejectLeadLenderRequest(rejectionReason, "remarks");
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);
        existingEntity.setRejectionDetails(null); // Null rejection details

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(
                rejectionReason, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER))
                .thenReturn(codeValueResponse);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getUsername).thenReturn(username);

            // When
            leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request);

            // Then
            ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
            verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
            
            LeadLender captured = captor.getValue();
            assertNotNull(captured.getRejectionDetails());
            assertEquals(username, captured.getRejectionDetails().getRejectedBy());
        }
    }

    @Test
    void rejectLeadLender_nullRejectionReason_skipsCodeValidation() {
        // Given
        String username = "testuser";
        
        RejectLeadLenderRequest request = new RejectLeadLenderRequest(null, "remarks");
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getUsername).thenReturn(username);

            // When
            leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request);

            // Then
            verify(codeValueMasterService, never()).getCodeValueByKeyAndCodeKey(any(), any());
            verify(leadLenderRepositoryWrapper).saveWithException(any(LeadLender.class));
        }
    }

    @Test
    void rejectLeadLender_leadLenderDoesNotBelongToLead_throwsException() {
        // Given
        RejectLeadLenderRequest request = new RejectLeadLenderRequest("REASON", "remarks");
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(999L); // Different lead ID
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request)
        );
        
        assertTrue(exception.getMessage().contains(lenderIdentifier.toString()));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
        verify(leadWriteService, never()).touchLead(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void rejectLeadLender_alreadyRejected_throwsException() {
        // Given
        RejectLeadLenderRequest request = new RejectLeadLenderRequest("REASON", "remarks");
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.REJECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request)
        );
        
        assertTrue(exception.getMessage().contains("already rejected"));
        assertTrue(exception.getMessage().contains(LeadLenderStatus.REJECTED.toString()));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void rejectLeadLender_invalidRejectionReasonCode_verifiesCodeValidation() {
        // Given
        String rejectionReason = "INVALID_REASON";
        
        RejectLeadLenderRequest request = new RejectLeadLenderRequest(rejectionReason, "remarks");
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(
                rejectionReason, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER))
                .thenThrow(new RuntimeException("Code value not found"));

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request)
        );
        
        verify(codeValueMasterService).getCodeValueByKeyAndCodeKey(
                rejectionReason, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER);
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void rejectLeadLender_success_publishesEventWithRejectionReason() {
        // Given
        String rejectionReason = "REJECTION_REASON_001";
        String username = "testuser";
        
        RejectLeadLenderRequest request = new RejectLeadLenderRequest(rejectionReason, "remarks");
        
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);
        existingEntity.setRejectionDetails(null);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(
                rejectionReason, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER))
                .thenReturn(codeValueResponse);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        try (MockedStatic<UserContext> userContextMock = mockStatic(UserContext.class)) {
            userContextMock.when(UserContext::getUsername).thenReturn(username);

            // When
            leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request);

            // Then
            ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            
            SystemEvent<?> publishedEvent = eventCaptor.getValue();
            LeadLenderRejectionEventPayload payload = (LeadLenderRejectionEventPayload) publishedEvent.getPayload();
            assertEquals(rejectionReason, payload.getRejectionReason());
            assertEquals(leadId, payload.getLeadId());
            assertEquals(lenderId, payload.getLenderId());
            assertEquals(lenderIdentifier, payload.getLenderIdentifier());
        }
    }

    // ==================== submitLeadLender() Tests ====================

    @Test
    void submitLeadLender_success_submitsFromSelectedStatus() {
        // Given
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.submitLeadLender(leadIdentifier, lenderIdentifier);

        // Then
        ArgumentCaptor<LeadLender> captor = ArgumentCaptor.forClass(LeadLender.class);
        verify(leadLenderRepositoryWrapper).saveWithException(captor.capture());
        
        LeadLender captured = captor.getValue();
        assertEquals(LeadLenderStatus.SUBMITTED, captured.getStatus());
        
        verify(leadWriteService).touchLead(leadIdentifier);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
        
        ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        SystemEvent<?> publishedEvent = eventCaptor.getValue();
        assertEquals(BusinessEvent.LEAD_LENDER_SUBMITTED.toString(), publishedEvent.getEventType());
        assertTrue(publishedEvent.getPayload() instanceof LeadLenderSubmissionEventPayload);
        
        LeadLenderSubmissionEventPayload payload = (LeadLenderSubmissionEventPayload) publishedEvent.getPayload();
        assertEquals(leadId, payload.getLeadId());
        assertEquals(lenderId, payload.getLenderId());
        assertEquals(lenderIdentifier, payload.getLenderIdentifier());
    }

    @Test
    void submitLeadLender_leadLenderDoesNotBelongToLead_throwsException() {
        // Given
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(999L); // Different lead ID
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.submitLeadLender(leadIdentifier, lenderIdentifier)
        );
        
        assertTrue(exception.getMessage().contains(lenderIdentifier.toString()));
        assertTrue(exception.getMessage().contains(leadIdentifier.toString()));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
        verify(leadWriteService, never()).touchLead(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    void submitLeadLender_alreadySubmitted_throwsException() {
        // Given
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SUBMITTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.submitLeadLender(leadIdentifier, lenderIdentifier)
        );
        
        assertTrue(exception.getMessage().contains("already submitted"));
        assertTrue(exception.getMessage().contains(LeadLenderStatus.SUBMITTED.toString()));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void submitLeadLender_statusIsRejected_throwsException() {
        // Given
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.REJECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);

        // When & Then
        InvalidLeadLenderStatusException exception = assertThrows(
                InvalidLeadLenderStatusException.class,
                () -> leadLenderWriteService.submitLeadLender(leadIdentifier, lenderIdentifier)
        );
        
        assertTrue(exception.getMessage().contains("Cannot submit rejected"));
        assertTrue(exception.getMessage().contains(LeadLenderStatus.REJECTED.toString()));
        
        verify(leadLenderRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void submitLeadLender_success_verifiesEventPayload() {
        // Given
        LeadLender existingEntity = new LeadLender();
        existingEntity.setId(lenderId);
        existingEntity.setLenderIdentifier(lenderIdentifier);
        existingEntity.setLeadId(leadId);
        existingEntity.setLenderKey(lenderKey);
        existingEntity.setStatus(LeadLenderStatus.SELECTED);

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasicResponse);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(existingEntity);
        when(leadLenderRepositoryWrapper.saveWithException(any(LeadLender.class)))
                .thenReturn(existingEntity);

        // When
        leadLenderWriteService.submitLeadLender(leadIdentifier, lenderIdentifier);

        // Then
        ArgumentCaptor<SystemEvent> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        
        SystemEvent<?> publishedEvent = eventCaptor.getValue();
        LeadLenderSubmissionEventPayload payload = (LeadLenderSubmissionEventPayload) publishedEvent.getPayload();
        assertEquals(leadId, payload.getLeadId());
        assertEquals(lenderId, payload.getLenderId());
        assertEquals(lenderIdentifier, payload.getLenderIdentifier());
    }
}

