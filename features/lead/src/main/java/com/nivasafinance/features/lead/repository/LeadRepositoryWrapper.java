package com.nivasafinance.features.lead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.AdminLeadSearchResponse;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.AdminLeadSearchRequest;
import com.nivasafinance.features.lead.dto.LeadSearchRequest;
import com.nivasafinance.features.lead.dto.LeadSearchResponse;
import com.nivasafinance.features.lead.dto.LeadWorkflowDetailsDto;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.exception.LeadConflictException;
import com.nivasafinance.features.lead.exception.LeadExceptionFactory;
import com.nivasafinance.features.lead.exception.LeadNotFoundException;
import com.nivasafinance.features.lead.exception.LeadOperationException;
import com.nivasafinance.features.lead.mapper.LeadBasicResponseMapper;
import com.nivasafinance.features.lead.mapper.LeadWorkflowDetailsRowMapper;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.staff.service.StaffReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class LeadRepositoryWrapper {

    private static final String LOG_SEARCH_PHONE_STAFF_MS = "searchLeadsByPhoneNumber: getCurrentStaff took {} ms";
    private static final String LOG_SEARCH_PHONE_OFFICE_MS = "searchLeadsByPhoneNumber: getOfficeByKey took {} ms";
    private static final String LOG_SEARCH_PHONE_COUNT_MS = "searchLeadsByPhoneNumber: count query took {} ms";
    private static final String LOG_SEARCH_PHONE_DATA_MS = "searchLeadsByPhoneNumber: data query took {} ms";
    private static final String LOG_SEARCH_PHONE_TOTAL_MS = "searchLeadsByPhoneNumber: total (incl. hierarchy) took {} ms";

    private final LeadRepository leadRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final CodeValueMasterService codeValueMasterService; 
    private final ProductReadService productReadService; 
    private final StaffReadService staffReadService; 
    private final OfficeReadService officeReadService;

    public Lead saveWithException(Lead lead) {
        try {
            return leadRepository.saveAndFlush(lead);
        } catch (OptimisticLockingFailureException e) {
            throw new LeadConflictException(
                    "error.lead.optimistic.locking.failure",
                    new Object[]{},
                    messageSource
            );
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to save lead", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Lead findByIdWithException(Long id) {
        try {
            return leadRepository.findById(id)
                    .orElseThrow(() -> new LeadNotFoundException(id, messageSource));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve lead", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Lead findByLeadIdentifierWithException(UUID leadIdentifier) {
        try {
            return leadRepository.findByLeadIdentifier(leadIdentifier)
                    .orElseThrow(() -> new LeadNotFoundException(leadIdentifier, messageSource));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve lead by identifier", e);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Unfiltered — returns lead including soft-deleted. Used by admin delete/undo-delete.
     */
    public Lead findByLeadIdentifierIncludingDeletedWithException(UUID leadIdentifier) {
        try {
            return leadRepository.findByLeadIdentifierIncludingDeleted(leadIdentifier)
                    .orElseThrow(() -> new LeadNotFoundException(leadIdentifier, messageSource));
        } catch (LeadNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            LeadOperationException exception = LeadExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Get LeadResponse by lead identifier using JDBC template.
     * Primary person name and number priority: applicant -> first co-applicant -> first contact
     * Reason code/value extracted from reasons.reject[0] if exists
     */
    @SuppressWarnings("text-blocks")
    public LeadResponse findLeadResponseByIdentifierWithException(UUID leadIdentifier) {
        String sql = """
                SELECT
                     l.lead_identifier as leadIdentifier,
                     l.requested_amount as requestedAmount,
                     l.product_code as productCode,
                     l.purpose,
                     l.customer_convince_status as customer_convince_status_key,
                     l.office_key as officeKey,
                     l.owner as ownerUsername,
                     l.created_at as leadCreatedAt,
                     l.status,
                     l.substatus as subStatus,
                    (l.other_details->>'preferredCallStartTime')::time as preferred_call_start_time,
                    (l.other_details->>'preferredCallEndTime')::time as preferred_call_end_time,
                    l.other_details->>'priority' as priority_key,
                    (l.other_details->>'noOfCampaignCalls')::bigint as no_of_campaign_calls,
                    (l.proposed_details->>'proposedLoanAmount')::numeric as proposed_amount,
                    (l.proposed_details->>'roi')::numeric as proposed_roi,
                    (l.credit_rating_details->>'eligibleLoanAmount')::numeric as eligible_loan_amount,
                    (l.disbursement_details->>'disbursedAmount')::numeric as disbursed_amount,
                    l.credit_rating_details->>'bureauRating' as bureau_rating_key,
                    l.credit_rating_details->>'customerProfiles' as customer_profiles_key,
                    l.credit_rating_details->>'monthlyFamilyIncome' as monthly_family_income_key,
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
                         WHEN l.substatus = 'DROPOFF' AND l.reasons ->> 'dropoff' IS NOT NULL
                             THEN l.reasons ->> 'dropoff'
                         ELSE NULL
                         END as reasonCode,
                    CASE
                        WHEN l.onhold_details->>'holdFollowUpDate' IS NOT NULL
                        THEN TO_DATE(l.onhold_details->>'holdFollowUpDate', 'DD-MM-YYYY')
                        ELSE NULL
                    END AS hold_follow_up_date,
                    primary_contact_person.display_name AS primaryPersonName,
                    (jsonb_path_query_first(COALESCE(primary_contact_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primaryPersonNumber,
                     o.name as officeName,
                     (l.workflow_details->>'workflowConfigKey') AS workflow_config_key,
                     ((l.workflow_details->'currentStageDetails')->>'stageKey') AS current_stage_key,
                     ((l.workflow_details->'currentStageDetails')->>'subStageKey') AS current_sub_stage_key,
                     ((l.workflow_details->'currentStageDetails')->>'assignedTo') AS assigned_to,
                     CASE
                         WHEN (l.workflow_details->'currentStageDetails')->>'assignedAt' IS NOT NULL
                         THEN to_timestamp((l.workflow_details->'currentStageDetails')->>'assignedAt', 'DD-MM-YYYY HH24:MI:SS')
                         ELSE NULL
                     END AS assigned_at,
                     CASE
                         WHEN (l.workflow_details->'currentStageDetails')->>'enteredAt' IS NOT NULL
                         THEN to_timestamp((l.workflow_details->'currentStageDetails')->>'enteredAt', 'DD-MM-YYYY HH24:MI:SS')
                         ELSE NULL
                     END AS entered_at,
                    sc.marketing_details->>'referredByCode' AS referred_by_code,
                    r.entity_type::text AS referred_by_type,
                    r.entity_identifier AS referred_by_identifier,
                    COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name,
                    COALESCE(
                        (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                        (jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                    ) AS referred_by_number
                 FROM n_lead l
                LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id
                LEFT JOIN n_referral_code_registry r ON r.referral_code = sc.marketing_details->>'referredByCode'
                LEFT JOIN n_advisor ref_adv ON ref_adv.identifier = r.entity_identifier AND r.entity_type::text = 'ADVISOR'
                LEFT JOIN n_user ref_adv_u ON ref_adv_u.username = ref_adv.username
                LEFT JOIN n_person ref_adv_p ON ref_adv_p.id = ref_adv_u.person_id
                LEFT JOIN n_staff ref_st ON ref_st.identifier = r.entity_identifier AND r.entity_type::text = 'STAFF'
                LEFT JOIN n_user ref_st_u ON ref_st_u.id = ref_st.user_id
                LEFT JOIN n_person ref_st_p ON ref_st_p.id = ref_st_u.person_id
                LEFT JOIN n_lead ref_lead ON ref_lead.lead_identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT'
                LEFT JOIN n_contact ref_lead_c ON ref_lead_c.id = (ref_lead.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person ref_lead_p ON ref_lead_p.id = ref_lead_c.person_id
                LEFT JOIN n_applicant ref_lead_app ON ref_lead_app.id = ref_lead.applicant
                LEFT JOIN n_person ref_lead_app_p ON ref_lead_app_p.id = ref_lead_app.person_id
                LEFT JOIN n_applicant ref_app_by_uuid ON ref_app_by_uuid.identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT'
                LEFT JOIN n_person ref_app_by_uuid_p ON ref_app_by_uuid_p.id = ref_app_by_uuid.person_id
                LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person primary_contact_person ON primary_contact.person_id = primary_contact_person.id
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
                   AND l.is_deleted = false
                """;

        try {
            LeadResponse leadResponse = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapLeadResponseFromResultSet(rs), leadIdentifier);
            if (leadResponse == null) {
                throw new LeadNotFoundException(leadIdentifier, messageSource);
            }
            // Enrich leadResponse with service data after query completes
            enrichLeadResponse(leadResponse);
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
                  AND l.is_deleted = false
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

    /**
     * Maps ResultSet to LeadResponse without making service calls.
     * Service calls are done separately in enrichLeadResponse() to avoid transaction issues.
     */
    private LeadResponse mapLeadResponseFromResultSet(ResultSet rs) throws SQLException {
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
                .disbursedAmount(rs.getBigDecimal("disbursed_amount"))
                .recentNote(rs.getString("recent_note"))
                .noteCreatedBy(rs.getString("recent_note_created_by"))
                .noteCreatedAt(getLocalDateTime(rs, "recent_note_created_at"))
                .lenderIdentifier(rs.getString("lender_identifier"))
                .lenderName(rs.getString("lender_name"))
                .lenderStatus(rs.getString("lender_status"))
                .lenderOfficeName(rs.getString("lender_office_name"))
                .preferredCallStartTime(getLocalTime(rs, "preferred_call_start_time"))
                .preferredCallEndTime(getLocalTime(rs, "preferred_call_end_time"))
                .noOfCampaignCalls(rs.getLong("no_of_campaign_calls"))
                .workflowConfigKey(rs.getString("workflow_config_key"))
                .currentStageKey(rs.getString("current_stage_key"))
                .currentSubStageKey(rs.getString("current_sub_stage_key"))
                .assignedTo(rs.getString("assigned_to"))
                .assignedAt(getLocalDateTime(rs, "assigned_at"))
                .enteredAt(getLocalDateTime(rs, "entered_at"))
                .holdFollowUpDate(getLocalDate(rs, "hold_follow_up_date"))
                .referredByCode(rs.getString("referred_by_code"))
                .referredByName(rs.getString("referred_by_name"))
                .referredByNumber(rs.getString("referred_by_number"));

        String referredByTypeStr = rs.getString("referred_by_type");
        if (referredByTypeStr != null) {
            try {
                builder.referredByType(EntityType.valueOf(referredByTypeStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
        String referredByIdentifierStr = rs.getString("referred_by_identifier");
        if (referredByIdentifierStr != null) {
            try {
                builder.referredByIdentifier(UUID.fromString(referredByIdentifierStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

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

        builder.leadCreatedAt(getLocalDateTime(rs, "leadCreatedAt"));

        LeadResponse leadResponse = builder.build();

        // Store keys for later enrichment (outside ResultSet processing)
        String priorityKey = rs.getString("priority_key");
        if (priorityKey != null) {
            leadResponse.setPriority(CodeValueResponse.builder().key(priorityKey).build());
        }

        String customerConvinceStatusKey = rs.getString("customer_convince_status_key");
        if (customerConvinceStatusKey != null) {
            leadResponse.setCustomerConvinceStatus(CodeValueResponse.builder().key(customerConvinceStatusKey).build());
        }

        String bureauRatingKey = rs.getString("bureau_rating_key");
        if (bureauRatingKey != null) {
            leadResponse.setBureauRating(CodeValueResponse.builder().key(bureauRatingKey).build());
        }

        String customerProfilesKey = rs.getString("customer_profiles_key");
        if (customerProfilesKey != null) {
            leadResponse.setCustomerProfiles(CodeValueResponse.builder().key(customerProfilesKey).build());
        }

        String monthlyFamilyIncomeKey = rs.getString("monthly_family_income_key");
        if (monthlyFamilyIncomeKey != null) {
            leadResponse.setMonthlyFamilyIncome(CodeValueResponse.builder().key(monthlyFamilyIncomeKey).build());
        }

        String lenderStageKey = rs.getString("lender_stage_key");
        if (lenderStageKey != null) {
            leadResponse.setLenderStage(CodeValueResponse.builder().key(lenderStageKey).build());
        }

        return leadResponse;
    }

    /**
     * Enriches LeadResponse with data from service calls.
     * Called after the query completes to avoid making database calls while ResultSet is open.
     */
    private void enrichLeadResponse(LeadResponse leadResponse) {
        try {
            // Enrich reason code
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
                    if (codeValueResponse != null) {
                        leadResponse.setReason(codeValueResponse.getValue());
                    }
                }
                if (leadResponse.getSubStatus() == LeadSubStatus.DROPOFF) {
                    CodeValueResponse codeValueResponse = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                            leadResponse.getReasonCode(), SystemControlledMasterCodes.LEAD_DROPOFF_REASON_MASTER);
                    if (codeValueResponse != null) {
                        leadResponse.setReason(codeValueResponse.getValue());
                    }
                }
            }

            // Enrich product name
            if (leadResponse.getProductCode() != null) {
                leadResponse.setProductName(productReadService.getProductByCode(leadResponse.getProductCode()).getName());
            }

            // Enrich priority
            if (leadResponse.getPriority() != null && leadResponse.getPriority().getKey() != null) {
                CodeValueResponse priority = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        leadResponse.getPriority().getKey(),
                        SystemControlledMasterCodes.LEAD_PRIORITY_MASTER
                );
                leadResponse.setPriority(priority);
            }

            if (leadResponse.getCustomerConvinceStatus() != null && leadResponse.getCustomerConvinceStatus().getKey() != null) {
                CodeValueResponse customerConvinceStatus = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        leadResponse.getCustomerConvinceStatus().getKey(),
                        SystemControlledMasterCodes.LEAD_CUSTOMER_CONVINCE_STATUS_MASTER
                );
                leadResponse.setCustomerConvinceStatus(customerConvinceStatus);
            }

            // Enrich bureau rating
            if (leadResponse.getBureauRating() != null && leadResponse.getBureauRating().getKey() != null) {
                CodeValueResponse bureauRating = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        leadResponse.getBureauRating().getKey(),
                        SystemControlledMasterCodes.LEAD_BUREAU_RATING_MASTER
                );
                leadResponse.setBureauRating(bureauRating);
            }

            // Enrich customer profiles
            if (leadResponse.getCustomerProfiles() != null && leadResponse.getCustomerProfiles().getKey() != null) {
                CodeValueResponse customerProfile = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        leadResponse.getCustomerProfiles().getKey(),
                        SystemControlledMasterCodes.LEAD_CUSTOMER_PROFILE_MASTER
                );
                leadResponse.setCustomerProfiles(customerProfile);
            }

            // Enrich monthly family income
            if (leadResponse.getMonthlyFamilyIncome() != null && leadResponse.getMonthlyFamilyIncome().getKey() != null) {
                CodeValueResponse monthlyIncome = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        leadResponse.getMonthlyFamilyIncome().getKey(),
                        SystemControlledMasterCodes.LEAD_MONTHLY_INCOME_MASTER
                );
                leadResponse.setMonthlyFamilyIncome(monthlyIncome);
            }

            // Enrich lender stage
            if (leadResponse.getLenderStage() != null && leadResponse.getLenderStage().getKey() != null) {
                CodeValueResponse lenderStage = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                        leadResponse.getLenderStage().getKey(),
                        SystemControlledMasterCodes.LENDER_STAGE_MASTER
                );
                leadResponse.setLenderStage(lenderStage);
            }

            // Enrich current sub-stage name
            if (leadResponse.getCurrentSubStageKey() != null && !leadResponse.getCurrentSubStageKey().trim().isEmpty()) {
                try {
                    CodeValueResponse subStage = codeValueMasterService.getByKey(leadResponse.getCurrentSubStageKey());
                    if (subStage != null && subStage.getValue() != null) {
                        leadResponse.setCurrentSubStageName(subStage.getValue());
                    }
                } catch (Exception e) {
                    // Ignore if sub-stage not found - leave name as null
                }
            }
        } catch (DataAccessException e) {
            // Log error but don't fail the entire operation
            // The lead response will be returned with partial data
            throw new RuntimeException("Failed to enrich lead response with service data", e);
        }
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
        String phoneJson = buildPhoneNumberJsonb(mobileNumber);

        long t0 = System.nanoTime();
        // Get current staff office and code for hierarchy filtering
        String currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
        log.debug(LOG_SEARCH_PHONE_STAFF_MS, (System.nanoTime() - t0) / 1_000_000);

        long t1 = System.nanoTime();
        String currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();
        log.debug(LOG_SEARCH_PHONE_OFFICE_MS, (System.nanoTime() - t1) / 1_000_000);

        // Contact→lead first (subquery fence), then office filter; prevents planner from starting from n_office → n_lead
        String countSql = """
            WITH person_with_phone AS MATERIALIZED (SELECT id FROM n_person WHERE mobile_numbers @> ?::jsonb),
                 contact_ids_with_phone AS MATERIALIZED (SELECT c.id FROM n_contact c INNER JOIN person_with_phone p ON c.person_id = p.id),
                 lead_match AS MATERIALIZED (
                    SELECT l.id, l.office_key
                    FROM contact_ids_with_phone cip
                    JOIN n_lead l ON l.contacts @> jsonb_build_array(cip.id)
                    WHERE l.is_deleted = false
                 )
            SELECT COUNT(DISTINCT lm.id)
            FROM lead_match lm
            LEFT JOIN n_office o ON o.key = lm.office_key
            WHERE o.code LIKE ?
            """;

        String dataSql = """
            WITH person_with_phone AS MATERIALIZED (SELECT id FROM n_person WHERE mobile_numbers @> ?::jsonb),
                 contact_ids_with_phone AS MATERIALIZED (SELECT c.id FROM n_contact c INNER JOIN person_with_phone p ON c.person_id = p.id),
                 lead_match AS MATERIALIZED (
                    SELECT cip.id AS cip_id,
                           l.lead_identifier, l.requested_amount, l.other_details, l.product_code,
                           l.status, l.substatus, l.created_at, l.updated_at, l.office_key
                    FROM contact_ids_with_phone cip
                    JOIN n_lead l ON l.contacts @> jsonb_build_array(cip.id)
                    WHERE l.is_deleted = false
                 )
            SELECT DISTINCT
                lm.lead_identifier,
                lm.requested_amount,
                lm.other_details->>'noOfCampaignCalls' as no_of_campaign_calls,
                p.name->>'default' as product_name,
                primary_contact.identifier as primary_person_identifier,
                primary_person.display_name as primary_person_name,
                (jsonb_path_query_first(COALESCE(primary_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_person_number,
                matching_contact.identifier as contact_person_identifier,
                matching_person.display_name as contact_person_name,
                (jsonb_path_query_first(COALESCE(matching_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS contact_person_number,
                lm.status,
                lm.substatus,
                lm.created_at as lead_created_at,
                lm.updated_at as last_activity_date
            FROM lead_match lm
            LEFT JOIN n_office o ON o.key = lm.office_key
            JOIN n_contact matching_contact ON matching_contact.id = lm.cip_id
            JOIN n_person matching_person ON matching_person.id = matching_contact.person_id
            LEFT JOIN n_contact primary_contact ON primary_contact.id = (lm.other_details->>'primaryContactId')::bigint
            LEFT JOIN n_person primary_person ON primary_contact.person_id = primary_person.id
            LEFT JOIN n_product p ON p.code = lm.product_code
            WHERE o.code LIKE ?
            ORDER BY lm.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            String officePattern = currentUserOfficeCode + "%";

            long t2 = System.nanoTime();
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, phoneJson, officePattern);
            long total = totalCount != null ? totalCount : 0L;
            log.debug(LOG_SEARCH_PHONE_COUNT_MS, (System.nanoTime() - t2) / 1_000_000);

            long t3 = System.nanoTime();
            List<LeadSearchResponse> results = jdbcTemplate.query(
                    dataSql,
                    new LeadSearchRowMapper(),
                    phoneJson,
                    officePattern,
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset()
            );
            log.debug(LOG_SEARCH_PHONE_DATA_MS, (System.nanoTime() - t3) / 1_000_000);

            log.debug(LOG_SEARCH_PHONE_TOTAL_MS, (System.nanoTime() - t0) / 1_000_000);

            PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, total);
            return new PaginatedResponse<>(results, paginationInfo);
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to search leads by phone number", e);
        }
    }

    /**
     * Returns whether at least one lead exists for the given mobile number.
     * Does not apply office hierarchy.( duplicate check).
     */
    public boolean existsLeadWithMobileNumber(String mobileNumber) {
        if (!StringUtils.hasText(mobileNumber)) {
            return false;
        }
        String trimmed = mobileNumber.trim();
        String phoneJson = buildPhoneNumberJsonb(trimmed);
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM n_person p
                WHERE p.mobile_numbers @> ?::jsonb
                AND EXISTS (
                    SELECT 1 FROM n_contact c
                    INNER JOIN n_lead l ON l.contacts @> jsonb_build_array(c.id)
                    WHERE c.person_id = p.id
                      AND l.is_deleted = false
                )
            ) AS exists_flag
            """;
        try {
            Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, phoneJson);
            return Boolean.TRUE.equals(result);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to check existence of lead by mobile number", e);
        }
    }

    /**
     * Returns a reusable lead for the given mobile number without office hierarchy filtering.
     * Preference order:
     * 1. Any ACTIVE lead
     * 2. The most recent REJECTED lead, only if all matching leads are rejected
     */
    public Optional<LeadBasicResponse> findReusableLeadByPhoneNumber(String mobileNumber) {
        if (!StringUtils.hasText(mobileNumber)) {
            return Optional.empty();
        }

        String trimmed = mobileNumber.trim();
        String phoneJson = buildPhoneNumberJsonb(trimmed);
        String sql = """
            WITH person_with_phone AS MATERIALIZED (
                SELECT id
                FROM n_person
                WHERE mobile_numbers @> ?::jsonb
            ),
            contact_ids_with_phone AS MATERIALIZED (
                SELECT c.id
                FROM n_contact c
                INNER JOIN person_with_phone p ON c.person_id = p.id
            )
            SELECT DISTINCT
                l.id,
                l.lead_identifier,
                l.status,
                l.substatus,
                l.product_code,
                l.created_at
            FROM contact_ids_with_phone cip
            INNER JOIN n_lead l ON l.contacts @> jsonb_build_array(cip.id)
            WHERE l.is_deleted = false
            ORDER BY l.created_at DESC
            """;

        try {
            List<LeadBasicResponse> leads = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        LeadBasicResponse.LeadBasicResponseBuilder builder = LeadBasicResponse.builder()
                                .id(rs.getLong("id"))
                                .leadIdentifier(UUID.fromString(rs.getString("lead_identifier")))
                                .productCode(rs.getString("product_code"))
                                .createdAt(getLocalDateTime(rs, "created_at"));

                        String status = rs.getString("status");
                        if (status != null) {
                            try {
                                builder.status(LeadStatus.valueOf(status));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }

                        String subStatus = rs.getString("substatus");
                        if (subStatus != null) {
                            try {
                                builder.substatus(LeadSubStatus.valueOf(subStatus));
                            } catch (IllegalArgumentException ignored) {
                            }
                        }

                        return builder.build();
                    },
                    phoneJson
            );

            Optional<LeadBasicResponse> activeLead = leads.stream()
                    .filter(lead -> LeadStatus.ACTIVE.equals(lead.getStatus()))
                    .findFirst();
            if (activeLead.isPresent()) {
                return activeLead;
            }

            boolean allRejected = !leads.isEmpty() && leads.stream()
                    .allMatch(lead -> LeadStatus.REJECTED.equals(lead.getStatus()));
            if (allRejected) {
                return Optional.of(leads.get(0));
            }

            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find reusable lead by mobile number", e);
        }
    }

    /**
     * Admin search: returns all leads (deleted + non-deleted) matching the phone number,
     * without office hierarchy filtering.
     */
    public PaginatedResponse<AdminLeadSearchResponse> adminSearchLeadsByPhoneNumber(
            PaginationRequest paginationRequest, AdminLeadSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getMobileNumber())) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }

        String mobileNumber = request.getMobileNumber().trim();
        String phoneJson = buildPhoneNumberJsonb(mobileNumber);

        String countSql = """
            WITH person_with_phone AS MATERIALIZED (SELECT id FROM n_person WHERE mobile_numbers @> :phoneJson::jsonb),
                 contact_ids_with_phone AS MATERIALIZED (SELECT c.id FROM n_contact c INNER JOIN person_with_phone p ON c.person_id = p.id),
                 lead_match AS MATERIALIZED (
                    SELECT l.id
                    FROM contact_ids_with_phone cip
                    JOIN n_lead l ON l.contacts @> jsonb_build_array(cip.id)
                 )
            SELECT COUNT(DISTINCT lm.id)
            FROM lead_match lm
            """;

        String dataSql = """
            WITH person_with_phone AS MATERIALIZED (SELECT id FROM n_person WHERE mobile_numbers @> :phoneJson::jsonb),
                 contact_ids_with_phone AS MATERIALIZED (SELECT c.id FROM n_contact c INNER JOIN person_with_phone p ON c.person_id = p.id),
                 lead_match AS MATERIALIZED (
                    SELECT cip.id AS cip_id,
                           l.lead_identifier, l.requested_amount, l.other_details, l.product_code,
                           l.status, l.substatus, l.created_at, l.updated_at, l.is_deleted
                    FROM contact_ids_with_phone cip
                    JOIN n_lead l ON l.contacts @> jsonb_build_array(cip.id)
                 )
            SELECT DISTINCT
                lm.lead_identifier,
                lm.requested_amount,
                p.name->>'default' as product_name,
                primary_person.display_name as primary_person_name,
                (jsonb_path_query_first(COALESCE(primary_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_person_number,
                lm.status,
                lm.substatus,
                lm.created_at as lead_created_at,
                lm.updated_at as last_activity_date,
                lm.is_deleted
            FROM lead_match lm
            LEFT JOIN n_contact primary_contact ON primary_contact.id = (lm.other_details->>'primaryContactId')::bigint
            LEFT JOIN n_person primary_person ON primary_contact.person_id = primary_person.id
            LEFT JOIN n_product p ON p.code = lm.product_code
            ORDER BY lm.updated_at DESC
            LIMIT :limit OFFSET :offset
            """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("phoneJson", phoneJson);

            Long totalCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
            long total = totalCount != null ? totalCount : 0L;

            params.addValue("limit", paginationRequest.getLimit());
            params.addValue("offset", paginationRequest.getOffset());

            List<AdminLeadSearchResponse> results = namedParameterJdbcTemplate.query(
                    dataSql, params, new AdminLeadSearchRowMapper());

            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            LeadOperationException exception = LeadExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Admin: returns only soft-deleted leads, paginated.
     */
    public PaginatedResponse<AdminLeadSearchResponse> findDeletedLeads(PaginationRequest paginationRequest) {
        String countSql = "SELECT COUNT(*) FROM n_lead l WHERE l.is_deleted = true";

        String dataSql = """
            SELECT DISTINCT
                l.lead_identifier,
                l.requested_amount,
                p.name->>'default' as product_name,
                primary_person.display_name as primary_person_name,
                (jsonb_path_query_first(COALESCE(primary_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_person_number,
                l.status,
                l.substatus,
                l.created_at as lead_created_at,
                l.updated_at as last_activity_date,
                l.is_deleted
            FROM n_lead l
            LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
            LEFT JOIN n_person primary_person ON primary_contact.person_id = primary_person.id
            LEFT JOIN n_product p ON p.code = l.product_code
            WHERE l.is_deleted = true
            ORDER BY l.updated_at DESC
            LIMIT :limit OFFSET :offset
            """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource();

            Long totalCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
            long total = totalCount != null ? totalCount : 0L;

            params.addValue("limit", paginationRequest.getLimit());
            params.addValue("offset", paginationRequest.getOffset());

            List<AdminLeadSearchResponse> results = namedParameterJdbcTemplate.query(
                    dataSql, params, new AdminLeadSearchRowMapper());

            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (DataAccessException e) {
            LeadOperationException exception = LeadExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    private static String buildPhoneNumberJsonb(String mobileNumber) {
        String escaped = mobileNumber.replace("\\", "\\\\").replace("\"", "\\\"");
        return "[{\"number\":\"" + escaped + "\"}]";
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

            Long noOfCampaignCalls = rs.getLong("no_of_campaign_calls");
            builder.numberOfCampaignCalls(noOfCampaignCalls);

            return builder.build();
        }
    }

    /**
     * Finds the lead identifier for the lead that has a contact whose cb_enquiry_id contains the given enquiry ID.
     * Used when uploading CB Excel report to the correct lead after CB report is stored.
     *
     * @param enquiryId the credit bureau enquiry ID
     * @return Optional of lead identifier if found, empty otherwise
     */
    public Optional<UUID> findLeadIdentifierByCbEnquiryId(Long enquiryId) {
        String sql = """
                SELECT l.lead_identifier
                FROM n_lead l
                CROSS JOIN LATERAL jsonb_array_elements_text(COALESCE(l.contacts, '[]'::jsonb)) AS cid
                JOIN n_contact c ON c.id = (cid::bigint)
                WHERE c.cb_enquiry_id IS NOT NULL
                  AND l.is_deleted = false
                  AND EXISTS (
                      SELECT 1 FROM jsonb_array_elements_text(c.cb_enquiry_id) AS eid
                      WHERE eid::bigint = ?
                  )
                LIMIT 1
                """;
        try {
            List<UUID> results = jdbcTemplate.query(sql, (rs, rowNum) -> (UUID) rs.getObject("lead_identifier"), enquiryId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.warn("Failed to find lead by cb_enquiry_id for enquiry ID: {}", enquiryId, e);
            return Optional.empty();
        }
    }

    public List<LeadWorkflowDetailsDto> findLeadsByPersonIdsAndStatusesAndSubstatuses(List<Long> personIds,
            List<LeadStatus> statuses, List<LeadSubStatus> substatuses) {
        try {
            List<String> statusesString = statuses.stream().map(LeadStatus::name).toList();
            List<String> substatusesString = substatuses.stream().map(LeadSubStatus::name).toList();

            String sql = getLeadWorkflowDetailsQuery();

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("personIds", personIds.toArray(new Long[0]), Types.ARRAY);
            params.addValue("statuses", statusesString.toArray(new String[0]), Types.ARRAY);
            params.addValue("substatuses", substatusesString.toArray(new String[0]), Types.ARRAY);

            return namedParameterJdbcTemplate.query(sql, params, new LeadWorkflowDetailsRowMapper());
        } catch (DataAccessException e) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }



    private String getLeadWorkflowDetailsQuery() {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append("l.id AS lead_id, ");
        sql.append("l.lead_identifier AS lead_identifier, ");
        sql.append("l.workflow_details->>'workflowConfigKey' AS workflow_config_key, ");
        sql.append("l.workflow_details->'currentStageDetails'->>'stageKey' AS current_stage_key, ");
        sql.append("l.workflow_details->'currentStageDetails'->>'subStageKey' AS current_sub_stage_key, ");
        sql.append("l.workflow_details->'currentStageDetails'->>'assignedTo' AS assigned_to ");

        sql.append("FROM n_lead l ");

        sql.append("WHERE ( ");
        sql.append("EXISTS (SELECT 1 FROM n_contact c WHERE c.id = l.applicant AND c.person_id = ANY (:personIds::bigint[])) ");

        sql.append("OR EXISTS ( ");
        sql.append("SELECT 1 FROM jsonb_array_elements_text(l.contacts) AS elem ");
        sql.append("JOIN n_contact c ON c.id = (elem)::bigint ");
        sql.append("WHERE c.person_id = ANY (:personIds::bigint[]) ");
        sql.append(") ");

        sql.append("OR EXISTS ( ");
        sql.append("SELECT 1 FROM jsonb_array_elements_text(l.co_applicants) AS elem ");
        sql.append("JOIN n_contact c ON c.id = (elem)::bigint ");
        sql.append("WHERE c.person_id = ANY (:personIds::bigint[]) ");
        sql.append(") ");
        sql.append(") ");

        sql.append("AND l.status = ANY (:statuses::text[]) ");
        sql.append("AND (l.substatus IS NULL OR l.substatus = ANY (:substatuses::text[])) ");
        sql.append("AND l.is_deleted = false ");

        sql.append("ORDER BY l.created_at DESC ");

        return sql.toString();
    }

    public LeadBasicResponse findLeadByReferralTrackingCodeWithException(String referralTrackingCode) {
        try {
            String sql = getLeadByReferralTrackingCodeQuery();
            return jdbcTemplate.queryForObject(sql, new LeadBasicResponseMapper(), referralTrackingCode);
        } catch (EmptyResultDataAccessException e) {
            throw LeadExceptionFactory.leadNotFoundByReferralTrackingCode(referralTrackingCode, messageSource);
        } catch (DataAccessException e) {
            throw LeadExceptionFactory.leadNotFoundByReferralTrackingCode(referralTrackingCode, messageSource);
        }
    }

    private String getLeadByReferralTrackingCodeQuery() {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append("l.id AS id, ");
        sql.append("l.lead_identifier AS lead_identifier, ");
        sql.append("primary_contact.display_name AS primary_contact_name, ");
        sql.append("(SELECT m->>'number' FROM jsonb_array_elements(COALESCE(primary_contact.mobile_numbers, '[]'::jsonb)) m WHERE (m->>'isPrimary')::boolean = true LIMIT 1) AS primary_contact_phone, ");
        sql.append("l.requested_amount AS requested_amount, ");
        sql.append("l.workflow_details->'currentStageDetails'->>'stageKey' AS current_stage, ");
        sql.append("l.status::text AS status, ");
        sql.append("l.substatus::text AS substatus, ");
        sql.append("l.created_at AS created_at, ");
        sql.append("o.name AS office, ");
        sql.append("l.product_code AS product_code, ");
        sql.append("sc.marketing_details->>'referredByCode' AS referred_by_code, ");
        sql.append("r.entity_type::text AS referred_by_type, ");
        sql.append("r.entity_identifier AS referred_by_identifier, ");
        sql.append("COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name, ");
        sql.append("COALESCE(");
        sql.append("(jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') ");
        sql.append(") AS referred_by_number ");
        sql.append("FROM n_lead l ");
        sql.append("LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id ");
        sql.append("LEFT JOIN n_referral_code_registry r ON r.referral_code = sc.marketing_details->>'referredByCode' ");
        sql.append("LEFT JOIN n_advisor ref_adv ON ref_adv.identifier = r.entity_identifier AND r.entity_type::text = 'ADVISOR' ");
        sql.append("LEFT JOIN n_user ref_adv_u ON ref_adv_u.username = ref_adv.username ");
        sql.append("LEFT JOIN n_person ref_adv_p ON ref_adv_p.id = ref_adv_u.person_id ");
        sql.append("LEFT JOIN n_staff ref_st ON ref_st.identifier = r.entity_identifier AND r.entity_type::text = 'STAFF' ");
        sql.append("LEFT JOIN n_user ref_st_u ON ref_st_u.id = ref_st.user_id ");
        sql.append("LEFT JOIN n_person ref_st_p ON ref_st_p.id = ref_st_u.person_id ");
        sql.append("LEFT JOIN n_lead ref_lead ON ref_lead.lead_identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT' ");
        sql.append("LEFT JOIN n_contact ref_lead_c ON ref_lead_c.id = (ref_lead.other_details->>'primaryContactId')::bigint ");
        sql.append("LEFT JOIN n_person ref_lead_p ON ref_lead_p.id = ref_lead_c.person_id ");
        sql.append("LEFT JOIN n_applicant ref_lead_app ON ref_lead_app.id = ref_lead.applicant ");
        sql.append("LEFT JOIN n_person ref_lead_app_p ON ref_lead_app_p.id = ref_lead_app.person_id ");
        sql.append("LEFT JOIN n_applicant ref_app_by_uuid ON ref_app_by_uuid.identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT' ");
        sql.append("LEFT JOIN n_person ref_app_by_uuid_p ON ref_app_by_uuid_p.id = ref_app_by_uuid.person_id ");
        sql.append("LEFT JOIN n_contact primary_contact_person ON primary_contact_person.id = (l.other_details->>'primaryContactId')::bigint ");
        sql.append("LEFT JOIN n_person primary_contact ON primary_contact.id = primary_contact_person.person_id ");
        sql.append("LEFT JOIN n_office o ON o.key = l.office_key ");
        sql.append("WHERE l.referral_tracking_code = :referralTrackingCode ");
        sql.append("AND l.is_deleted = false ");

        return sql.toString();
    }

    public PaginatedResponse<LeadBasicResponse> findLeadsByEntity(
            EntityType entityType,
            UUID entityIdentifier,
            PaginationRequest paginationRequest) {

        try {

            String sortBy = ALLOWED_SORT_COLUMNS_FOR_LEAD_BASIC_RESPONSE
                    .contains(paginationRequest.getSortBy())
                            ? paginationRequest.getSortBy()
                            : "created_at";

            String sortDirection = ALLOWED_SORT_DIRECTIONS
                    .contains(paginationRequest.getSortDirection())
                            ? paginationRequest.getSortDirection()
                            : "DESC";

            String sql = getLeadsByEntityQuery(sortBy, sortDirection);

            List<LeadBasicResponse> results = jdbcTemplate.query(
                    sql,
                    new LeadBasicResponseMapper(),
                    entityType.name(),
                    entityIdentifier,
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset());

            return new PaginatedResponse<>(
                    results,
                    buildPaginationInfo(paginationRequest, results.size()));

        } catch (DataAccessException e) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public PaginatedResponse<LeadBasicResponse> findLeadsByReferralCode(
            String referralCode, PaginationRequest paginationRequest) {
        if (!StringUtils.hasText(referralCode)) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }
        String sortBy = ALLOWED_SORT_COLUMNS_FOR_LEAD_BASIC_RESPONSE.contains(paginationRequest.getSortBy())
                ? paginationRequest.getSortBy() : "created_at";
        String sortDirection = ALLOWED_SORT_DIRECTIONS.contains(paginationRequest.getSortDirection())
                ? paginationRequest.getSortDirection() : "DESC";
        String countSql = """
            SELECT COUNT(*)
            FROM n_sourcing_channel_details sc
            JOIN n_lead l ON l.sourcing_channel_id = sc.id
            WHERE sc.marketing_details->>'referredByCode' = ?
              AND l.is_deleted = false
            """;
        String dataSql = """
            WITH leads AS MATERIALIZED (
              SELECT l.id, l.lead_identifier, l.other_details, l.created_at, l.requested_amount,
                     l.workflow_details, l.status, l.substatus, l.product_code, l.office_key,
                     (sc.marketing_details->>'referredByCode') AS ref_code
              FROM n_sourcing_channel_details sc
              JOIN n_lead l ON l.sourcing_channel_id = sc.id
              WHERE sc.marketing_details->>'referredByCode' = ?
                AND l.is_deleted = false
            )
            SELECT leads.id AS id,
                   leads.lead_identifier AS lead_identifier,
                   primary_lat.name AS primary_contact_name,
                   primary_lat.num AS primary_contact_phone,
                   leads.requested_amount AS requested_amount,
                   leads.workflow_details->'currentStageDetails'->>'stageKey' AS current_stage,
                   leads.status::text AS status,
                   leads.substatus::text AS substatus,
                   leads.created_at AS created_at,
                   o.name AS office,
                   leads.product_code AS product_code,
                   leads.ref_code AS referred_by_code,
                   r.entity_type::text AS referred_by_type,
                   r.entity_identifier AS referred_by_identifier,
                   COALESCE(ref_adv_lat.name, ref_st_lat.name, ref_lead_lat.name, ref_lead_app_lat.name, ref_app_uuid_lat.name) AS referred_by_name,
                   COALESCE(ref_adv_lat.num, ref_st_lat.num, ref_lead_lat.num, ref_lead_app_lat.num, ref_app_uuid_lat.num) AS referred_by_number
            FROM leads
            LEFT JOIN n_referral_code_registry r ON r.referral_code = leads.ref_code
            LEFT JOIN LATERAL (
              SELECT ref_adv_p.display_name AS name,
                     (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS num
              FROM n_advisor ref_adv
              JOIN n_user ref_adv_u ON ref_adv_u.username = ref_adv.username
              JOIN n_person ref_adv_p ON ref_adv_p.id = ref_adv_u.person_id
              WHERE ref_adv.identifier = r.entity_identifier AND (r.entity_type)::text = 'ADVISOR'
              LIMIT 1
            ) ref_adv_lat ON true
            LEFT JOIN LATERAL (
              SELECT ref_st_p.display_name AS name,
                     (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS num
              FROM n_staff ref_st
              JOIN n_user ref_st_u ON ref_st_u.id = ref_st.user_id
              JOIN n_person ref_st_p ON ref_st_p.id = ref_st_u.person_id
              WHERE ref_st.identifier = r.entity_identifier AND (r.entity_type)::text = 'STAFF'
              LIMIT 1
            ) ref_st_lat ON true
            LEFT JOIN LATERAL (
              SELECT ref_lead_p.display_name AS name,
                     (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS num
              FROM n_lead ref_lead
              LEFT JOIN n_contact ref_lead_c ON ref_lead_c.id = (ref_lead.other_details->>'primaryContactId')::bigint
              LEFT JOIN n_person ref_lead_p ON ref_lead_p.id = ref_lead_c.person_id
              WHERE ref_lead.lead_identifier = r.entity_identifier AND (r.entity_type)::text = 'APPLICANT'
              LIMIT 1
            ) ref_lead_lat ON true
            LEFT JOIN LATERAL (
              SELECT ref_lead_app_p.display_name AS name,
                     (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS num
              FROM n_lead ref_lead2
              JOIN n_applicant ref_lead_app ON ref_lead_app.id = ref_lead2.applicant
              JOIN n_person ref_lead_app_p ON ref_lead_app_p.id = ref_lead_app.person_id
              WHERE ref_lead2.lead_identifier = r.entity_identifier AND (r.entity_type)::text = 'APPLICANT'
              LIMIT 1
            ) ref_lead_app_lat ON true
            LEFT JOIN LATERAL (
              SELECT ref_app_p.display_name AS name,
                     (jsonb_path_query_first(COALESCE(ref_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS num
              FROM n_applicant ref_app
              JOIN n_person ref_app_p ON ref_app_p.id = ref_app.person_id
              WHERE ref_app.identifier = r.entity_identifier AND (r.entity_type)::text = 'APPLICANT'
              LIMIT 1
            ) ref_app_uuid_lat ON true
            LEFT JOIN LATERAL (
              SELECT prim_p.display_name AS name,
                     (SELECT m->>'number' FROM jsonb_array_elements(COALESCE(prim_p.mobile_numbers, '[]'::jsonb)) m
                      WHERE (m->>'isPrimary')::boolean = true LIMIT 1) AS num
              FROM n_contact prim_c
              JOIN n_person prim_p ON prim_p.id = prim_c.person_id
              WHERE prim_c.id = (leads.other_details->>'primaryContactId')::bigint
              LIMIT 1
            ) primary_lat ON true
            LEFT JOIN n_office o ON o.key = leads.office_key
            ORDER BY leads.""" + sortBy + " " + sortDirection + """
             LIMIT ? OFFSET ?
            """;
        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, referralCode.trim());
            long total = totalCount != null ? totalCount : 0L;
            List<LeadBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new LeadBasicResponseMapper(),
                    referralCode.trim(),
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset());
            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (DataAccessException e) {
            throw LeadExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    private String getLeadsByEntityQuery(String sortBy, String sortDirection) {

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ");
        sql.append("l.id AS id, ");
        sql.append("l.lead_identifier AS lead_identifier, ");
        sql.append("p.display_name AS primary_contact_name, ");
        sql.append("(SELECT m->>'number' FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) m WHERE (m->>'isPrimary')::boolean = true LIMIT 1) AS primary_contact_phone, ");
        sql.append("l.requested_amount AS requested_amount, ");
        sql.append("l.workflow_details->'currentStageDetails'->>'stageKey' AS current_stage, ");
        sql.append("l.status::text AS status, ");
        sql.append("l.substatus::text AS substatus, ");
        sql.append("l.created_at AS created_at, ");
        sql.append("o.name AS office, ");
        sql.append("l.product_code AS product_code, ");
        sql.append("sc.marketing_details->>'referredByCode' AS referred_by_code, ");
        sql.append("r.entity_type::text AS referred_by_type, ");
        sql.append("r.entity_identifier AS referred_by_identifier, ");
        sql.append("COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name, ");
        sql.append("COALESCE(");
        sql.append("(jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'), ");
        sql.append("(jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') ");
        sql.append(") AS referred_by_number ");
        sql.append("FROM n_lead l ");
        sql.append("LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id ");
        sql.append("LEFT JOIN n_referral_code_registry r ON r.referral_code = sc.marketing_details->>'referredByCode' ");
        sql.append("LEFT JOIN n_advisor ref_adv ON ref_adv.identifier = r.entity_identifier AND r.entity_type::text = 'ADVISOR' ");
        sql.append("LEFT JOIN n_user ref_adv_u ON ref_adv_u.username = ref_adv.username ");
        sql.append("LEFT JOIN n_person ref_adv_p ON ref_adv_p.id = ref_adv_u.person_id ");
        sql.append("LEFT JOIN n_staff ref_st ON ref_st.identifier = r.entity_identifier AND r.entity_type::text = 'STAFF' ");
        sql.append("LEFT JOIN n_user ref_st_u ON ref_st_u.id = ref_st.user_id ");
        sql.append("LEFT JOIN n_person ref_st_p ON ref_st_p.id = ref_st_u.person_id ");
        sql.append("LEFT JOIN n_lead ref_lead ON ref_lead.lead_identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT' ");
        sql.append("LEFT JOIN n_contact ref_lead_c ON ref_lead_c.id = (ref_lead.other_details->>'primaryContactId')::bigint ");
        sql.append("LEFT JOIN n_person ref_lead_p ON ref_lead_p.id = ref_lead_c.person_id ");
        sql.append("LEFT JOIN n_applicant ref_lead_app ON ref_lead_app.id = ref_lead.applicant ");
        sql.append("LEFT JOIN n_person ref_lead_app_p ON ref_lead_app_p.id = ref_lead_app.person_id ");
        sql.append("LEFT JOIN n_applicant ref_app_by_uuid ON ref_app_by_uuid.identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT' ");
        sql.append("LEFT JOIN n_person ref_app_by_uuid_p ON ref_app_by_uuid_p.id = ref_app_by_uuid.person_id ");
        sql.append("LEFT JOIN n_contact primary_contact_person ON primary_contact_person.id = (l.other_details->>'primaryContactId')::bigint ");
        sql.append("LEFT JOIN n_person p ON p.id = primary_contact_person.person_id ");
        sql.append("LEFT JOIN n_office o ON o.key = l.office_key ");
        sql.append("WHERE l.entity_type = ? ");
        sql.append("AND l.entity_identifier = ? ");
        sql.append("AND l.is_deleted = false ");
        sql.append("ORDER BY l.").append(sortBy).append(" ").append(sortDirection).append(" ");
        sql.append("LIMIT ? OFFSET ? ");

        return sql.toString();
    }
    

    private static final Set<String> ALLOWED_SORT_COLUMNS_FOR_LEAD_BASIC_RESPONSE = Set.of(
        "created_at");
    private static final Set<String> ALLOWED_SORT_DIRECTIONS = Set.of("ASC", "DESC");

    public Long findPrimaryPersonIdForLead(UUID leadIdentifier) {
        String sql = """
            SELECT c.person_id
            FROM n_lead l
            JOIN n_contact c ON c.id = (l.other_details->>'primaryContactId')::bigint
            WHERE l.lead_identifier = :leadIdentifier
            """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("leadIdentifier", leadIdentifier);
            return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static class AdminLeadSearchRowMapper implements RowMapper<AdminLeadSearchResponse> {
        @Override
        public AdminLeadSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdminLeadSearchResponse.AdminLeadSearchResponseBuilder builder = AdminLeadSearchResponse.builder();

            String leadIdentifierStr = rs.getString("lead_identifier");
            if (leadIdentifierStr != null) {
                builder.leadIdentifier(UUID.fromString(leadIdentifierStr));
            }

            builder.requestedAmount(rs.getBigDecimal("requested_amount"));
            builder.productName(rs.getString("product_name"));
            builder.primaryPersonName(rs.getString("primary_person_name"));
            builder.primaryPersonNumber(rs.getString("primary_person_number"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(LeadStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                }
            }

            String subStatus = rs.getString("substatus");
            if (subStatus != null) {
                try {
                    builder.subStatus(LeadSubStatus.valueOf(subStatus));
                } catch (IllegalArgumentException ignored) {
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

            builder.deleted(rs.getBoolean("is_deleted"));

            return builder.build();
        }
    }
}
