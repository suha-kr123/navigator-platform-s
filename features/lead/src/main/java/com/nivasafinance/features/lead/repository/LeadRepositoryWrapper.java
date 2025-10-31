package com.nivasafinance.features.lead.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.lead.dto.LeadDocumentResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadDocumentStatus;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.LeadNotFoundException;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LeadRepositoryWrapper {

    private final LeadRepository leadRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final CodeValueMasterService codeValueMasterService;
    private final ObjectMapper objectMapper;

    public Lead saveWithException(Lead lead) {
        try {
            return leadRepository.save(lead);
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to save lead", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Lead findByIdWithException(Long id) {
        try {
            return leadRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve lead", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Lead findByLeadIdentifierWithException(UUID leadIdentifier) {
        try {
            return leadRepository.findByLeadIdentifier(leadIdentifier)
                    .orElseThrow(() -> new RuntimeException("Lead not found with identifier: " + leadIdentifier));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve lead by identifier", e);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Get LeadResponse by lead identifier using JDBC template.
     * Primary person name and number priority: applicant -> first co-applicant -> first contact
     * Reason code/value extracted from reasons.reject[0] if exists
     */
    public LeadResponse findLeadResponseByIdentifierWithException(UUID leadIdentifier) {
        String sql = """
                SELECT
                     l.lead_identifier as leadIdentifier,
                     l.requested_amount as requestedAmount,
                     l.purpose,
                     l.office_key as officeKey,
                     l.workflow_details->>'currentStage' as currentStage,
                     l.owner as ownerUsername,
                     l.status,
                     l.substatus as subStatus,
                     CASE
                         WHEN l.status = 'REJECTED' AND l.reasons ->> 'reject' IS NOT NULL
                             THEN l.reasons ->> 'reject'
                         WHEN l.status = 'ONHOLD' AND l.reasons ->> 'onhold' IS NOT NULL
                             THEN l.reasons ->> 'onhold'
                         WHEN l.status = 'WITHDRAWN' AND l.reasons ->> 'withdrawn' IS NOT NULL
                             THEN l.reasons ->> 'withdrawn'
                         ELSE NULL
                         END as reasonCode,
                     -- Primary person name priority: applicant -> co-applicant -> contact
                     COALESCE(
                         applicant_person.display_name,
                         co_applicant_person.display_name,
                         contact_person.display_name
                     ) as primaryPersonName,
                     -- Primary person number priority: applicant -> co-applicant -> contact
                     -- Extract first primary number or first number from mobile_numbers JSONB array
                     COALESCE(
                         -- Applicant person mobile number (first primary or first number)
                         (
                             SELECT mn->>'number'
                             FROM jsonb_array_elements(applicant_person.mobile_numbers) mn
                             WHERE (mn->>'isPrimary')::boolean = true
                             LIMIT 1
                         ),
                         -- Co-applicant person mobile number
                         (
                             SELECT mn->>'number'
                             FROM jsonb_array_elements(co_applicant_person.mobile_numbers) mn
                             WHERE (mn->>'isPrimary')::boolean = true
                             LIMIT 1
                         ),
                         -- Contact person mobile number
                         (
                             SELECT mn->>'number'
                             FROM jsonb_array_elements(contact_person.mobile_numbers) mn
                             WHERE (mn->>'isPrimary')::boolean = true
                             LIMIT 1
                         )
                     ) as primaryPersonNumber,
                     NULL as officeName  -- TODO: Join with office table if needed
                 FROM n_lead l
                 -- Left join with applicant -> person (for primary person name/number)
                 LEFT JOIN n_applicant applicant ON
                     (l.applicant_details->>'applicantId')::bigint = applicant.id
                 LEFT JOIN n_person applicant_person ON
                     applicant.person_id = applicant_person.id
                 -- Left join with first co-applicant -> person
                 LEFT JOIN LATERAL (
                     SELECT (co_app->>'applicantId')::bigint as applicant_id
                     FROM jsonb_array_elements(l.co_applicant_details) AS co_app
                     LIMIT 1
                 ) first_co_applicant ON true
                 LEFT JOIN n_applicant co_applicant ON
                     first_co_applicant.applicant_id = co_applicant.id
                 LEFT JOIN n_person co_applicant_person ON
                     co_applicant.person_id = co_applicant_person.id
                 -- Left join with first contact -> person
                 LEFT JOIN LATERAL (
                     SELECT (cont->>'contactId')::bigint as contact_id
                     FROM jsonb_array_elements(l.contact_details) AS cont
                     LIMIT 1
                 ) first_contact ON true
                 LEFT JOIN n_contact contact ON
                     first_contact.contact_id = contact.id
                 LEFT JOIN n_person contact_person ON
                     contact.person_id = contact_person.id
                
                 WHERE l.lead_identifier = ?
                    \s""";

        try {
            LeadResponse leadResponse = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(LeadResponse.class),
                    leadIdentifier
            );
            if (leadResponse == null) {
                throw new LeadNotFoundException(leadIdentifier, messageSource);
            }
            if (leadResponse.getReasonCode() != null) {
                if (leadResponse.getSubStatus() == LeadSubStatus.ONHOLD) {
                    CodeValueResponse codeValueResponse = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                            leadResponse.getReasonCode(), SystemControlledMasterCodes.LEAD_ONHOLD_REASON_MASTER);
                    leadResponse.setReason(codeValueResponse.getValue());
                }
                if (leadResponse.getStatus() == LeadStatus.REJECTED) {
                    CodeValueResponse codeValueResponse = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                            leadResponse.getReasonCode(), SystemControlledMasterCodes.LEAD_REJECT_REASON_MASTER);
                    leadResponse.setReason(codeValueResponse.getValue());
                }
                if (leadResponse.getStatus() == LeadStatus.WITHDRAWN) {
                    CodeValueResponse codeValueResponse = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                            leadResponse.getReasonCode(), SystemControlledMasterCodes.LEAD_WITHDRAWAL_REASON_MASTER);
                    leadResponse.setReason(codeValueResponse.getValue());
                }
            }
            return leadResponse;
        } catch (EmptyResultDataAccessException e) {
            throw new LeadNotFoundException(leadIdentifier, messageSource);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve lead response by identifier", e);
        }
    }

    private final String sqlQueryForLeadDocuments = """
              SELECT
                  d.identifier,
                  d.name,
                  d.type,
                  d.size,
                  d.created_at as createdAt,
                  d.created_by as createdBy,
                  doc_detail->>'status' as status,
                  doc_detail->'tag' as tags
              FROM n_lead l
              CROSS JOIN LATERAL jsonb_array_elements(l.document_details) doc_detail
              JOIN n_document d ON (doc_detail->>'id')::bigint = d.id
            """;

    /**
     * Private static RowMapper for mapping ResultSet to LeadDocumentResponse.
     * Reusable across multiple query methods.
     */
    private record LeadDocumentRowMapper(CodeValueMasterService codeValueMasterService,
                                         ObjectMapper objectMapper) implements RowMapper<LeadDocumentResponse> {

        @Override
        public LeadDocumentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadDocumentResponse response = new LeadDocumentResponse();
            response.setIdentifier(UUID.fromString(rs.getString("identifier")));
            response.setName(rs.getString("name"));
            response.setType(rs.getString("type"));
            response.setSize(rs.getLong("size"));
            response.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            response.setCreatedBy(rs.getString("createdBy"));

            String statusStr = rs.getString("status");
            if (statusStr != null) {
                response.setStatus(LeadDocumentStatus.valueOf(statusStr));
            }

            String tagsJson = rs.getString("tags");
            if (tagsJson != null && !tagsJson.equals("null")) {
                try {
                    // Parse JSONB array to List<String> using Jackson
                    List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
                    });
                    List<CodeValueResponse> tagsCodes = codeValueMasterService.getCodeValueByKeysAndCodeKey(tags, SystemControlledMasterCodes.DOCUMENT_MASTER);
                    response.setTags(tagsCodes);
                } catch (Exception e) {
                    response.setTags(null);
                }
            }

            return response;
        }
    }

    /**
     * Get all documents for a lead by lead identifier.
     * Joins n_lead and n_document tables, extracts tags and status from Lead's documentDetails JSONB.
     */
    public List<LeadDocumentResponse> findAllDocumentsByLeadIdentifier(UUID leadIdentifier) {
        String sql = sqlQueryForLeadDocuments + " WHERE l.lead_identifier = ?";

        try {
            return jdbcTemplate.query(sql, new LeadDocumentRowMapper(codeValueMasterService, objectMapper), leadIdentifier);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve documents for lead: " + leadIdentifier, e);
        }
    }

    /**
     * Get a specific document for a lead by lead identifier and document identifier.
     */
    public LeadDocumentResponse findDocumentByLeadIdentifierAndDocumentIdentifier(
            UUID leadIdentifier, UUID documentIdentifier) {
        String sql = sqlQueryForLeadDocuments + " WHERE l.lead_identifier = ? AND d.identifier = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new LeadDocumentRowMapper(codeValueMasterService, objectMapper),
                    leadIdentifier, documentIdentifier);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Document not found for lead: " + leadIdentifier +
                                       " with document identifier: " + documentIdentifier);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve document for lead: " + leadIdentifier, e);
        }
    }
}
