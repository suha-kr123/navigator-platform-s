package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppNoteResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppNoteService;
import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadNoteWriteService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class WhatsAppNoteServiceImpl implements WhatsAppNoteService {

    private final LeadNoteWriteService leadNoteWriteService;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public WhatsAppNoteResponse createNote(WhatsAppNoteRequest request) {
        UUID leadIdentifier = resolveLeadIdentifier(request);

        LeadNoteCreateRequest leadNoteCreateRequest = LeadNoteCreateRequest.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        LeadNoteCreateResponse noteResponse = leadNoteWriteService.createLeadNote(leadIdentifier, leadNoteCreateRequest);

        return WhatsAppNoteResponse.builder()
                .noteIdentifier(noteResponse.getNoteIdentifier())
                .build();
    }

    private UUID resolveLeadIdentifier(WhatsAppNoteRequest request) {
        if (ValidationUtils.isNonNull(request.getLeadIdentifier())) {
            return request.getLeadIdentifier();
        }

        if (ValidationUtils.isNullOrEmpty(request.getMobileNumber())) {
            throw new IllegalArgumentException("Either leadIdentifier or mobileNumber (10 digits) must be provided");
        }

        Optional<Person> person = personRepositoryWrapper.findByPrimaryMobileNumber(request.getMobileNumber());
        if (person.isEmpty()) {
            throw new IllegalArgumentException("No person found with mobile number: " + request.getMobileNumber());
        }

        Optional<Lead> lead = findLeadByContactPersonId(person.get().getId());
        if (lead.isEmpty()) {
            throw new IllegalArgumentException("No lead found for mobile number: " + request.getMobileNumber());
        }

        return lead.get().getLeadIdentifier();
    }

    private Optional<Lead> findLeadByContactPersonId(Long personId) {
        Optional<Lead> activeLead = findActiveLeadByContactPersonId(personId);
        if (activeLead.isPresent()) {
            return activeLead;
        }
        return findAnyLeadByContactPersonId(personId);
    }

    private Optional<Lead> findActiveLeadByContactPersonId(Long personId) {
        try {
            String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint as id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                  AND l.status = 'ACTIVE'
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;

            Long leadId = jdbcTemplate.queryForObject(sql, Long.class, personId);
            Lead lead = leadId != null ? leadRepositoryWrapper.findByIdWithException(leadId) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find active lead by contact person", e);
        }
    }

    private Optional<Lead> findAnyLeadByContactPersonId(Long personId) {
        try {
            String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint as id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;

            Long leadId = jdbcTemplate.queryForObject(sql, Long.class, personId);
            Lead lead = leadId != null ? leadRepositoryWrapper.findByIdWithException(leadId) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find lead by contact person", e);
        }
    }
}
