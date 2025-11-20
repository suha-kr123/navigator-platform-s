package com.nivasafinance.features.lead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.LeadSearchRequest;
import com.nivasafinance.features.lead.dto.LeadSearchResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.LeadNotFoundException;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.service.StaffReadService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
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
    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

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
                    (l.proposed_details->>'proposedLoanAmount')::numeric as proposed_amount,
                    (l.proposed_details->>'roi')::numeric as proposed_roi,
                    (l.credit_rating_details->>'eligibleLoanAmount')::numeric as eligible_loan_amount,
                    l.credit_rating_details->>'bureauRating' as bureau_rating_key,
                    l.credit_rating_details->>'customerProfiles' as customer_profiles_key,
                    l.credit_rating_details->>'monthlyFamilyIncome' as monthly_family_income_key,
                    advisor.identifier::text as advisor_identifier,
                    advisor_person.display_name as advisor_name,
                    (jsonb_path_query_first(COALESCE(advisor_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS advisor_number,
                    latest_note.content as recent_note,
                    latest_note.created_by as recent_note_created_by,
                    latest_note.created_at as recent_note_created_at,
                    latest_lender.lender_identifier::text as lender_identifier,
                    lender.name as lender_name,
                    latest_lender.status as lender_status,
                    latest_lender.stage as lender_stage_key,
                    lender_office.name as lender_office_name,
                     CASE
                         WHEN l.substatus = 'ONHOLD' AND l.reasons ->> 'onhold' IS NOT NULL
                             THEN l.reasons ->> 'onhold'
                         WHEN l.status = 'REJECTED' AND l.reasons ->> 'reject' IS NOT NULL
                             THEN l.reasons ->> 'reject'
                         WHEN l.status = 'WITHDRAWN' AND l.reasons ->> 'withdrawn' IS NOT NULL
                             THEN l.reasons ->> 'withdrawn'
                         ELSE NULL
                         END as reasonCode,
                    primary_contact_person.display_name         AS primaryPersonName,
                    (jsonb_path_query_first(COALESCE(primary_contact_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primaryPersonNumber,
                     o.name as officeName
                 FROM n_lead l
                -- Left join with primary contact from other_details -> person
                LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person primary_contact_person ON primary_contact.person_id = primary_contact_person.id
                LEFT JOIN n_advisor_lead_mapping alm ON alm.lead_id = l.id
                LEFT JOIN n_advisor advisor ON advisor.id = alm.advisor_id
                LEFT JOIN n_person advisor_person ON advisor.person_id = advisor_person.id
                LEFT JOIN LATERAL (
                    SELECT n.content,
                           n.created_at,
                           n.created_by
                    FROM jsonb_array_elements_text(COALESCE(l.notes, '[]'::jsonb)) note_id
                    JOIN n_note n ON n.id = note_id::bigint
                    ORDER BY n.created_at DESC
                    LIMIT 1
                ) latest_note ON true
                LEFT JOIN LATERAL (
                    SELECT ll.lender_identifier,
                           ll.lender_key,
                           ll.lender_office_key,
                           ll.status,
                           ll.stage
                    FROM n_lead_lender ll
                    WHERE ll.lead_id = l.id
                      AND ll.status IN ('SELECTED','SUBMITTED')
                    ORDER BY ll.updated_at DESC
                    LIMIT 1
                ) latest_lender ON true
                LEFT JOIN n_lender lender ON lender.key = latest_lender.lender_key
                LEFT JOIN n_lender_office lender_office ON lender_office.key = latest_lender.lender_office_key
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
                .proposedAmount(rs.getBigDecimal("proposed_amount"))
                .proposedRoi(rs.getBigDecimal("proposed_roi"))
                .eligibleLoanAmount(rs.getBigDecimal("eligible_loan_amount"))
                .recentNote(rs.getString("recent_note"))
                .noteCreatedBy(rs.getString("recent_note_created_by"))
                .noteCreatedAt(getLocalDateTime(rs, "recent_note_created_at"))
                .advisorIdentifier(rs.getString("advisor_identifier"))
                .advisorName(rs.getString("advisor_name"))
                .advisorNumber(rs.getString("advisor_number"))
                .lenderIdentifier(rs.getString("lender_identifier"))
                .lenderName(rs.getString("lender_name"))
                .lenderStatus(rs.getString("lender_status"))
                .lenderOfficeName(rs.getString("lender_office_name"))
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

        String bureauRatingKey = rs.getString("bureau_rating_key");
        if (bureauRatingKey != null) {
            CodeValueResponse bureauRating = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                    bureauRatingKey,
                    SystemControlledMasterCodes.LEAD_BUREAU_RATING_MASTER
            );
            leadResponse.setBureauRating(bureauRating);
        }

        String customerProfilesKey = rs.getString("customer_profiles_key");
        if (customerProfilesKey != null) {
            CodeValueResponse customerProfile = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                    customerProfilesKey,
                    SystemControlledMasterCodes.LEAD_CUSTOMER_PROFILE_MASTER
            );
            leadResponse.setCustomerProfiles(customerProfile);
        }

        String monthlyFamilyIncomeKey = rs.getString("monthly_family_income_key");
        if (monthlyFamilyIncomeKey != null) {
            CodeValueResponse monthlyIncome = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                    monthlyFamilyIncomeKey,
                    SystemControlledMasterCodes.LEAD_MONTHLY_INCOME_MASTER
            );
            leadResponse.setMonthlyFamilyIncome(monthlyIncome);
        }

        String lenderStageKey = rs.getString("lender_stage_key");
        if (lenderStageKey != null) {
            CodeValueResponse lenderStage = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                    lenderStageKey,
                    SystemControlledMasterCodes.LENDER_STAGE_MASTER
            );
            leadResponse.setLenderStage(lenderStage);
        }

        return leadResponse;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) != null ? rs.getTimestamp(column).toLocalDateTime() : null;
    }

    private LocalDate getLocalDate(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) != null ? rs.getTimestamp(column).toLocalDateTime().toLocalDate() : null;
    }

    private LocalTime getLocalTime(ResultSet rs, String column) throws SQLException {
        return rs.getTime(column) != null ? rs.getTime(column).toLocalTime() : null;
    }

    /**
     * Search leads by phone number.
     * Finds persons with the given phone number, then finds contacts associated with those persons,
     * and finally finds leads that contain those contacts.
     *
     * @param paginationRequest Pagination parameters
     * @param request Search request containing mobile number
     * @return PaginatedResponse containing LeadSearchResponse objects
     */
    public PaginatedResponse<LeadSearchResponse> searchLeadsByPhoneNumber(
            PaginationRequest paginationRequest, LeadSearchRequest request) {
        // Validate phone number
        if (request == null || !StringUtils.hasText(request.getMobileNumber())) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }

        String mobileNumber = request.getMobileNumber().trim();
        
        // Get current staff office and code for hierarchy filtering
        String currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
        String currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();

        // Build SQL query to search leads by phone number
        // Start with lead, join to contacts, then to person with matching phone number
        // Apply office hierarchy filter to restrict to current staff's office hierarchy
        String countSql = """
            SELECT COUNT(DISTINCT l.id)
            FROM n_lead l
            LEFT JOIN n_office o ON o.key = l.office_key
            JOIN LATERAL (
                SELECT (contact_id)::bigint as id
                FROM jsonb_array_elements_text(COALESCE(l.contacts, '[]'::jsonb)) AS contact_id
            ) contact_ids ON true
            JOIN n_contact matching_contact ON matching_contact.id = contact_ids.id
            JOIN n_person matching_person ON matching_person.id = matching_contact.person_id
            WHERE EXISTS (
                SELECT 1 FROM jsonb_array_elements(COALESCE(matching_person.mobile_numbers, '[]'::jsonb)) AS m
                WHERE m->>'number' = ?
            )
            AND o.code LIKE ?
            """;

        String dataSql = """
            SELECT DISTINCT
                l.lead_identifier,
                l.requested_amount,
                p.name as product_name,
                primary_contact.identifier as primary_person_identifier,
                primary_person.display_name as primary_person_name,
                (jsonb_path_query_first(COALESCE(primary_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_person_number,
                matching_contact.identifier as contact_person_identifier,
                matching_person.display_name as contact_person_name,
                (jsonb_path_query_first(COALESCE(matching_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS contact_person_number,
                l.status,
                l.substatus,
                l.created_at as lead_created_at,
                l.updated_at as last_activity_date
            FROM n_lead l
            LEFT JOIN n_office o ON o.key = l.office_key
            JOIN LATERAL (
                SELECT (contact_id)::bigint as id
                FROM jsonb_array_elements_text(COALESCE(l.contacts, '[]'::jsonb)) AS contact_id
            ) contact_ids ON true
            JOIN n_contact matching_contact ON matching_contact.id = contact_ids.id
            JOIN n_person matching_person ON matching_person.id = matching_contact.person_id
            LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
            LEFT JOIN n_person primary_person ON primary_contact.person_id = primary_person.id
            LEFT JOIN n_product p ON p.code = l.product_code
            WHERE EXISTS (
                SELECT 1 FROM jsonb_array_elements(COALESCE(matching_person.mobile_numbers, '[]'::jsonb)) AS m
                WHERE m->>'number' = ?
            )
            AND o.code LIKE ?
            ORDER BY l.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            String officePattern = currentUserOfficeCode + "%";
            
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, mobileNumber, officePattern);
            long total = totalCount != null ? totalCount : 0L;

            // Get paginated data
            List<LeadSearchResponse> results = jdbcTemplate.query(
                    dataSql,
                    new LeadSearchRowMapper(),
                    mobileNumber,
                    officePattern,
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset()
            );

            PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, total);
            return new PaginatedResponse<>(results, paginationInfo);
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to search leads by phone number", e);
        }
    }

    private PaginationInfo buildPaginationInfo(PaginationRequest paginationRequest, long totalElements) {
        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) totalElements / limit);
        int currentPage = limit == 0 ? 0 : offset / limit;
        boolean hasNext = offset + limit < totalElements;
        boolean hasPrevious = offset > 0;

        return PaginationInfo.builder()
                .offset(offset)
                .limit(limit)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    private static class LeadSearchRowMapper implements RowMapper<LeadSearchResponse> {
        @Override
        public LeadSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadSearchResponse.LeadSearchResponseBuilder builder = LeadSearchResponse.builder();

            String leadIdentifierStr = rs.getString("lead_identifier");
            if (leadIdentifierStr != null) {
                builder.leadIdentifier(UUID.fromString(leadIdentifierStr));
            }

            builder.requestedAmount(rs.getBigDecimal("requested_amount"));

            String primaryPersonIdentifierStr = rs.getString("primary_person_identifier");
            if (primaryPersonIdentifierStr != null) {
                builder.primaryPersonIdentifier(UUID.fromString(primaryPersonIdentifierStr));
            }

            builder.primaryPersonName(rs.getString("primary_person_name"));
            builder.primaryPersonNumber(rs.getString("primary_person_number"));

            String contactPersonIdentifierStr = rs.getString("contact_person_identifier");
            if (contactPersonIdentifierStr != null) {
                builder.contactPersonIdentifier(UUID.fromString(contactPersonIdentifierStr));
            }

            builder.contactPersonName(rs.getString("contact_person_name"));
            builder.contactPersonNumber(rs.getString("contact_person_number"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(LeadStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                    // Invalid status, leave as null
                }
            }

            String subStatus = rs.getString("substatus");
            if (subStatus != null) {
                try {
                    builder.subStatus(LeadSubStatus.valueOf(subStatus));
                } catch (IllegalArgumentException ignored) {
                    // Invalid substatus, leave as null
                }
            }

            java.sql.Timestamp leadCreatedAt = rs.getTimestamp("lead_created_at");
            if (leadCreatedAt != null) {
                builder.leadCreatedAt(leadCreatedAt.toLocalDateTime());
            }

            java.sql.Timestamp lastActivityDate = rs.getTimestamp("last_activity_date");
            if (lastActivityDate != null) {
                builder.lastActivityDate(lastActivityDate.toLocalDateTime());
            }

            String productName = rs.getString("product_name");
            if (productName != null) {
                builder.productName(productName);
            }

            return builder.build();
        }
    }
}
