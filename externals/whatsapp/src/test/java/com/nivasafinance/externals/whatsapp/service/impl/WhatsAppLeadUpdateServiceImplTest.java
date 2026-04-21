package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadUpdateRequest;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadService;
import com.nivasafinance.features.lead.dto.DropoffLeadRequest;
import com.nivasafinance.features.lead.dto.OnholdLeadRequest;
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhatsAppLeadUpdateServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private LeadWriteService leadWriteService;

    @Mock
    private PersonReadService personReadService;

    @Mock
    private PersonWriteService personWriteService;

    @Mock
    private WhatsAppLeadService whatsAppLeadService;

    @InjectMocks
    private WhatsAppLeadUpdateServiceImpl service;

    @Test
    void updateLead_whenNoActionFields_throwsBadRequestException() {
        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(UUID.randomUUID());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.updateLead(request),
                "Request with no update fields should be rejected");
        assertTrue(ex.getMessage().contains("At least one of"),
                "Error should describe required update fields");
        verifyNoInteractions(leadWriteService);
    }

    @Test
    void updateLead_whenMissingLeadIdentifierAndMobile_throwsBadRequestException() {
        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setStatusAction("resume");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.updateLead(request),
                "Resume without lead identity should fail");
        assertEquals("leadIdentifier or mobileNumber is required", ex.getMessage(),
                "Should require lead identifier or mobile for resolution");
        verifyNoInteractions(leadWriteService);
    }

    @Test
    void updateLead_whenLeadIdentifierAndResume_callsResumeLead() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setStatusAction("resume");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        verify(leadWriteService).resumeLead(leadId);
        verify(leadWriteService, never()).onholdLead(any(), any());
    }

    @Test
    void updateLead_whenMobileResolves_andResume_callsResumeWithResolvedLead() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setMobileNumber("9876543210");
        request.setStatusAction("RESUME");

        when(whatsAppLeadService.resolveLeadIdentifierByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(leadId));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        verify(leadWriteService).resumeLead(leadId);
    }

    @Test
    void updateLead_whenHoldFollowUpInDays_callsOnholdWithTodayPlusDays() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);
        LocalDate expectedDate = LocalDate.now().plusDays(7);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setHoldFollowUpInDays(7);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        ArgumentCaptor<OnholdLeadRequest> captor = ArgumentCaptor.forClass(OnholdLeadRequest.class);
        verify(leadWriteService).onholdLead(eq(leadId), captor.capture());
        assertEquals(expectedDate, captor.getValue().getHoldFollowUpDate(),
                "On-hold follow-up should be current date plus holdFollowUpInDays");
        assertNull(captor.getValue().getReasonCode(),
                "Omitting onHoldReasonCode should pass null reason to lead service");
    }

    @Test
    void updateLead_whenOnHoldReasonBlank_passesNullReasonCode() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setHoldFollowUpInDays(3);
        request.setOnHoldReasonCode("   ");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        ArgumentCaptor<OnholdLeadRequest> captor = ArgumentCaptor.forClass(OnholdLeadRequest.class);
        verify(leadWriteService).onholdLead(eq(leadId), captor.capture());
        assertNull(captor.getValue().getReasonCode(),
                "Blank onHoldReasonCode should be treated as absent");
    }

    @Test
    void updateLead_whenHoldFollowUpInDaysZero_withStatusAction_throwsBadRequestException() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setHoldFollowUpInDays(0);
        request.setStatusAction("resume");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.updateLead(request),
                "Non-positive holdFollowUpInDays should be rejected");
        assertEquals("holdFollowUpInDays must be a positive number", ex.getMessage(),
                "Should explain invalid day offset");
    }

    @Test
    void updateLead_whenHoldFollowUpInDaysTakesPrecedenceOverAbsoluteDate() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);
        LocalDate expectedDate = LocalDate.now().plusDays(2);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setHoldFollowUpInDays(2);
        request.setHoldFollowUpDate(LocalDate.of(2099, 1, 1));

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        ArgumentCaptor<OnholdLeadRequest> captor = ArgumentCaptor.forClass(OnholdLeadRequest.class);
        verify(leadWriteService).onholdLead(eq(leadId), captor.capture());
        assertEquals(expectedDate, captor.getValue().getHoldFollowUpDate(),
                "holdFollowUpInDays should override holdFollowUpDate when both are set");
    }

    @Test
    void updateLead_whenInvalidMobile_throwsBadRequestException() {
        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setMobileNumber("12345");
        request.setStatusAction("resume");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.updateLead(request),
                "Invalid mobile length should be rejected");
        assertEquals("mobileNumber must be exactly 10 digits", ex.getMessage(),
                "Should enforce 10-digit mobile for lookup");
    }

    @Test
    void updateLead_whenMobileResolvesEmpty_throwsBadRequestException() {
        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setMobileNumber("9876543210");
        request.setStatusAction("resume");

        when(whatsAppLeadService.resolveLeadIdentifierByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.updateLead(request),
                "Unknown mobile with no lead should fail");
        assertEquals("No lead found for mobileNumber", ex.getMessage(),
                "Should surface missing lead for mobile");
    }

    @Test
    void updateLead_whenDropoff_callsDropoffLead() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setStatusAction("dropoff");
        request.setReasonCode("NO_RESPONSE");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        ArgumentCaptor<DropoffLeadRequest> captor = ArgumentCaptor.forClass(DropoffLeadRequest.class);
        verify(leadWriteService).dropoffLead(eq(leadId), captor.capture());
        assertEquals("NO_RESPONSE", captor.getValue().getReasonCode(),
                "Dropoff reason should flow to lead write service");
    }

    @Test
    void updateLead_whenReject_callsRejectLead() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setStatusAction("reject");
        request.setReasonCode("LOW_INCOME");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        service.updateLead(request);

        ArgumentCaptor<RejectLeadRequest> captor = ArgumentCaptor.forClass(RejectLeadRequest.class);
        verify(leadWriteService).rejectLead(eq(leadId), captor.capture());
        assertEquals("LOW_INCOME", captor.getValue().getReasonCode(),
                "Reject reason should flow to lead write service");
    }

    @Test
    void updateLead_whenInvalidStatusAction_throwsBadRequestException() {
        UUID leadId = UUID.randomUUID();
        Lead lead = minimalLead(leadId, 10L);

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setStatusAction("unknown");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.updateLead(request),
                "Unsupported status action should be rejected");
        assertEquals("statusAction must be dropoff, resume, or reject", ex.getMessage(),
                "Should list allowed status actions");
    }

    @Test
    void updateLead_whenAlternativeMobile_appendsAndUpdatesPerson() {
        UUID leadId = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();
        long contactDbId = 10L;
        long personId = 99L;
        Lead lead = minimalLead(leadId, contactDbId);
        Contact contact = new Contact();
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);

        PersonResponse personResponse = PersonResponse.builder()
                .firstName("A")
                .lastName("B")
                .email("a@b.com")
                .mobileNumbers(List.of(MobileNumberDetails.builder()
                        .number("9999999999")
                        .isPrimary(true)
                        .isWhatsappAvailable(true)
                        .build()))
                .build();

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setAlternativeMobileNumber("8888888888");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactDbId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        service.updateLead(request);

        ArgumentCaptor<PersonUpdateRequest> captor = ArgumentCaptor.forClass(PersonUpdateRequest.class);
        verify(personWriteService).updatePerson(eq(personId), captor.capture());
        List<MobileNumberDetails> mobiles = captor.getValue().getMobileNumbers();
        assertEquals(2, mobiles.size(), "Alternative number should be appended to existing mobiles");
        assertTrue(mobiles.stream().anyMatch(m -> "8888888888".equals(m.getNumber())),
                "Merged list should contain the new alternative number");
    }

    @Test
    void updateLead_whenAlternativeMobileAlreadyPresent_doesNotCallPersonWrite() {
        UUID leadId = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();
        long contactDbId = 10L;
        long personId = 99L;
        Lead lead = minimalLead(leadId, contactDbId);
        Contact contact = new Contact();
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);

        PersonResponse personResponse = PersonResponse.builder()
                .mobileNumbers(List.of(MobileNumberDetails.builder()
                        .number("8888888888")
                        .isPrimary(false)
                        .isWhatsappAvailable(true)
                        .build()))
                .build();

        WhatsAppLeadUpdateRequest request = new WhatsAppLeadUpdateRequest();
        request.setLeadIdentifier(leadId);
        request.setAlternativeMobileNumber("8888888888");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId)).thenReturn(lead);
        when(contactRepositoryWrapper.findByIdWithException(contactDbId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(personReadService.getPersonById(personId)).thenReturn(personResponse);

        service.updateLead(request);

        verify(personWriteService, never()).updatePerson(any(), any());
    }

    private Lead minimalLead(UUID leadIdentifier, Long primaryContactDbId) {
        Lead lead = new Lead();
        lead.setLeadIdentifier(leadIdentifier);
        lead.setStatus(LeadStatus.ACTIVE);
        lead.setOtherDetails(Lead.OtherDetails.builder()
                .primaryContactId(primaryContactDbId)
                .build());
        lead.setContacts(List.of(primaryContactDbId));
        return lead;
    }
}
