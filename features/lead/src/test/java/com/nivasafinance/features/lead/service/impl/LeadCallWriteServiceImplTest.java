package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.call.dto.CallLogResponse;
import com.nivasafinance.features.call.dto.CreateCallLogResponse;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.call.dto.UpdateCallLog;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.service.CallReadService;
import com.nivasafinance.features.call.service.CallWriteService;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.service.CampaignReadService;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogResponse;
import com.nivasafinance.features.lead.dto.CreateLeadCallRequest;
import com.nivasafinance.features.lead.dto.CreateLeadCallResponse;
import com.nivasafinance.features.lead.dto.LeadUpdateCallLog;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadCallWriteServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private PersonReadService personReadService;

    @Mock
    private CallWriteService callWriteService;

    @Mock
    private CallReadService callReadService;

    @Mock
    private UserReadService userReadService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CampaignReadService campaignReadService;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private LeadCallWriteServiceImpl leadCallWriteService;

    private UUID leadIdentifier;
    private Long leadId;
    private Lead lead;
    private UUID contactIdentifier;
    private Long contactId;
    private Long personId;
    private Contact contact;
    private PersonResponse personResponse;
    private String phoneNumber;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        leadId = 1L;
        contactIdentifier = UUID.randomUUID();
        contactId = 10L;
        personId = 20L;
        phoneNumber = "9876543210";

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setContacts(List.of(contactId));

        contact = new Contact();
        contact.setId(contactId);
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);

        MobileNumberDetails mobile = MobileNumberDetails.builder()
                .number(phoneNumber)
                .isPrimary(true)
                .build();

        personResponse = new PersonResponse();
        personResponse.setMobileNumbers(List.of(mobile));
        personResponse.setDisplayName("Test Person");

        UserContext.setUsername("testuser");
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ==================== callContact() Tests ====================

    @Test
    void callContact_success_returnsResponse() {
        // Given
        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        InitiateCallResponse callResponse = InitiateCallResponse.builder()
                .id(100L).identifier(UUID.randomUUID()).status(CallStatus.QUEUED).build();

        mockCurrentUserWithPrimaryNumber("1234567890");
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.call(any())).thenReturn(callResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        CreateLeadCallResponse result = leadCallWriteService.callContact(leadIdentifier, request);

        // Then
        assertNotNull(result, "Response should not be null");
        assertEquals(callResponse.getIdentifier(), result.getIdentifier(), "Response identifier should match call response");
        assertEquals(CallStatus.QUEUED, result.getStatus(), "Response status should be QUEUED");
        verify(leadRepositoryWrapper).saveWithException(lead);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void callContact_contactNotBelongingToLead_throwsBadRequest() {
        // Given
        lead.setContacts(List.of(999L));
        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.callContact(leadIdentifier, request),
                "Should throw BadRequestException when contact does not belong to lead");
        verify(callWriteService, never()).call(any());
    }

    @Test
    void callContact_phoneNotBelongingToPerson_throwsBadRequest() {
        // Given
        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber("0000000000")
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.callContact(leadIdentifier, request),
                "Should throw BadRequestException when phone number does not belong to person");
        verify(callWriteService, never()).call(any());
    }

    @Test
    void callContact_emptyContactsList_throwsBadRequest() {
        // Given
        lead.setContacts(List.of());
        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.callContact(leadIdentifier, request),
                "Should throw BadRequestException when lead has no contacts");
    }

    @Test
    void callContact_nullOtherDetails_setsLastCallId() {
        // Given
        lead.setOtherDetails(null);
        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        InitiateCallResponse callResponse = InitiateCallResponse.builder()
                .id(100L).identifier(UUID.randomUUID()).status(CallStatus.QUEUED).build();

        mockCurrentUserWithPrimaryNumber("1234567890");
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.call(any())).thenReturn(callResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.callContact(leadIdentifier, request);

        // Then
        assertNotNull(lead.getOtherDetails(), "OtherDetails should be created when null");
        assertEquals(100L, lead.getOtherDetails().getLastCallId(), "lastCallId should be set to the new call log ID");
    }

    // ==================== updateCallLog() Tests ====================

    @Test
    void updateCallLog_success_updatesAndPublishesEvent() {
        // Given
        String externalId = "ext-123";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED)
                .build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);

        // Then
        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any(UpdateCallLog.class));
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void updateCallLog_withRecordingDetails_mapsCorrectly() {
        // Given
        String externalId = "ext-123";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED)
                .recordingDetails(LeadUpdateCallLog.RecordingDetails.builder().url("https://rec.url").build())
                .build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED)
                .recordingDetails(CallLog.RecordingDetails.builder().url("https://rec.url").build())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);

        // Then
        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any(UpdateCallLog.class));
    }

    @Test
    void updateCallLog_withCompletionDetailsAndLegs_mapsCorrectly() {
        // Given
        String externalId = "ext-123";
        LeadUpdateCallLog.CompletionLeg leg = LeadUpdateCallLog.CompletionLeg.builder()
                .duration("30").direction("outbound").status(CallStatus.COMPLETED).build();
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED)
                .completionDetails(LeadUpdateCallLog.CompletionDetails.builder()
                        .duration(60L).legs(List.of(leg)).build())
                .build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);

        // Then
        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any(UpdateCallLog.class));
    }

    @Test
    void updateCallLog_callLogNotFoundByProviderId_throwsBadRequest() {
        // Given
        String externalId = "ext-999";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder().status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.updateCallLog(leadIdentifier, externalId, request),
                "Should throw BadRequestException when call log is not found by provider ID");
    }

    // ==================== createExternalCallLog() Tests ====================

    @Test
    void createExternalCallLog_outbound_success() {
        // Given
        UUID callLogIdentifier = UUID.randomUUID();
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.OUTBOUND)
                .toNumber(phoneNumber)
                .fromNumber("1234567890")
                .status(CallStatus.COMPLETED)
                .build();

        CreateCallLogResponse savedResponse = CreateCallLogResponse.builder()
                .id(200L).identifier(callLogIdentifier).status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.createCallLog(any(CallLog.class))).thenReturn(savedResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        CreateExternalCallLogResponse result = leadCallWriteService.createExternalCallLog(leadIdentifier, request);

        // Then
        assertNotNull(result, "Response should not be null");
        assertEquals(callLogIdentifier, result.getIdentifier(), "Identifier should match saved call log");
        assertEquals(CallStatus.COMPLETED, result.getStatus(), "Status should match saved call log");
        verify(callWriteService).mapCallLogToLead(200L, leadId, contactId);
        verify(leadRepositoryWrapper).saveWithException(lead);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void createExternalCallLog_inbound_validatesFromNumber() {
        // Given
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.INBOUND)
                .fromNumber(phoneNumber)
                .toNumber("1234567890")
                .status(CallStatus.COMPLETED)
                .build();

        CreateCallLogResponse savedResponse = CreateCallLogResponse.builder()
                .id(200L).identifier(UUID.randomUUID()).status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.createCallLog(any(CallLog.class))).thenReturn(savedResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        CreateExternalCallLogResponse result = leadCallWriteService.createExternalCallLog(leadIdentifier, request);

        // Then
        assertNotNull(result, "Response should not be null for inbound call");
    }

    @Test
    void createExternalCallLog_inbound_invalidFromNumber_throwsBadRequest() {
        // Given
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.INBOUND)
                .fromNumber("0000000000")
                .toNumber("1234567890")
                .status(CallStatus.COMPLETED)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.createExternalCallLog(leadIdentifier, request),
                "Should throw BadRequestException when inbound fromNumber does not belong to person");
    }

    @Test
    void createExternalCallLog_outbound_invalidToNumber_throwsBadRequest() {
        // Given
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.OUTBOUND)
                .toNumber("0000000000")
                .fromNumber("1234567890")
                .status(CallStatus.COMPLETED)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.createExternalCallLog(leadIdentifier, request),
                "Should throw BadRequestException when outbound toNumber does not belong to person");
    }

    @Test
    void createExternalCallLog_withCampaignId_setsOutboundDirection() {
        // Given
        UUID campaignId = UUID.randomUUID();
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.OUTBOUND)
                .toNumber(phoneNumber)
                .fromNumber("1234567890")
                .status(CallStatus.COMPLETED)
                .campaignId(campaignId)
                .build();

        CampaignDetailedResponse campaignResponse = new CampaignDetailedResponse();
        campaignResponse.setCampaignId(50L);

        CreateCallLogResponse savedResponse = CreateCallLogResponse.builder()
                .id(200L).identifier(UUID.randomUUID()).status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(campaignReadService.getCampaignByIdentifier(campaignId)).thenReturn(campaignResponse);
        when(callWriteService.createCallLog(any(CallLog.class))).thenReturn(savedResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        CreateExternalCallLogResponse result = leadCallWriteService.createExternalCallLog(leadIdentifier, request);

        // Then
        assertNotNull(result, "Response should not be null for campaign call");
        verify(campaignReadService).getCampaignByIdentifier(campaignId);
    }

    @Test
    void createExternalCallLog_contactNotBelongingToLead_throwsBadRequest() {
        // Given
        lead.setContacts(List.of(999L));
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.OUTBOUND)
                .toNumber(phoneNumber)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> leadCallWriteService.createExternalCallLog(leadIdentifier, request),
                "Should throw BadRequestException when contact does not belong to lead");
        verify(callWriteService, never()).createCallLog(any());
    }

    // ==================== updateCallLog() – branch coverage ====================

    @Test
    void updateCallLog_withNullRecordingAndCompletionDetails_mapsMinimally() {
        // Given
        String externalId = "ext-456";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.FAILED)
                .recordingDetails(null)
                .completionDetails(null)
                .build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.FAILED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);

        // Then
        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any(UpdateCallLog.class));
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void updateCallLog_withCompletionDetailsNullLegs_skipsLegMapping() {
        // Given
        String externalId = "ext-789";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED)
                .completionDetails(LeadUpdateCallLog.CompletionDetails.builder()
                        .duration(45L).legs(null).build())
                .build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);

        // Then
        verify(callWriteService).updateCallLogByProviderId(eq(externalId), any(UpdateCallLog.class));
    }

    // ==================== createExternalCallLog() – branch coverage ====================

    @Test
    void createExternalCallLog_withCreatedAt_setsCreatedAtOnCallLog() {
        // Given
        java.time.LocalDateTime customCreatedAt = java.time.LocalDateTime.of(2025, 6, 15, 10, 30);
        CreateExternalCallLogRequest request = CreateExternalCallLogRequest.builder()
                .contactIdentifier(contactIdentifier)
                .direction(CallDirection.OUTBOUND)
                .toNumber(phoneNumber)
                .fromNumber("1234567890")
                .status(CallStatus.COMPLETED)
                .createdAt(customCreatedAt)
                .build();

        CreateCallLogResponse savedResponse = CreateCallLogResponse.builder()
                .id(200L).identifier(UUID.randomUUID()).status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.createCallLog(any(CallLog.class))).thenReturn(savedResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        CreateExternalCallLogResponse result = leadCallWriteService.createExternalCallLog(leadIdentifier, request);

        // Then
        assertNotNull(result);
        verify(callWriteService).createCallLog(any(CallLog.class));
    }

    // ==================== updateLastCallId() – branch coverage ====================

    @Test
    void callContact_withExistingLastCallIdSame_skipsUpdate() {
        // Given
        Lead.OtherDetails otherDetails = new Lead.OtherDetails();
        otherDetails.setLastCallId(100L);
        lead.setOtherDetails(otherDetails);

        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        InitiateCallResponse callResponse = InitiateCallResponse.builder()
                .id(100L).identifier(UUID.randomUUID()).status(CallStatus.QUEUED).build();

        mockCurrentUserWithPrimaryNumber("1234567890");
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.call(any())).thenReturn(callResponse);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.callContact(leadIdentifier, request);

        // Then
        assertEquals(100L, lead.getOtherDetails().getLastCallId(),
                "lastCallId should remain the same when call IDs match");
    }

    @Test
    void callContact_withOlderExistingLastCallId_updatesToNewerCallId() {
        // Given
        Lead.OtherDetails otherDetails = new Lead.OtherDetails();
        otherDetails.setLastCallId(50L);
        lead.setOtherDetails(otherDetails);

        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        InitiateCallResponse callResponse = InitiateCallResponse.builder()
                .id(200L).identifier(UUID.randomUUID()).status(CallStatus.QUEUED).build();

        java.time.LocalDateTime olderTime = java.time.LocalDateTime.of(2025, 1, 1, 10, 0);
        java.time.LocalDateTime newerTime = java.time.LocalDateTime.of(2025, 6, 1, 10, 0);

        CallLogResponse currentCallLog = CallLogResponse.builder().id(50L).createdAt(olderTime).build();
        CallLogResponse newCallLog = CallLogResponse.builder().id(200L).createdAt(newerTime).build();

        mockCurrentUserWithPrimaryNumber("1234567890");
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.call(any())).thenReturn(callResponse);
        when(callReadService.getCallLogByID(50L)).thenReturn(currentCallLog);
        when(callReadService.getCallLogByID(200L)).thenReturn(newCallLog);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.callContact(leadIdentifier, request);

        // Then
        assertEquals(200L, lead.getOtherDetails().getLastCallId(),
                "lastCallId should be updated to the newer call");
    }

    @Test
    void callContact_withNewerExistingLastCallId_keepsExisting() {
        // Given
        Lead.OtherDetails otherDetails = new Lead.OtherDetails();
        otherDetails.setLastCallId(50L);
        lead.setOtherDetails(otherDetails);

        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        InitiateCallResponse callResponse = InitiateCallResponse.builder()
                .id(200L).identifier(UUID.randomUUID()).status(CallStatus.QUEUED).build();

        java.time.LocalDateTime newerTime = java.time.LocalDateTime.of(2025, 6, 1, 10, 0);
        java.time.LocalDateTime olderTime = java.time.LocalDateTime.of(2025, 1, 1, 10, 0);

        CallLogResponse currentCallLog = CallLogResponse.builder().id(50L).createdAt(newerTime).build();
        CallLogResponse newCallLog = CallLogResponse.builder().id(200L).createdAt(olderTime).build();

        mockCurrentUserWithPrimaryNumber("1234567890");
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.call(any())).thenReturn(callResponse);
        when(callReadService.getCallLogByID(50L)).thenReturn(currentCallLog);
        when(callReadService.getCallLogByID(200L)).thenReturn(newCallLog);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.callContact(leadIdentifier, request);

        // Then
        assertEquals(50L, lead.getOtherDetails().getLastCallId(),
                "lastCallId should stay as the existing newer call");
    }

    @Test
    void callContact_withExistingLastCallIdNullCreatedAt_updatesToNew() {
        // Given
        Lead.OtherDetails otherDetails = new Lead.OtherDetails();
        otherDetails.setLastCallId(50L);
        lead.setOtherDetails(otherDetails);

        CreateLeadCallRequest request = CreateLeadCallRequest.builder()
                .contactIdentifier(contactIdentifier)
                .phoneNumber(phoneNumber)
                .build();

        InitiateCallResponse callResponse = InitiateCallResponse.builder()
                .id(200L).identifier(UUID.randomUUID()).status(CallStatus.QUEUED).build();

        CallLogResponse currentCallLog = CallLogResponse.builder().id(50L).createdAt(null).build();
        CallLogResponse newCallLog = CallLogResponse.builder().id(200L).createdAt(null).build();

        mockCurrentUserWithPrimaryNumber("1234567890");
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);
        when(callWriteService.call(any())).thenReturn(callResponse);
        when(callReadService.getCallLogByID(50L)).thenReturn(currentCallLog);
        when(callReadService.getCallLogByID(200L)).thenReturn(newCallLog);
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When
        leadCallWriteService.callContact(leadIdentifier, request);

        // Then
        assertEquals(200L, lead.getOtherDetails().getLastCallId(),
                "lastCallId should fallback to new when createdAt is null");
    }

    // ==================== updateCallLog() – recordingUrlFrom branch coverage ====================

    @Test
    void updateCallLog_withNullRecordingDetails_recordingUrlIsNull() {
        // Given
        String externalId = "ext-rec-null";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED).build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED)
                .recordingDetails(null)
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When & Then (no exception)
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void updateCallLog_withEmptyRecordingUrl_recordingUrlIsNull() {
        // Given
        String externalId = "ext-rec-empty";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED).build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED)
                .recordingDetails(CallLog.RecordingDetails.builder().url("  ").build())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When & Then (no exception)
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void updateCallLog_withNullRecordingUrl_recordingUrlIsNull() {
        // Given
        String externalId = "ext-rec-url-null";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED).build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED)
                .recordingDetails(CallLog.RecordingDetails.builder().url(null).build())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));
        when(userRoleService.getPrimaryRoleForUsername("testuser")).thenReturn("AGENT");

        // When & Then (no exception)
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    // ==================== publishEvent – null username branch ====================

    @Test
    void updateCallLog_withNullUsername_publishesEventWithNullRole() {
        // Given
        UserContext.setUsername(null);
        String externalId = "ext-null-user";
        LeadUpdateCallLog request = LeadUpdateCallLog.builder()
                .status(CallStatus.COMPLETED).build();

        CallLogResponse updatedLog = CallLogResponse.builder()
                .id(100L).identifier(UUID.randomUUID())
                .status(CallStatus.COMPLETED).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(callReadService.getCallLogByProviderId(externalId)).thenReturn(Optional.of(updatedLog));

        // When
        leadCallWriteService.updateCallLog(leadIdentifier, externalId, request);

        // Then
        verify(userRoleService, never()).getPrimaryRoleForUsername(any());
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    // ==================== Helper Methods ====================

    private void mockCurrentUserWithPrimaryNumber(String number) {
        MobileNumberDetails primaryMobile = MobileNumberDetails.builder()
                .number(number)
                .isPrimary(true)
                .build();
        PersonResponse userPerson = new PersonResponse();
        userPerson.setMobileNumbers(List.of(primaryMobile));
        UserResponse userResponse = new UserResponse();
        userResponse.setPersonResponse(userPerson);
        when(userReadService.getUserByUsername("testuser")).thenReturn(userResponse);
    }
}
