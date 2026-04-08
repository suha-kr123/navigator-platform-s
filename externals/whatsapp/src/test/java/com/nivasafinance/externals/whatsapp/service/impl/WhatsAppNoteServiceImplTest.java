package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteResponse;
import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadNoteWriteService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppNoteServiceImplTest {

    @Mock
    private LeadNoteWriteService leadNoteWriteService;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WhatsAppNoteServiceImpl service;

    // ── createNote: with leadIdentifier provided ──

    @Test
    void createNote_withLeadIdentifier_createsNoteSuccessfully() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID noteIdentifier = UUID.randomUUID();

        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTitle("Test Title");
        request.setContent("Test Content");

        LeadNoteCreateResponse noteResponse = LeadNoteCreateResponse.builder()
                .noteIdentifier(noteIdentifier)
                .build();
        when(leadNoteWriteService.createLeadNote(eq(leadIdentifier), any(LeadNoteCreateRequest.class)))
                .thenReturn(noteResponse);

        WhatsAppNoteResponse result = service.createNote(request);

        assertEquals(noteIdentifier, result.getNoteIdentifier(),
                "Note identifier should match the created note");
        verify(leadNoteWriteService).createLeadNote(eq(leadIdentifier), any(LeadNoteCreateRequest.class));
        verifyNoInteractions(personRepositoryWrapper, jdbcTemplate);
    }

    // ── createNote: resolve via mobileNumber ──

    @Test
    void createNote_withMobileNumber_resolvesActiveLeadAndCreatesNote() {
        Long personId = 1L;
        Long leadId = 10L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID noteIdentifier = UUID.randomUUID();

        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("9876543210");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = new Lead();
        lead.setLeadIdentifier(leadIdentifier);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        LeadNoteCreateResponse noteResponse = LeadNoteCreateResponse.builder()
                .noteIdentifier(noteIdentifier)
                .build();
        when(leadNoteWriteService.createLeadNote(eq(leadIdentifier), any(LeadNoteCreateRequest.class)))
                .thenReturn(noteResponse);

        WhatsAppNoteResponse result = service.createNote(request);

        assertEquals(noteIdentifier, result.getNoteIdentifier(),
                "Note identifier should match the created note resolved via mobile number");
    }

    @Test
    void createNote_withMobileNumberAndNoActiveLead_fallsBackToAnyLead() {
        Long personId = 1L;
        Long leadId = 10L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID noteIdentifier = UUID.randomUUID();

        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("9876543210");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new EmptyResultDataAccessException(1))
                .thenReturn(leadId);

        Lead lead = new Lead();
        lead.setLeadIdentifier(leadIdentifier);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        LeadNoteCreateResponse noteResponse = LeadNoteCreateResponse.builder()
                .noteIdentifier(noteIdentifier)
                .build();
        when(leadNoteWriteService.createLeadNote(eq(leadIdentifier), any(LeadNoteCreateRequest.class)))
                .thenReturn(noteResponse);

        WhatsAppNoteResponse result = service.createNote(request);

        assertEquals(noteIdentifier, result.getNoteIdentifier(),
                "Should fall back to any lead when no active lead found");
        verify(jdbcTemplate, times(2)).queryForObject(anyString(), eq(Long.class), eq(personId));
    }

    // ── createNote: validation errors ──

    @Test
    void createNote_withNullLeadIdentifierAndNullMobileNumber_throwsIllegalArgumentException() {
        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");

        assertThrows(IllegalArgumentException.class,
                () -> service.createNote(request),
                "Should throw when neither leadIdentifier nor mobileNumber is provided");
    }

    @Test
    void createNote_withNullLeadIdentifierAndEmptyMobileNumber_throwsIllegalArgumentException() {
        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        assertThrows(IllegalArgumentException.class,
                () -> service.createNote(request),
                "Should throw when mobileNumber is empty and leadIdentifier is null");
    }

    @Test
    void createNote_whenPersonNotFoundByMobileNumber_throwsIllegalArgumentException() {
        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("9876543210");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createNote(request),
                "Should throw when no person found for mobile number");
        assertTrue(ex.getMessage().contains("9876543210"),
                "Exception message should contain the mobile number");
    }

    @Test
    void createNote_whenNoLeadFoundForPerson_throwsIllegalArgumentException() {
        Long personId = 1L;

        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("9876543210");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createNote(request),
                "Should throw when no lead found for person");
        assertTrue(ex.getMessage().contains("9876543210"),
                "Exception message should contain the mobile number");
    }

    // ── createNote: JDBC error handling ──

    @Test
    void createNote_whenActiveLeadJdbcQueryFails_throwsRuntimeException() {
        Long personId = 1L;

        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("9876543210");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB error"));

        assertThrows(RuntimeException.class,
                () -> service.createNote(request),
                "Should wrap DataAccessException in RuntimeException when active lead query fails");
    }

    @Test
    void createNote_whenAnyLeadJdbcQueryFails_throwsRuntimeException() {
        Long personId = 1L;

        WhatsAppNoteRequest request = new WhatsAppNoteRequest();
        request.setMobileNumber("9876543210");
        request.setTitle("Test Title");
        request.setContent("Test Content");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new EmptyResultDataAccessException(1))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB error"));

        assertThrows(RuntimeException.class,
                () -> service.createNote(request),
                "Should wrap DataAccessException in RuntimeException when any-lead query fails");
    }
}
