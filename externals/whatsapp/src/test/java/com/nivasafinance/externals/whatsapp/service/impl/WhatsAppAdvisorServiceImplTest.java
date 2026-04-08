package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppAdvisorRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppAdvisorResponse;
import com.nivasafinance.features.advisor.dto.CreateAdvisorRequest;
import com.nivasafinance.features.advisor.dto.UpdateSegmentationDetailsRequest;
import com.nivasafinance.features.advisor.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppAdvisorServiceImplTest {

    @Mock
    private UserReadService userReadService;

    @Mock
    private AdvisorReadService advisorReadService;

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private AdvisorWriteService advisorWriteService;

    @InjectMocks
    private WhatsAppAdvisorServiceImpl service;

    // ── createOrGetAdvisor: existing advisor ──

    @Test
    void createOrGetAdvisor_whenAdvisorExists_returnsExistingAdvisorDetails() {
        UUID advisorIdentifier = UUID.randomUUID();
        Advisor advisor = buildAdvisor(advisorIdentifier, "testuser", "REF123");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        UserResponse userResponse = UserResponse.builder().username("testuser").build();
        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.of(userResponse));
        when(advisorReadService.findAdvisorByUsername("testuser")).thenReturn(Optional.of(advisor));

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("John Doe");
        when(userReadService.getPersonForUser("testuser")).thenReturn(personResponse);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertEquals(advisorIdentifier, result.getAdvisorIdentifier(),
                "Should return existing advisor's identifier");
        assertEquals("John Doe", result.getName(),
                "Should return display name from person");
        assertEquals("REF123", result.getReferralCode(),
                "Should return existing advisor's referral code");
        verifyNoInteractions(advisorWriteService);
    }

    @Test
    void createOrGetAdvisor_whenExistingAdvisorHasNullReferralCode_returnsEmpty() {
        UUID advisorIdentifier = UUID.randomUUID();
        Advisor advisor = buildAdvisor(advisorIdentifier, "testuser", null);

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        UserResponse userResponse = UserResponse.builder().username("testuser").build();
        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.of(userResponse));
        when(advisorReadService.findAdvisorByUsername("testuser")).thenReturn(Optional.of(advisor));

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("John");
        when(userReadService.getPersonForUser("testuser")).thenReturn(personResponse);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertEquals("empty", result.getReferralCode(),
                "Null referral code should be returned as 'empty'");
    }

    @Test
    void createOrGetAdvisor_whenExistingAdvisorHasBlankReferralCode_returnsEmpty() {
        UUID advisorIdentifier = UUID.randomUUID();
        Advisor advisor = buildAdvisor(advisorIdentifier, "testuser", "   ");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        UserResponse userResponse = UserResponse.builder().username("testuser").build();
        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.of(userResponse));
        when(advisorReadService.findAdvisorByUsername("testuser")).thenReturn(Optional.of(advisor));

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("John");
        when(userReadService.getPersonForUser("testuser")).thenReturn(personResponse);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertEquals("empty", result.getReferralCode(),
                "Blank referral code should be returned as 'empty'");
    }

    @Test
    void createOrGetAdvisor_whenExistingAdvisorHasNullStatus_returnsNullStatus() {
        UUID advisorIdentifier = UUID.randomUUID();
        Advisor advisor = buildAdvisor(advisorIdentifier, "testuser", "REF123");
        advisor.setStatus(null);

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        UserResponse userResponse = UserResponse.builder().username("testuser").build();
        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.of(userResponse));
        when(advisorReadService.findAdvisorByUsername("testuser")).thenReturn(Optional.of(advisor));

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("John");
        when(userReadService.getPersonForUser("testuser")).thenReturn(personResponse);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertNull(result.getStatus(),
                "Null advisor status should produce null status in response");
    }

    @Test
    void createOrGetAdvisor_whenPersonIsNull_returnsEmptyName() {
        UUID advisorIdentifier = UUID.randomUUID();
        Advisor advisor = buildAdvisor(advisorIdentifier, "testuser", "REF123");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        UserResponse userResponse = UserResponse.builder().username("testuser").build();
        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.of(userResponse));
        when(advisorReadService.findAdvisorByUsername("testuser")).thenReturn(Optional.of(advisor));
        when(userReadService.getPersonForUser("testuser")).thenReturn(null);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertEquals("empty", result.getName(),
                "Null person should produce 'empty' name");
    }

    @Test
    void createOrGetAdvisor_whenPersonDisplayNameIsBlank_returnsEmptyName() {
        UUID advisorIdentifier = UUID.randomUUID();
        Advisor advisor = buildAdvisor(advisorIdentifier, "testuser", "REF123");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        UserResponse userResponse = UserResponse.builder().username("testuser").build();
        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.of(userResponse));
        when(advisorReadService.findAdvisorByUsername("testuser")).thenReturn(Optional.of(advisor));

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("   ");
        when(userReadService.getPersonForUser("testuser")).thenReturn(personResponse);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertEquals("empty", result.getName(),
                "Blank display name should produce 'empty' name");
    }

    // ── createOrGetAdvisor: new advisor ──

    @Test
    void createOrGetAdvisor_whenAdvisorDoesNotExist_createsNewAdvisor() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        WhatsAppAdvisorResponse result = service.createOrGetAdvisor(request);

        assertEquals(advisorIdentifier, result.getAdvisorIdentifier(),
                "Should return newly created advisor's identifier");
        assertEquals("New User", result.getName(),
                "Should return the new advisor's display name");
        verify(advisorWriteService).createAdvisor(any(CreateAdvisorRequest.class));
        verify(advisorWriteService).updateSegmentationDetails(eq(advisorIdentifier), any(UpdateSegmentationDetailsRequest.class));
    }

    @Test
    void createOrGetAdvisor_whenNewAdvisorWithSourcingChannel_updatesSourcingDetails() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        service.createOrGetAdvisor(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(advisorWriteService).updateSourcingDetails(eq(advisorIdentifier), captor.capture());
        assertEquals("DIRECT_WHATSAPP_SOURCE", captor.getValue().getSourcingChannel(),
                "Sourcing channel should always be set to DIRECT_WHATSAPP_SOURCE");
    }

    @Test
    void createOrGetAdvisor_whenNewAdvisorWithoutSourcingChannel_skipsSourcingUpdate() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        service.createOrGetAdvisor(request);

        verify(advisorWriteService, never()).updateSourcingDetails(any(UUID.class), any(UpdateSourcingDetailsRequest.class));
    }

    // ── createOrGetAdvisor: sourcing details with marketing ──

    @Test
    void createOrGetAdvisor_whenNewAdvisorWithMarketingSourceIdAndUrl_setsBothFields() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest.MarketingDetails marketing = new WhatsAppAdvisorRequest.MarketingDetails();
        marketing.setSourceId("SRC-001");
        marketing.setSourceUrl("https://example.com");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");
        request.setMarketing_details(marketing);

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        service.createOrGetAdvisor(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(advisorWriteService).updateSourcingDetails(eq(advisorIdentifier), captor.capture());
        assertEquals("SRC-001", captor.getValue().getSourceId(),
                "Source ID should be set from marketing details");
        assertEquals("https://example.com", captor.getValue().getSourceUrl(),
                "Source URL should be set from marketing details");
    }

    @Test
    void createOrGetAdvisor_whenNewAdvisorWithOnlySourceId_setsBothFields() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest.MarketingDetails marketing = new WhatsAppAdvisorRequest.MarketingDetails();
        marketing.setSourceId("SRC-001");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");
        request.setMarketing_details(marketing);

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        service.createOrGetAdvisor(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(advisorWriteService).updateSourcingDetails(eq(advisorIdentifier), captor.capture());
        assertEquals("SRC-001", captor.getValue().getSourceId(),
                "Source ID should be set even when source URL is null");
        assertNull(captor.getValue().getSourceUrl(),
                "Source URL should be null when not provided");
    }

    @Test
    void createOrGetAdvisor_whenNewAdvisorWithCfMarketingSource_setsMarketingSource() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest.MarketingDetails marketing = new WhatsAppAdvisorRequest.MarketingDetails();
        marketing.setCf_marketing_source("FACEBOOK_AD");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");
        request.setMarketing_details(marketing);

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        service.createOrGetAdvisor(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(advisorWriteService).updateSourcingDetails(eq(advisorIdentifier), captor.capture());
        assertEquals("FACEBOOK_AD", captor.getValue().getMarketingSource(),
                "Marketing source should be set from cf_marketing_source");
    }

    @Test
    void createOrGetAdvisor_whenNewAdvisorWithBlankCfMarketingSource_skipsMarketingSource() {
        UUID advisorIdentifier = UUID.randomUUID();

        WhatsAppAdvisorRequest.MarketingDetails marketing = new WhatsAppAdvisorRequest.MarketingDetails();
        marketing.setCf_marketing_source("   ");

        WhatsAppAdvisorRequest request = new WhatsAppAdvisorRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");
        request.setMarketing_details(marketing);

        when(userReadService.findUserByPersonMobile("9876543210")).thenReturn(Optional.empty());
        when(advisorWriteService.createAdvisor(any(CreateAdvisorRequest.class))).thenReturn(advisorIdentifier);

        Advisor createdAdvisor = buildAdvisor(advisorIdentifier, "newuser", "REF456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(createdAdvisor);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setDisplayName("New User");
        when(userReadService.getPersonForUser("newuser")).thenReturn(personResponse);

        service.createOrGetAdvisor(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(advisorWriteService).updateSourcingDetails(eq(advisorIdentifier), captor.capture());
        assertNull(captor.getValue().getMarketingSource(),
                "Blank cf_marketing_source should not set marketing source");
    }

    // ── helper ──

    private Advisor buildAdvisor(UUID identifier, String username, String referralCode) {
        Advisor advisor = new Advisor();
        advisor.setIdentifier(identifier);
        advisor.setUsername(username);
        advisor.setReferralCode(referralCode);
        return advisor;
    }
}
