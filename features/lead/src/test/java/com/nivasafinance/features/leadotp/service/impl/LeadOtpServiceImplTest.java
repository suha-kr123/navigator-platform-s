package com.nivasafinance.features.leadotp.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpRequest;
import com.nivasafinance.features.leadotp.entity.LeadOneTimeToken;
import com.nivasafinance.features.leadotp.repository.LeadOneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpSendResult;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.dto.OtpVerifyResult;
import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.otp.core.repository.OneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpCoreService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadOtpServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;
    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;
    @Mock
    private PersonReadService personReadService;
    @Mock
    private OtpCoreService otpCoreService;
    @Mock
    private LeadOneTimeTokenRepositoryWrapper leadOneTimeTokenRepositoryWrapper;
    @Mock
    private LeadReadService leadReadService;
    @Mock
    private LeadWriteService leadWriteService;
    @Mock
    private OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper;

    private LeadOtpServiceImpl leadOtpService;
    private UUID leadIdentifier;
    private UUID contactIdentifier;
    private Lead lead;
    private Contact contact;

    @BeforeEach
    void setUp() {
        leadOtpService = new LeadOtpServiceImpl(
                leadRepositoryWrapper,
                contactRepositoryWrapper,
                personReadService,
                otpCoreService,
                leadOneTimeTokenRepositoryWrapper,
                leadReadService,
                leadWriteService,
                oneTimeTokenRepositoryWrapper);

        leadIdentifier = UUID.randomUUID();
        contactIdentifier = UUID.randomUUID();

        lead = new Lead();
        lead.setId(10L);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setContacts(List.of(22L));

        contact = new Contact();
        contact.setId(22L);
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(33L);
    }

    @Test
    void sendVerifyLeadForCbOtp_resolvesLeadContactAndNormalizesMobile() {
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(22L)).thenReturn(contact);
        when(personReadService.getPersonById(33L)).thenReturn(PersonResponse.builder()
                .id(33L)
                .mobileNumbers(List.of(new MobileNumberDetails("+91 99882-37733", true, true)))
                .build());
        when(otpCoreService.sendOtp(any())).thenReturn(OtpSendResult.builder()
                .oneTimeTokenId(100L)
                .reference("VERIFY_LEAD_FOR_CB")
                .validityInMins(10)
                .build());

        var response = leadOtpService.sendVerifyLeadForCbOtp(leadIdentifier, contactIdentifier);

        assertEquals(100L, response.getOneTimeTokenId());
        assertEquals("VERIFY_LEAD_FOR_CB", response.getReference());

        ArgumentCaptor<OtpSendCommand> captor = ArgumentCaptor.forClass(OtpSendCommand.class);
        verify(otpCoreService).sendOtp(captor.capture());
        assertEquals("VERIFY_LEAD_FOR_CB", captor.getValue().getReference());
        assertEquals("9988237733", captor.getValue().getRecipient());
        verify(leadOneTimeTokenRepositoryWrapper).invalidateActiveTokens("VERIFY_LEAD_FOR_CB", 10L, 22L);
        verify(leadOneTimeTokenRepositoryWrapper).saveWithException(any(LeadOneTimeToken.class));
    }

    @Test
    void sendVerifyLeadForCbOtp_rejectsWhenContactDoesNotBelongToLead() {
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(22L)).thenReturn(contact);

        assertThrows(BadRequestException.class, () -> leadOtpService.sendVerifyLeadForCbOtp(leadIdentifier, UUID.randomUUID()));
    }

    @Test
    void verifyLeadForCbOtp_returnsVerifiedResponse() {
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(22L)).thenReturn(contact);
        when(leadOneTimeTokenRepositoryWrapper.findByLeadContactAndTokenId("VERIFY_LEAD_FOR_CB", 10L, 22L, 100L))
                .thenReturn(Optional.of(LeadOneTimeToken.builder()
                        .id(77L)
                        .leadId(10L)
                        .contactId(22L)
                        .ottId(100L)
                        .reference("VERIFY_LEAD_FOR_CB")
                        .status(OtpStatus.SENT)
                        .build()));
        when(otpCoreService.verifyOtp(any())).thenReturn(OtpVerifyResult.builder().verified(true).build());

        var response = leadOtpService.verifyLeadForCbOtp(leadIdentifier, contactIdentifier, new LeadContactVerifyOtpRequest(100L, "2244"));

        assertTrue(response.isVerified());
        ArgumentCaptor<OtpVerifyCommand> captor = ArgumentCaptor.forClass(OtpVerifyCommand.class);
        verify(otpCoreService).verifyOtp(captor.capture());
        assertEquals(100L, captor.getValue().getOneTimeTokenId());
        assertEquals("2244", captor.getValue().getOtp());
        assertEquals("VERIFY_LEAD_FOR_CB", captor.getValue().getReference());
        verify(leadOneTimeTokenRepositoryWrapper).updateStatus(77L, OtpStatus.VERIFIED);
    }

    @Test
    void sendCreateLeadOtp_normalizesMobileAndStoresTrackingWithoutLead() {
        when(otpCoreService.sendOtp(any())).thenReturn(OtpSendResult.builder()
                .oneTimeTokenId(200L)
                .reference("CREATE_LEAD")
                .validityInMins(5)
                .build());

        var response = leadOtpService.sendCreateLeadOtp(new CreateLeadSendOtpRequest("+91 99882-37733"));

        assertEquals(200L, response.getOneTimeTokenId());
        assertEquals("CREATE_LEAD", response.getReference());
        ArgumentCaptor<OtpSendCommand> captor = ArgumentCaptor.forClass(OtpSendCommand.class);
        verify(otpCoreService).sendOtp(captor.capture());
        assertEquals("CREATE_LEAD", captor.getValue().getReference());
        assertEquals("9988237733", captor.getValue().getRecipient());
        verify(leadOneTimeTokenRepositoryWrapper).saveWithException(any(LeadOneTimeToken.class));
    }

    @Test
    void verifyCreateLeadOtp_returnsExistingReusableLead() {
        when(leadOneTimeTokenRepositoryWrapper.findByReferenceAndTokenId("CREATE_LEAD", 200L))
                .thenReturn(Optional.of(LeadOneTimeToken.builder()
                        .id(88L)
                        .ottId(200L)
                        .reference("CREATE_LEAD")
                        .status(OtpStatus.SENT)
                        .build()));
        when(oneTimeTokenRepositoryWrapper.findById(200L))
                .thenReturn(Optional.of(OneTimeToken.builder().id(200L).otp("2244").relatesTo("9988237733").build()));
        when(otpCoreService.verifyOtp(any())).thenReturn(OtpVerifyResult.builder().verified(true).build());
        when(leadReadService.findLeadByPhoneNumber("9988237733"))
                .thenReturn(Optional.of(LeadBasicResponse.builder().id(10L).leadIdentifier(leadIdentifier).build()));
        when(leadRepositoryWrapper.findByIdWithException(10L)).thenReturn(lead);

        var response = leadOtpService.verifyCreateLeadOtp(new CreateLeadVerifyOtpRequest(200L, "2244"));

        assertTrue(response.isVerified());
        assertEquals(leadIdentifier, response.getLeadIdentifier());
        verify(leadOneTimeTokenRepositoryWrapper).updateLeadId(88L, 10L);
        verify(leadOneTimeTokenRepositoryWrapper).updateStatus(88L, OtpStatus.VERIFIED);
    }

    @Test
    void verifyCreateLeadOtp_createsLeadWhenNoReusableLeadExists() {
        UUID createdLeadIdentifier = UUID.randomUUID();
        Lead createdLead = new Lead();
        createdLead.setId(50L);
        createdLead.setLeadIdentifier(createdLeadIdentifier);

        when(leadOneTimeTokenRepositoryWrapper.findByReferenceAndTokenId("CREATE_LEAD", 200L))
                .thenReturn(Optional.of(LeadOneTimeToken.builder()
                        .id(88L)
                        .ottId(200L)
                        .reference("CREATE_LEAD")
                        .status(OtpStatus.SENT)
                        .build()));
        when(oneTimeTokenRepositoryWrapper.findById(200L))
                .thenReturn(Optional.of(OneTimeToken.builder().id(200L).otp("2244").relatesTo("9988237733").build()));
        when(otpCoreService.verifyOtp(any())).thenReturn(OtpVerifyResult.builder().verified(true).build());
        when(leadReadService.findLeadByPhoneNumber("9988237733")).thenReturn(Optional.empty());
        when(leadWriteService.createLead(any())).thenReturn(CreateLeadResponse.builder().leadIdentifier(createdLeadIdentifier).build());
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(createdLeadIdentifier)).thenReturn(createdLead);

        var response = leadOtpService.verifyCreateLeadOtp(new CreateLeadVerifyOtpRequest(200L, "2244"));

        assertTrue(response.isVerified());
        assertEquals(createdLeadIdentifier, response.getLeadIdentifier());
        verify(leadOneTimeTokenRepositoryWrapper).updateLeadId(88L, 50L);
        verify(leadOneTimeTokenRepositoryWrapper).updateStatus(88L, OtpStatus.VERIFIED);
    }
}
