package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.LeadNotFoundException;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LeadRepositoryWrapper {

    private final LeadRepository leadRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final CodeValueMasterService codeValueMasterService;
    private final ProductReadService productReadService;

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
                     l.product_code as productCode,
                     l.purpose,
                     l.office_key as officeKey,
                     l.owner as ownerUsername,
                     l.created_at as leadCreatedAt,
                     l.status,
                     l.substatus as subStatus,
                    (l.other_details->>'preferredCallStartTime')::time as preferred_call_start_time,
                    (l.other_details->>'preferredCallEndTime')::time as preferred_call_end_time,
                    l.other_details->>'priority' as priority_key,
                     CASE
                         WHEN l.status = 'REJECTED' AND l.reasons ->> 'reject' IS NOT NULL
                             THEN l.reasons ->> 'reject'
                         WHEN l.status = 'ONHOLD' AND l.reasons ->> 'onhold' IS NOT NULL
                             THEN l.reasons ->> 'onhold'
                         WHEN l.status = 'WITHDRAWN' AND l.reasons ->> 'withdrawn' IS NOT NULL
                             THEN l.reasons ->> 'withdrawn'
                         ELSE NULL
                         END as reasonCode,
                    -- Primary person name priority: decision maker contact -> first contact
                    COALESCE(
                        decision_maker_person.display_name,
                        fallback_contact_person.display_name
                    ) as primaryPersonName,
                    -- Primary person number priority: decision maker contact -> first contact
                    -- Extract first primary number from mobile_numbers JSONB array
                    COALESCE(
                        (
                            SELECT mn->>'number'
                            FROM jsonb_array_elements(COALESCE(decision_maker_person.mobile_numbers, '[]'::jsonb)) mn
                            WHERE (mn->>'isPrimary')::boolean = true
                            LIMIT 1
                        ),
                        (
                            SELECT mn->>'number'
                            FROM jsonb_array_elements(COALESCE(fallback_contact_person.mobile_numbers, '[]'::jsonb)) mn
                            WHERE (mn->>'isPrimary')::boolean = true
                            LIMIT 1
                        )
                    ) as primaryPersonNumber,
                     o.name as officeName
                 FROM n_lead l
                -- Left join with decision maker contact -> person
                LEFT JOIN LATERAL (
                    SELECT c.id as contact_id, c.person_id
                    FROM jsonb_array_elements(COALESCE(l.contacts, '[]'::jsonb)) AS cont
                    JOIN n_contact c ON c.id = (cont)::bigint
                    WHERE c.decision_maker = true
                    LIMIT 1
                ) decision_maker_contact ON true
                LEFT JOIN n_person decision_maker_person ON
                    decision_maker_contact.person_id = decision_maker_person.id
                -- Left join with first contact -> person (fallback)
                LEFT JOIN LATERAL (
                    SELECT c.id as contact_id, c.person_id
                    FROM jsonb_array_elements(COALESCE(l.contacts, '[]'::jsonb)) AS cont
                    JOIN n_contact c ON c.id = (cont)::bigint
                    ORDER BY cont
                    LIMIT 1
                ) fallback_contact ON true
                LEFT JOIN n_person fallback_contact_person ON
                    fallback_contact.person_id = fallback_contact_person.id
                 LEFT JOIN n_office o ON o.key = l.office_key
                 WHERE l.lead_identifier = ?
                    \s""";

        try {
            LeadResponse leadResponse = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapLeadResponse(rs), leadIdentifier);
            if (leadResponse == null) {
                throw new LeadNotFoundException(leadIdentifier, messageSource);
            }
            return leadResponse;
        } catch (EmptyResultDataAccessException e) {
            throw new LeadNotFoundException(leadIdentifier, messageSource);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve lead response by identifier", e);
        }
    }

    /**
     * Find active or onhold lead where the given person is a contact.
     * Active leads are those with status = 'ACTIVE' and substatus is either NULL or 'ONHOLD'.
     *
     * @param personId The person ID to search for in contacts
     * @return Optional containing the lead if found, empty otherwise
     */
    public Optional<Lead> findActiveLeadByContactPersonId(Long personId) {
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
                LIMIT 1
                """;
            
            Long leadId = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                personId
            );
            Lead lead = leadId != null ? leadRepository.findById(leadId).orElse(null) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find active lead by contact person", e);
        }
    }

    private LeadResponse mapLeadResponse(ResultSet rs) throws SQLException {
        UUID leadIdentifier = rs.getString("leadIdentifier") != null ? UUID.fromString(rs.getString("leadIdentifier")) : null;

        LeadResponse.LeadResponseBuilder builder = LeadResponse.builder()
                .leadIdentifier(leadIdentifier)
                .requestedAmount(rs.getBigDecimal("requestedAmount"))
                .productCode(rs.getString("productCode"))
                .purpose(rs.getString("purpose"))
                .officeKey(rs.getString("officeKey"))
                .ownerUsername(rs.getString("ownerUsername"))
                .primaryPersonName(rs.getString("primaryPersonName"))
                .primaryPersonNumber(rs.getString("primaryPersonNumber"))
                .officeName(rs.getString("officeName"))
                .reasonCode(rs.getString("reasonCode"))
                .preferredCallStartTime(getLocalTime(rs, "preferred_call_start_time"))
                .preferredCallEndTime(getLocalTime(rs, "preferred_call_end_time"));

        String status = rs.getString("status");
        if (status != null) {
            try {
                builder.status(LeadStatus.valueOf(status));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String subStatus = rs.getString("subStatus");
        if (subStatus != null) {
            try {
                builder.subStatus(LeadSubStatus.valueOf(subStatus));
            } catch (IllegalArgumentException ignored) {
            }
        }

        builder.leadCreatedAt(getLocalDate(rs, "leadCreatedAt"));

        LeadResponse leadResponse = builder.build();

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
        if(leadResponse.getProductCode() != null) {
            leadResponse.setProductName(productReadService.getProductByCode(leadResponse.getProductCode()).getName());
        }

        String priorityKey = rs.getString("priority_key");
        if (priorityKey != null) {
            CodeValueResponse priority = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                    priorityKey,
                    SystemControlledMasterCodes.LEAD_PRIORITY_MASTER
            );
            leadResponse.setPriority(priority);
        }

        return leadResponse;
    }

    private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) != null ? rs.getTimestamp(column).toLocalDateTime().toLocalDate() : null;
    }

    private LocalTime getLocalTime(ResultSet rs, String column) throws SQLException {
        return rs.getTime(column) != null ? rs.getTime(column).toLocalTime() : null;
    }
}
