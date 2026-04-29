package com.nivasafinance.externals.whatsapp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppLeadServiceImplTest {

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private LeadWriteService leadWriteService;

    @Mock
    private LeadReadService leadReadService;

    @Mock
    private LeadContactReadService leadContactReadService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WhatsAppLeadServiceImpl service;

    // ── createOrGetLead: existing lead with full details ──

    @Test
    void createOrGetLead_whenLeadExists_returnsExistingLeadWithStatusAndSubstatus() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setProductCode("HOME_LOAN");
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);

        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());

        PreliminaryDetailsResponse prelimResponse = PreliminaryDetailsResponse.builder()
                .monthlyFamilyIncome(BigDecimal.valueOf(50000))
                .build();
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(prelimResponse);

        LeadResponse leadResponse = LeadResponse.builder()
                .productCode("HOME_LOAN")
                .productName("Home Loan")
                .build();
        when(leadReadService.getLeadByIdentifier(leadIdentifier)).thenReturn(leadResponse);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane Doe");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals(leadIdentifier, result.getLeadIdentifier(),
                "Should return existing lead's identifier");
        assertEquals(contactIdentifier, result.getContactIdentifier(),
                "Should return primary contact identifier");
        assertEquals("ACTIVE", result.getStatus(),
                "Should return lead status as string");
        assertEquals("ONHOLD", result.getSubstatus(),
                "Should return lead substatus as string");
        assertEquals("Jane Doe", result.getName(),
                "Should return contact person display name");
        assertEquals("HOME_LOAN", result.getProductCode(),
                "Should return product code from lead read API");
        assertEquals("Home Loan", result.getProductName(),
                "Should return product display name from lead read API");
    }

    @Test
    void createOrGetLead_whenExistingLeadHasBlankProductCode_returnsEmptyProductWithoutLeadRead() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setProductCode("   ");
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getProductCode(),
                "Blank product code on lead should be exposed as 'empty' without calling lead read enrichment");
        assertEquals("empty", result.getProductName(),
                "Product name should be 'empty' when product code is blank");
        verify(leadReadService, never()).getLeadByIdentifier(any(UUID.class));
    }

    @Test
    void createOrGetLead_whenExistingLeadHasNullStatus_returnsEmptyStatus() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setStatus(null);
        lead.setSubstatus(null);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getStatus(),
                "Null status should be returned as 'empty'");
        assertEquals("empty", result.getSubstatus(),
                "Null substatus should be returned as 'empty'");
    }

    @Test
    void createOrGetLead_whenExistingLeadHasNullWorkflowDetails_returnsEmptyStage() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setWorkflowDetails(null);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getStage(),
                "Null workflow details should produce 'empty' stage");
    }

    @Test
    void createOrGetLead_whenExistingLeadHasNullReasons_returnsEmptyReasons() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setReasons(null);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getReasons(),
                "Null reasons should be returned as 'empty'");
    }

    @Test
    void createOrGetLead_whenExistingLeadHasReasonsWithData_returnsReasonsAsJson() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setReasons(Lead.ReasonDetails.builder().reject("Bad credit").build());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertNotEquals("empty", result.getReasons(),
                "Reasons with data should be serialized as JSON, not 'empty'");
        assertTrue(result.getReasons().contains("Bad credit"),
                "Serialized reasons should contain the reject reason");
    }

    @Test
    void createOrGetLead_whenExistingLeadHasNullPreliminaryDetails_returnsEmpty() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getPreliminaryDetails(),
                "Null preliminary details should be returned as 'empty'");
    }

    @Test
    void createOrGetLead_whenPreliminaryDetailsHasNoData_returnsEmpty() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());

        PreliminaryDetailsResponse emptyPrelim = PreliminaryDetailsResponse.builder().build();
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(emptyPrelim);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getPreliminaryDetails(),
                "Preliminary details with no data should be returned as 'empty'");
    }

    @Test
    void createOrGetLead_whenPreliminaryDetailsThrowsException_returnsEmpty() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenThrow(new RuntimeException("DB error"));

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getPreliminaryDetails(),
                "Preliminary details should default to 'empty' on exception");
    }

    @Test
    void createOrGetLead_whenContactHasNullDisplayName_returnsEmptyName() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName(null);
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals("empty", result.getName(),
                "Null display name should produce 'empty' name");
    }

    // ── createOrGetLead: existing lead via fallback (no active lead) ──

    @Test
    void createOrGetLead_whenNoActiveLead_fallsBackToAnyLead() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new EmptyResultDataAccessException(1))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals(leadIdentifier, result.getLeadIdentifier(),
                "Should fall back to any lead when no active lead found");
        verify(jdbcTemplate, times(2)).queryForObject(anyString(), eq(Long.class), eq(personId));
    }

    // ── createOrGetLead: primary contact resolution ──

    @Test
    void createOrGetLead_whenNoPrimaryContactIdInOtherDetails_usesFirstContact() {
        Long personId = 1L;
        Long leadId = 10L;
        Long contactId = 20L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = buildLeadWithFullDetails(leadIdentifier, contactId);
        lead.setOtherDetails(Lead.OtherDetails.builder().primaryContactId(null).build());
        lead.setContacts(List.of(contactId));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Contact contact = buildContact(contactIdentifier, personId);
        when(contactRepositoryWrapper.findByIdWithException(contactId)).thenReturn(contact);
        when(contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier)).thenReturn(contact);
        when(leadContactReadService.getAddresses(contactIdentifier)).thenReturn(List.of());
        when(leadReadService.getPreliminaryDetails(leadIdentifier)).thenReturn(null);

        Person contactPerson = new Person();
        contactPerson.setDisplayName("Jane");
        when(personRepositoryWrapper.findByIdWithException(personId)).thenReturn(contactPerson);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals(contactIdentifier, result.getContactIdentifier(),
                "Should use first contact from contacts list when no primary contact ID in otherDetails");
    }

    // ── createOrGetLead: new lead creation ──

    @Test
    void createOrGetLead_whenNoPersonExists_createsNewLead() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        CreateLeadResponse createResponse = CreateLeadResponse.builder()
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .build();
        when(leadWriteService.createLead(any(CreateLeadRequest.class))).thenReturn(createResponse);

        WhatsAppLeadResponse result = service.createOrGetLead(request);

        assertEquals(leadIdentifier, result.getLeadIdentifier(),
                "Should return newly created lead's identifier");
        assertEquals(contactIdentifier, result.getContactIdentifier(),
                "Should return newly created contact's identifier");
        assertNull(result.getStatus(),
                "New lead response should have null status field");
        verify(leadWriteService).createLead(any(CreateLeadRequest.class));
    }

    @Test
    void createOrGetLead_whenNewLeadWithSourcingChannel_updatesSourcingDetails() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        CreateLeadResponse createResponse = CreateLeadResponse.builder()
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .build();
        when(leadWriteService.createLead(any(CreateLeadRequest.class))).thenReturn(createResponse);

        service.createOrGetLead(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(leadWriteService).updateSourcingDetails(eq(leadIdentifier), captor.capture());
        assertEquals("WHATSAPP", captor.getValue().getSourcingChannel(),
                "Sourcing channel should match the request value");
    }

    @Test
    void createOrGetLead_whenNewLeadWithoutSourcingChannel_skipsSourcingUpdate() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        CreateLeadResponse createResponse = CreateLeadResponse.builder()
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .build();
        when(leadWriteService.createLead(any(CreateLeadRequest.class))).thenReturn(createResponse);

        service.createOrGetLead(request);

        verify(leadWriteService, never()).updateSourcingDetails(any(UUID.class), any(UpdateSourcingDetailsRequest.class));
    }

    @Test
    void createOrGetLead_whenNewLeadWithMarketingDetails_setsSourcingFields() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID contactIdentifier = UUID.randomUUID();

        WhatsAppLeadRequest.MarketingDetails marketing = new WhatsAppLeadRequest.MarketingDetails();
        marketing.setSourceId("SRC-001");
        marketing.setSourceUrl("https://example.com");

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");
        request.setSourcing_channel_name("WHATSAPP");
        request.setMarketing_details(marketing);

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        CreateLeadResponse createResponse = CreateLeadResponse.builder()
                .leadIdentifier(leadIdentifier)
                .contactIdentifier(contactIdentifier)
                .build();
        when(leadWriteService.createLead(any(CreateLeadRequest.class))).thenReturn(createResponse);

        service.createOrGetLead(request);

        ArgumentCaptor<UpdateSourcingDetailsRequest> captor = ArgumentCaptor.forClass(UpdateSourcingDetailsRequest.class);
        verify(leadWriteService).updateSourcingDetails(eq(leadIdentifier), captor.capture());
        assertEquals("SRC-001", captor.getValue().getSourceId(),
                "Source ID should be set from marketing details");
        assertEquals("https://example.com", captor.getValue().getSourceUrl(),
                "Source URL should be set from marketing details");
    }

    // ── createOrGetLead: JDBC error handling ──

    @Test
    void createOrGetLead_whenActiveLeadJdbcQueryFails_throwsRuntimeException() {
        Long personId = 1L;

        WhatsAppLeadRequest request = new WhatsAppLeadRequest();
        request.setMobileNumber("9876543210");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB error"));

        assertThrows(RuntimeException.class,
                () -> service.createOrGetLead(request),
                "Should wrap DataAccessException in RuntimeException when active lead query fails");
    }

    // ── helpers ──

    private Lead buildLeadWithFullDetails(UUID leadIdentifier, Long primaryContactId) {
        Lead lead = new Lead();
        lead.setLeadIdentifier(leadIdentifier);
        lead.setStatus(LeadStatus.ACTIVE);
        lead.setSubstatus(LeadSubStatus.ONHOLD);
        lead.setReasons(null);
        lead.setWorkflowDetails(Lead.WorkflowDetails.builder()
                .currentStageDetails(Lead.CurrentStageDetails.builder()
                        .stageKey("VERIFICATION")
                        .build())
                .build());
        lead.setOtherDetails(Lead.OtherDetails.builder()
                .primaryContactId(primaryContactId)
                .build());
        lead.setContacts(List.of(primaryContactId));
        return lead;
    }

    private Contact buildContact(UUID contactIdentifier, Long personId) {
        Contact contact = new Contact();
        contact.setIdentifier(contactIdentifier);
        contact.setPersonId(personId);
        return contact;
    }
}
