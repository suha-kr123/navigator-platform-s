package com.nivasafinance.features.leadlender.service.impl;

import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadlender.dto.LeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.RejectionDetails;
import com.nivasafinance.features.leadlender.entity.LeadLender;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import com.nivasafinance.features.leadlender.exception.LeadLenderNotFoundException;
import com.nivasafinance.features.leadlender.repository.LeadLenderRepositoryWrapper;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadLenderReadServiceImplTest {

    @Mock
    private LeadLenderRepositoryWrapper leadLenderRepositoryWrapper;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private LenderReadService lenderReadService;

    @Mock
    private LenderOfficeReadService lenderOfficeReadService;

    @Mock
    private CodeValueMasterService codeValueMasterService;

    @InjectMocks
    private LeadLenderReadServiceImpl leadLenderReadService;

    private UUID leadIdentifier;
    private UUID lenderIdentifier;
    private Long leadId;
    private Long lenderId;
    private String lenderKey;
    private String lenderOfficeKey;
    private String stageKey;
    private Lead lead;
    private LenderResponseData lenderResponseData;
    private LenderOfficeReponseData lenderOfficeResponseData;
    private CodeValueResponse stageResponse;
    private CodeValueResponse rejectionReasonResponse;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        lenderIdentifier = UUID.randomUUID();
        leadId = 1L;
        lenderId = 100L;
        lenderKey = "LENDER_001";
        lenderOfficeKey = "OFFICE_001";
        stageKey = "STAGE_001";

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);

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

        stageResponse = new CodeValueResponse();
        stageResponse.setKey(stageKey);
        stageResponse.setValue("Stage 1");

        rejectionReasonResponse = new CodeValueResponse();
        rejectionReasonResponse.setKey("REJECTION_REASON_001");
        rejectionReasonResponse.setValue("Invalid Documents");
    }

    // ==================== getLeadLenders() Tests ====================

    @Test
    void getLeadLenders_success_returnsAllLeadLenders() {
        // Given
        LeadLender leadLender1 = createLeadLender(1L, lenderIdentifier, LeadLenderStatus.SELECTED);
        LeadLender leadLender2 = createLeadLender(2L, UUID.randomUUID(), LeadLenderStatus.SUBMITTED);
        List<LeadLender> leadLenders = List.of(leadLender1, leadLender2);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);
        when(leadRepositoryWrapper.findByIdWithException(any(Long.class))).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(leadIdentifier, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadLenderRepositoryWrapper).findByLeadId(leadId);
        verify(leadRepositoryWrapper, times(2)).findByIdWithException(any(Long.class));
        verify(lenderReadService, times(2)).getByKey(lenderKey);
    }

    @Test
    void getLeadLenders_success_withEmptyStatusList_returnsAllLeadLenders() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        List<LeadLender> leadLenders = List.of(leadLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(leadIdentifier, new ArrayList<>());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(leadLenderRepositoryWrapper).findByLeadId(leadId);
    }

    @Test
    void getLeadLenders_success_withStatusFilter_returnsFilteredLeadLenders() {
        // Given
        LeadLender selectedLender = createLeadLender(1L, lenderIdentifier, LeadLenderStatus.SELECTED);
        LeadLender submittedLender = createLeadLender(2L, UUID.randomUUID(), LeadLenderStatus.SUBMITTED);
        LeadLender rejectedLender = createLeadLender(3L, UUID.randomUUID(), LeadLenderStatus.REJECTED);
        List<LeadLender> allLeadLenders = List.of(selectedLender, submittedLender, rejectedLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(allLeadLenders);
        when(leadRepositoryWrapper.findByIdWithException(any(Long.class))).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(
                leadIdentifier, 
                List.of("SELECTED", "SUBMITTED")
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getStatus() == LeadLenderStatus.SELECTED));
        assertTrue(result.stream().anyMatch(r -> r.getStatus() == LeadLenderStatus.SUBMITTED));
        assertFalse(result.stream().anyMatch(r -> r.getStatus() == LeadLenderStatus.REJECTED));
    }

    @Test
    void getLeadLenders_success_withCaseInsensitiveStatusFilter() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        List<LeadLender> leadLenders = List.of(leadLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(
                leadIdentifier, 
                List.of("selected") // lowercase
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LeadLenderStatus.SELECTED, result.get(0).getStatus());
    }

    @Test
    void getLeadLenders_success_withNoMatchingStatus_returnsEmptyList() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        List<LeadLender> leadLenders = List.of(leadLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(
                leadIdentifier, 
                List.of("REJECTED")
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getLeadLenders_success_withAllOptionalFields() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        leadLender.setLenderOfficeKey(lenderOfficeKey);
        leadLender.setStage(stageKey);
        
        RejectionDetails rejectionDetails = new RejectionDetails();
        rejectionDetails.setRejectionReason("REJECTION_REASON_001");
        rejectionDetails.setRemarks("Test remarks");
        leadLender.setRejectionDetails(rejectionDetails);

        List<LeadLender> leadLenders = List.of(leadLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
        when(lenderOfficeReadService.getByKey(lenderOfficeKey)).thenReturn(lenderOfficeResponseData);
        when(codeValueMasterService.getByKey(stageKey)).thenReturn(stageResponse);
        when(codeValueMasterService.getByKey("REJECTION_REASON_001")).thenReturn(rejectionReasonResponse);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(leadIdentifier, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        LeadLenderResponse response = result.get(0);
        assertNotNull(response.getLenderOffice());
        assertNotNull(response.getStage());
        assertNotNull(response.getRejectionReason());
        assertEquals("Test remarks", response.getRejectionRemarks());
        
        verify(lenderOfficeReadService).getByKey(lenderOfficeKey);
        verify(codeValueMasterService).getByKey(stageKey);
        verify(codeValueMasterService).getByKey("REJECTION_REASON_001");
    }

    @Test
    void getLeadLenders_success_withNullOptionalFields() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        leadLender.setLenderOfficeKey(null);
        leadLender.setStage(null);
        leadLender.setRejectionDetails(null);

        List<LeadLender> leadLenders = List.of(leadLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(leadIdentifier, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        LeadLenderResponse response = result.get(0);
        assertNull(response.getLenderOffice());
        assertNull(response.getStage());
        assertNull(response.getRejectionReason());
        assertNull(response.getRejectionRemarks());
        
        verify(lenderOfficeReadService, never()).getByKey(any());
        verify(codeValueMasterService, never()).getByKey(any());
    }

    @Test
    void getLeadLenders_success_withNullRejectionReason() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.REJECTED);
        
        RejectionDetails rejectionDetails = new RejectionDetails();
        rejectionDetails.setRejectionReason(null);
        rejectionDetails.setRemarks("Test remarks");
        leadLender.setRejectionDetails(rejectionDetails);

        List<LeadLender> leadLenders = List.of(leadLender);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(leadLenders);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(leadIdentifier, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        LeadLenderResponse response = result.get(0);
        assertNull(response.getRejectionReason());
        assertEquals("Test remarks", response.getRejectionRemarks());
        
        verify(codeValueMasterService, never()).getByKey(any());
    }

    @Test
    void getLeadLenders_success_withEmptyList_returnsEmptyList() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLeadId(leadId)).thenReturn(new ArrayList<>());

        // When
        List<LeadLenderResponse> result = leadLenderReadService.getLeadLenders(leadIdentifier, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadLenderRepositoryWrapper).findByLeadId(leadId);
    }

    // ==================== getLeadLenderByIdentifier() Tests ====================

    @Test
    void getLeadLenderByIdentifier_success_returnsLeadLenderResponse() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(leadLender);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        LeadLenderResponse result = leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier);

        // Then
        assertNotNull(result);
        assertEquals(lenderIdentifier, result.getLenderIdentifier());
        assertEquals(leadIdentifier, result.getLeadIdentifier());
        assertEquals(LeadLenderStatus.SELECTED, result.getStatus());
        assertNotNull(result.getLender());
        
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadLenderRepositoryWrapper).findByLenderIdentifierWithException(lenderIdentifier);
        verify(leadRepositoryWrapper).findByIdWithException(leadId);
        verify(lenderReadService).getByKey(lenderKey);
    }

    @Test
    void getLeadLenderByIdentifier_success_withAllOptionalFields() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        leadLender.setLenderOfficeKey(lenderOfficeKey);
        leadLender.setStage(stageKey);
        
        RejectionDetails rejectionDetails = new RejectionDetails();
        rejectionDetails.setRejectionReason("REJECTION_REASON_001");
        rejectionDetails.setRemarks("Test remarks");
        leadLender.setRejectionDetails(rejectionDetails);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(leadLender);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
        when(lenderOfficeReadService.getByKey(lenderOfficeKey)).thenReturn(lenderOfficeResponseData);
        when(codeValueMasterService.getByKey(stageKey)).thenReturn(stageResponse);
        when(codeValueMasterService.getByKey("REJECTION_REASON_001")).thenReturn(rejectionReasonResponse);

        // When
        LeadLenderResponse result = leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier);

        // Then
        assertNotNull(result);
        assertNotNull(result.getLenderOffice());
        assertNotNull(result.getStage());
        assertNotNull(result.getRejectionReason());
        assertEquals("Test remarks", result.getRejectionRemarks());
        
        verify(lenderOfficeReadService).getByKey(lenderOfficeKey);
        verify(codeValueMasterService).getByKey(stageKey);
        verify(codeValueMasterService).getByKey("REJECTION_REASON_001");
    }

    @Test
    void getLeadLenderByIdentifier_lenderDoesNotBelongToLead_throwsException() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        leadLender.setLeadId(999L); // Different lead ID

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(leadLender);

        // When & Then
        LeadLenderNotFoundException exception = assertThrows(
                LeadLenderNotFoundException.class,
                () -> leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier)
        );

        assertTrue(exception.getMessage().contains(lenderIdentifier.toString()));
        assertTrue(exception.getMessage().contains(leadIdentifier.toString()));
        
        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadLenderRepositoryWrapper).findByLenderIdentifierWithException(lenderIdentifier);
        verify(leadRepositoryWrapper, never()).findByIdWithException(any());
        verify(lenderReadService, never()).getByKey(any());
    }

    @Test
    void getLeadLenderByIdentifier_leadNotFound_throwsException() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier))
                .thenThrow(new RuntimeException("Lead not found"));

        // When & Then
        assertThrows(
                RuntimeException.class,
                () -> leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier)
        );

        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadLenderRepositoryWrapper, never()).findByLenderIdentifierWithException(any());
    }

    @Test
    void getLeadLenderByIdentifier_lenderNotFound_throwsException() {
        // Given
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenThrow(new LeadLenderNotFoundException("Lender not found"));

        // When & Then
        assertThrows(
                LeadLenderNotFoundException.class,
                () -> leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier)
        );

        verify(leadRepositoryWrapper).findByLeadIdentifierWithException(leadIdentifier);
        verify(leadLenderRepositoryWrapper).findByLenderIdentifierWithException(lenderIdentifier);
    }

    @Test
    void getLeadLenderByIdentifier_success_withNullRejectionDetails() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.SELECTED);
        leadLender.setRejectionDetails(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(leadLender);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        LeadLenderResponse result = leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier);

        // Then
        assertNotNull(result);
        assertNull(result.getRejectionReason());
        assertNull(result.getRejectionRemarks());
        
        verify(codeValueMasterService, never()).getByKey(any());
    }

    @Test
    void getLeadLenderByIdentifier_success_withNullRejectionReasonInDetails() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.REJECTED);
        
        RejectionDetails rejectionDetails = new RejectionDetails();
        rejectionDetails.setRejectionReason(null);
        rejectionDetails.setRemarks("Test remarks");
        leadLender.setRejectionDetails(rejectionDetails);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(leadLender);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);

        // When
        LeadLenderResponse result = leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier);

        // Then
        assertNotNull(result);
        assertNull(result.getRejectionReason());
        assertEquals("Test remarks", result.getRejectionRemarks());
        
        verify(codeValueMasterService, never()).getByKey(any());
    }

    @Test
    void getLeadLenderByIdentifier_success_withNullRemarksInRejectionDetails() {
        // Given
        LeadLender leadLender = createLeadLender(lenderId, lenderIdentifier, LeadLenderStatus.REJECTED);
        
        RejectionDetails rejectionDetails = new RejectionDetails();
        rejectionDetails.setRejectionReason("REJECTION_REASON_001");
        rejectionDetails.setRemarks(null);
        leadLender.setRejectionDetails(rejectionDetails);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier))
                .thenReturn(leadLender);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(lenderReadService.getByKey(lenderKey)).thenReturn(lenderResponseData);
        when(codeValueMasterService.getByKey("REJECTION_REASON_001")).thenReturn(rejectionReasonResponse);

        // When
        LeadLenderResponse result = leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier);

        // Then
        assertNotNull(result);
        assertNotNull(result.getRejectionReason());
        assertNull(result.getRejectionRemarks());
        
        verify(codeValueMasterService).getByKey("REJECTION_REASON_001");
    }

    // ==================== Helper Methods ====================

    private LeadLender createLeadLender(Long id, UUID lenderIdentifier, LeadLenderStatus status) {
        LeadLender leadLender = new LeadLender();
        leadLender.setId(id);
        leadLender.setLenderIdentifier(lenderIdentifier);
        leadLender.setLeadId(leadId);
        leadLender.setLenderKey(lenderKey);
        leadLender.setStatus(status);
        return leadLender;
    }
}

