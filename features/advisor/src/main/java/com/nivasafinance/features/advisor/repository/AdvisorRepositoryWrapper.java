package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.advisor.dto.AdminAdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdminAdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadResponse;
import com.nivasafinance.features.advisor.dto.self.SelfPaymentDetails;
import com.nivasafinance.features.advisor.dto.self.SelfPayoutDetailResponse;
import com.nivasafinance.features.advisor.dto.self.SelfPayoutResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory;
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException;
import com.nivasafinance.features.advisor.exception.AdvisorOperationException;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.service.StaffReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdvisorRepositoryWrapper {

    private final AdvisorRepository advisorRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

    @Autowired
    public AdvisorRepositoryWrapper(AdvisorRepository advisorRepository, MessageSource messageSource,
                                     JdbcTemplate jdbcTemplate, StaffReadService staffReadService,
                                     OfficeReadService officeReadService) {
        this.advisorRepository = advisorRepository;
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
        this.staffReadService = staffReadService;
        this.officeReadService = officeReadService;
    }

    public Advisor saveWithException(Advisor advisor) {
        try {
            return advisorRepository.save(advisor);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Advisor findByIdWithException(UUID id) {
        try {
            return advisorRepository.findById(id).orElseThrow(() ->
                    AdvisorExceptionFactory.notFound(id, messageSource)
            );
        } catch (AdvisorNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Advisor findByIdentifierWithException(UUID identifier) {
        try {
            return advisorRepository.findByIdentifier(identifier).orElseThrow(() ->
                    AdvisorExceptionFactory.notFound(identifier, messageSource)
            );
        } catch (AdvisorNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Unfiltered — returns advisor including soft-deleted. Used by admin delete/undo-delete.
     */
    public Advisor findByIdentifierIncludingDeletedWithException(UUID identifier) {
        try {
            return advisorRepository.findByIdentifierIncludingDeleted(identifier).orElseThrow(() ->
                    AdvisorExceptionFactory.notFound(identifier, messageSource)
            );
        } catch (AdvisorNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Optional<Advisor> findByUsername(String username) {
        try {
            return advisorRepository.findByUsername(username);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Unfiltered — returns advisor including soft-deleted. Used by creation uniqueness checks.
     */
    public Optional<Advisor> findByUsernameIncludingDeleted(String username) {
        try {
            return advisorRepository.findByUsernameIncludingDeleted(username);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Page<Advisor> findAllWithException(Pageable pageable) {
        try {
            return advisorRepository.findAll(pageable);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Long countWithException() {
        try {
            return advisorRepository.count();
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(UUID id) {
        try {
            advisorRepository.deleteById(id);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Find the latest advisor by mobile number (no office hierarchy filtering).
     * For use in open API flows where staff context is not available.
     *
     * @param mobileNo 10-digit mobile number
     * @return Optional with AdvisorBasicResponse if found, empty otherwise
     */
    public Optional<AdvisorBasicResponse> findAdvisorByMobileNo(String mobileNo) {
        if (!StringUtils.hasText(mobileNo)) {
            return Optional.empty();
        }
        String phoneJson = buildPhoneNumberJsonb(mobileNo.trim());
        String sql = """
            SELECT a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                NULL::text AS referred_by_code,
                NULL::text AS referred_by_type,
                NULL::uuid AS referred_by_identifier,
                NULL::text AS referred_by_name,
                NULL::text AS referred_by_number
            FROM n_person p
            JOIN n_user u ON u.person_id = p.id
            JOIN n_advisor a ON a.username = u.username
            WHERE p.mobile_numbers @> ?::jsonb
            ORDER BY a.updated_at DESC
            LIMIT 1
            """;
        try {
            List<AdvisorBasicResponse> results = jdbcTemplate.query(sql, new AdvisorSearchRowMapper(), phoneJson);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Search advisors by phone number.
     * Finds advisors with persons that have the given phone number.
     * Applies office hierarchy filter to restrict to current staff's office hierarchy.
     *
     * @param paginationRequest Pagination parameters
     * @param request Search request containing mobile number
     * @return PaginatedResponse containing AdvisorBasicResponse objects
     */
    public PaginatedResponse<AdvisorBasicResponse> searchAdvisorsByPhoneNumber(
            PaginationRequest paginationRequest, AdvisorSearchRequest request) {
        // Validate phone number
        if (request == null || !StringUtils.hasText(request.getMobileNumber())) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }

        String mobileNumber = request.getMobileNumber().trim();
        String phoneJson = buildPhoneNumberJsonb(mobileNumber);

        // Get current staff office and code for hierarchy filtering
        String currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
        String currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();

        // Person-first: CTE uses GIN on n_person.mobile_numbers; join advisors via username
        String countSql = """
            WITH person_with_phone AS (SELECT id FROM n_person WHERE mobile_numbers @> ?::jsonb)
            SELECT COUNT(DISTINCT a.id)
            FROM person_with_phone pwp
            JOIN n_user u_phone ON u_phone.person_id = pwp.id
            JOIN n_advisor a ON a.username = u_phone.username
            LEFT JOIN n_office o ON o.key = a.office_key
            WHERE o.code LIKE ?
              AND a.is_deleted = false
            """;

        String dataSql = """
            WITH person_with_phone AS (SELECT id FROM n_person WHERE mobile_numbers @> ?::jsonb)
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode') AS referred_by_code,
                COALESCE(a.referred_by_type::text, r.entity_type::text) AS referred_by_type,
                COALESCE(a.referred_by_identifier, r.entity_identifier) AS referred_by_identifier,
                COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name,
                COALESCE(
                    (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                ) AS referred_by_number
            FROM person_with_phone pwp
            JOIN n_user u_phone ON u_phone.person_id = pwp.id
            JOIN n_advisor a ON a.username = u_phone.username
            JOIN n_person p ON p.id = pwp.id
            LEFT JOIN n_office o ON o.key = a.office_key
            LEFT JOIN n_sourcing_channel_details sc ON sc.id = a.source_channel_id
            LEFT JOIN n_referral_code_registry r ON r.referral_code = COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode')
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
            WHERE o.code LIKE ?
              AND a.is_deleted = false
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            String officePattern = currentUserOfficeCode + "%";

            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, phoneJson, officePattern);
            long total = totalCount != null ? totalCount : 0L;

            // Get paginated data
            List<AdvisorBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdvisorSearchRowMapper(),
                    phoneJson,
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
            throw new RuntimeException("Failed to search advisors by phone number", e);
        }
    }

    /**
     * Get all advisors with pagination and optional filters.
     * Returns a paginated list of AdvisorBasicResponse objects.
     * Applies office hierarchy filter to restrict to current staff's office hierarchy.
     *
     * @param paginationRequest Pagination parameters
     * @param name Optional name filter (case-insensitive partial match)
     * @param mobileNumber Optional mobile number filter (exact match)
     * @return PaginatedResponse containing AdvisorBasicResponse objects
     */
    public PaginatedResponse<AdvisorBasicResponse> findAllAdvisors(
            PaginationRequest paginationRequest, String name, String mobileNumber) {

        // Get current staff office and code for hierarchy filtering
        String currentUserOfficeKey;
        String currentUserOfficeCode;
        String officePattern;

        try {
            currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
            if (currentUserOfficeKey == null || currentUserOfficeKey.trim().isEmpty()) {
                throw new IllegalStateException("Current user does not have an office assigned");
            }
            currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();
            officePattern = currentUserOfficeCode + "%";
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("Staff not found")) {
                throw new IllegalStateException("Current user does not have a staff record. Please contact administrator.", e);
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve current user office information", e);
        }

        // Build WHERE clause dynamically
        List<Object> queryParams = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        whereClause.append(" AND a.is_deleted = false ");

        // Apply office hierarchy filter
        whereClause.append(" AND (o.code LIKE ? OR a.office_key IS NULL) ");
        queryParams.add(officePattern);

        // Apply name filter if provided
        if (StringUtils.hasText(name)) {
            whereClause.append(" AND p.display_name ILIKE ? ");
            queryParams.add("%" + name.trim() + "%");
        }

        // Apply mobile number filter if provided
        if (StringUtils.hasText(mobileNumber)) {
            whereClause.append(" AND EXISTS (")
                    .append("SELECT 1 FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) AS m ")
                    .append("WHERE m->>'number' = ? ")
                    .append(") ");
            queryParams.add(mobileNumber.trim());
        }

        String countSql = """
            SELECT COUNT(DISTINCT a.id)
            FROM n_advisor a
            LEFT JOIN n_office o ON o.key = a.office_key
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            """ + whereClause;

        String dataSql = """
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode') AS referred_by_code,
                COALESCE(a.referred_by_type::text, r.entity_type::text) AS referred_by_type,
                COALESCE(a.referred_by_identifier, r.entity_identifier) AS referred_by_identifier,
                COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name,
                COALESCE(
                    (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                ) AS referred_by_number
            FROM n_advisor a
            LEFT JOIN n_office o ON o.key = a.office_key
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            LEFT JOIN n_sourcing_channel_details sc ON sc.id = a.source_channel_id
            LEFT JOIN n_referral_code_registry r ON r.referral_code = COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode')
            LEFT JOIN n_advisor ref_adv ON ref_adv.identifier = r.entity_identifier AND r.entity_type::text = 'ADVISOR'
            LEFT JOIN n_user ref_adv_u ON ref_adv_u.username = ref_adv.username
            LEFT JOIN n_person ref_adv_p ON ref_adv_p.id = ref_adv_u.person_id
            LEFT JOIN n_staff ref_st ON ref_st.identifier = r.entity_identifier AND r.entity_type::text = 'STAFF'
            LEFT JOIN n_user ref_st_u ON ref_st_u.id = ref_st.user_id
            LEFT JOIN n_person ref_st_p ON ref_st_p.id = ref_st_u.person_id
            LEFT JOIN n_lead ref_lead ON ref_lead.lead_identifier = r.entity_identifier AND r.entity_type::text = 'APPLICANT'
            LEFT JOIN n_contact ref_lead_c ON ref_lead_c.id = (ref_lead.other_details->>'primaryContactId')::bigint
            LEFT JOIN n_person ref_lead_p ON ref_lead_p.id = ref_lead_c.person_id
            """ + whereClause + """
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, queryParams.toArray());
            long total = totalCount != null ? totalCount : 0L;

            // Add pagination parameters
            List<Object> dataQueryParams = new ArrayList<>(queryParams);
            dataQueryParams.add(paginationRequest.getLimit());
            dataQueryParams.add(paginationRequest.getOffset());

            // Get paginated data
            List<AdvisorBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdvisorSearchRowMapper(),
                    dataQueryParams.toArray()
            );

            PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, total);
            return new PaginatedResponse<>(results, paginationInfo);
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to fetch all advisors", e);
        }
    }

    public PaginatedResponse<AdvisorBasicResponse> findAdvisorsByUsername(
            String username, PaginationRequest paginationRequest) {
        if (!StringUtils.hasText(username)) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }
        String countSql = """
            SELECT COUNT(DISTINCT a.id)
            FROM n_advisor a
            LEFT JOIN n_office o ON o.key = a.office_key
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            WHERE a.owner = ?
              AND a.is_deleted = false
            """;
        String dataSql = """
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode') AS referred_by_code,
                COALESCE(a.referred_by_type::text, r.entity_type::text) AS referred_by_type,
                COALESCE(a.referred_by_identifier, r.entity_identifier) AS referred_by_identifier,
                COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name,
                COALESCE(
                    (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                ) AS referred_by_number
            FROM n_advisor a
            LEFT JOIN n_office o ON o.key = a.office_key
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            LEFT JOIN n_sourcing_channel_details sc ON sc.id = a.source_channel_id
            LEFT JOIN n_referral_code_registry r ON r.referral_code = COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode')
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
            WHERE a.owner = ?
              AND a.is_deleted = false
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;
        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, username.trim());
            long total = totalCount != null ? totalCount : 0L;
            List<AdvisorBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdvisorSearchRowMapper(),
                    username.trim(),
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset());
            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to fetch advisors by username", e);
        }
    }

    public PaginatedResponse<AdvisorBasicResponse> findAdvisorsByReferralCode(
            String referralCode, PaginationRequest paginationRequest) {
        if (!StringUtils.hasText(referralCode)) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }
        String countSql = """
            SELECT COUNT(DISTINCT a.id)
            FROM n_advisor a
            LEFT JOIN n_sourcing_channel_details sc ON sc.id = a.source_channel_id
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            WHERE COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode') = ?
              AND a.is_deleted = false
            """;
        String dataSql = """
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode') AS referred_by_code,
                COALESCE(a.referred_by_type::text, r.entity_type::text) AS referred_by_type,
                COALESCE(a.referred_by_identifier, r.entity_identifier) AS referred_by_identifier,
                COALESCE(ref_adv_p.display_name, ref_st_p.display_name, ref_lead_p.display_name, ref_lead_app_p.display_name, ref_app_by_uuid_p.display_name) AS referred_by_name,
                COALESCE(
                    (jsonb_path_query_first(COALESCE(ref_adv_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_st_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_lead_app_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                    (jsonb_path_query_first(COALESCE(ref_app_by_uuid_p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                ) AS referred_by_number
            FROM n_advisor a
            LEFT JOIN n_sourcing_channel_details sc ON sc.id = a.source_channel_id
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            LEFT JOIN n_referral_code_registry r ON r.referral_code = COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode')
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
            WHERE COALESCE(a.referred_by_code, sc.marketing_details->>'referredByCode') = ?
              AND a.is_deleted = false
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;
        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, referralCode.trim());
            long total = totalCount != null ? totalCount : 0L;
            List<AdvisorBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdvisorSearchRowMapper(),
                    referralCode.trim(),
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset());
            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to fetch advisors by referral code", e);
        }
    }

    public Optional<ReferrerDisplayInfo> findReferrerDisplayInfo(EntityType entityType, UUID entityIdentifier) {
        if (entityIdentifier == null) {
            return Optional.empty();
        }
        try {
            return switch (entityType) {
                case STAFF -> {
                    String sql = """
                        SELECT p.display_name AS name,
                               (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS phone
                        FROM n_staff s
                        LEFT JOIN n_user u ON u.id = s.user_id
                        LEFT JOIN n_person p ON p.id = u.person_id
                        WHERE s.identifier = ?::uuid
                        """;
                    var list = jdbcTemplate.query(sql, (rs, rowNum) ->
                                    new ReferrerDisplayInfo(rs.getString("name"), rs.getString("phone")),
                            entityIdentifier.toString());
                    yield list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
                }
                case APPLICANT -> {
                    String sql = """
                        SELECT COALESCE(p_lead.display_name, p_lead_app.display_name, p_app.display_name) AS name,
                               COALESCE(
                                   (jsonb_path_query_first(COALESCE(p_lead.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                                   (jsonb_path_query_first(COALESCE(p_lead_app.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number'),
                                   (jsonb_path_query_first(COALESCE(p_app.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number')
                               ) AS phone
                        FROM (SELECT ?::uuid AS entity_id) params
                        LEFT JOIN n_lead l ON l.lead_identifier = params.entity_id
                        LEFT JOIN n_contact c ON c.id = (l.other_details->>'primaryContactId')::bigint
                        LEFT JOIN n_person p_lead ON p_lead.id = c.person_id
                        LEFT JOIN n_applicant ref_lead_app ON ref_lead_app.id = l.applicant
                        LEFT JOIN n_person p_lead_app ON p_lead_app.id = ref_lead_app.person_id
                        LEFT JOIN n_applicant a ON a.identifier = params.entity_id
                        LEFT JOIN n_person p_app ON p_app.id = a.person_id
                        WHERE l.lead_identifier IS NOT NULL OR a.id IS NOT NULL
                        """;
                    var list = jdbcTemplate.query(sql, (rs, rowNum) ->
                                    new ReferrerDisplayInfo(rs.getString("name"), rs.getString("phone")),
                            entityIdentifier.toString());
                    yield list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(0));
                }
                default -> Optional.empty();
            };
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds leads by referral code (self-advisor) with optional filter by any contact's mobile, name, status, or substatus.
     */
    public PaginatedResponse<AdvisorSelfLeadResponse> findLeadsByReferralCodeWithSearch(
            String referralCode,
            PaginationRequest paginationRequest,
            String mobileNumber,
            String name,
            String status,
            String subStatus,
            String stageKey,
            Boolean pendingPayout) {
        if (!StringUtils.hasText(referralCode)) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }
        boolean hasMobile = StringUtils.hasText(mobileNumber);
        boolean hasName = StringUtils.hasText(name);
        boolean hasStatusFilter = StringUtils.hasText(status);
        boolean hasSubStatusFilter = StringUtils.hasText(subStatus);
        boolean hasStageKey = StringUtils.hasText(stageKey);
        boolean hasPendingPayout = Boolean.TRUE.equals(pendingPayout);
        String namePattern = hasName ? "%" + name.trim().replace("%", "\\%").replace("_", "\\_") + "%" : null;
        String mobilePattern = hasMobile ? "%" + mobileNumber.trim().replace("%", "\\%").replace("_", "\\_") + "%" : null;

        StringBuilder extraClause = new StringBuilder();
        List<Object> extraParams = new ArrayList<>();
        if (hasStatusFilter) {
            extraClause.append(" AND l.status::text = ? ");
            extraParams.add(status.trim().toUpperCase());
        }
        if (hasSubStatusFilter) {
            if ("NONE".equalsIgnoreCase(subStatus.trim())) {
                extraClause.append(" AND l.substatus IS NULL ");
            } else {
                extraClause.append(" AND l.substatus::text = ? ");
                extraParams.add(subStatus.trim().toUpperCase());
            }
        }
        if (hasStageKey) {
            extraClause.append(" AND l.workflow_details->'currentStageDetails'->>'stageKey' = ? ");
            extraParams.add(stageKey.trim().toUpperCase());
        }
        if (hasPendingPayout) {
            extraClause.append(" AND NOT EXISTS (SELECT 1 FROM n_lead_transaction lt WHERE lt.lead_id = l.id) ");
        }
        String extraFilter = extraClause.toString();

        List<Object> params = new ArrayList<>();
        if (hasName) params.add(namePattern);
        if (hasMobile) params.add(mobilePattern);
        params.add(referralCode.trim());

        String countSql;
        if (hasMobile || hasName) {
            StringBuilder sql = new StringBuilder();
            sql.append(" WITH matching_contacts AS ( SELECT c.id FROM n_contact c ");
            sql.append(" INNER JOIN n_person p ON p.id = c.person_id WHERE ");
            if (hasName) sql.append(" p.display_name ILIKE ? ");
            if (hasName && hasMobile) sql.append(" AND ");
            if (hasMobile) sql.append(" EXISTS (SELECT 1 FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) m WHERE m->>'number' LIKE ?) ");
            sql.append(" ) SELECT COUNT(*) FROM n_lead l ");
            sql.append(" LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id ");
            sql.append(" WHERE COALESCE(l.referred_by_code, sc.marketing_details->>'referredByCode') = ? ");
            sql.append(" AND l.is_deleted = false ");
            sql.append(" AND (EXISTS (SELECT 1 FROM jsonb_array_elements_text(COALESCE(l.contacts, '[]'::jsonb)) AS e WHERE (e)::bigint IN (SELECT id FROM matching_contacts)) ");
            sql.append(" OR EXISTS (SELECT 1 FROM jsonb_array_elements_text(COALESCE(l.co_applicants, '[]'::jsonb)) AS e WHERE (e)::bigint IN (SELECT id FROM matching_contacts))) ");
            sql.append(extraFilter);
            countSql = sql.toString();
        } else {
            countSql = " SELECT COUNT(*) FROM n_lead l " +
                    " LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id " +
                    " WHERE COALESCE(l.referred_by_code, sc.marketing_details->>'referredByCode') = ? " +
                    " AND l.is_deleted = false " +
                    extraFilter;
        }

        String dataSql = (hasMobile || hasName)
                ? buildSelfLeadSearchDataSql(hasName, hasMobile, extraFilter)
                : """
                SELECT l.lead_identifier AS lead_identifier,
                       primary_person.display_name AS primary_contact_name,
                       (SELECT m->>'number' FROM jsonb_array_elements(COALESCE(primary_person.mobile_numbers, '[]'::jsonb)) m
                        WHERE (m->>'isPrimary')::boolean = true LIMIT 1) AS primary_contact_phone,
                       l.product_code AS product_code,
                       l.status::text AS status,
                       l.substatus::text AS substatus,
                       l.requested_amount AS requested_amount,
                       l.created_at AS created_at,
                       CAST(NULLIF(l.disbursement_details->>'disbursedAmount', '') AS NUMERIC) AS disbursed_amount,
                       TO_DATE(NULLIF(l.disbursement_details->>'disbursedDate', ''), 'DD-MM-YYYY') AS disbursed_date
                FROM n_lead l
                LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id
                LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person primary_person ON primary_person.id = primary_contact.person_id
                WHERE COALESCE(l.referred_by_code, sc.marketing_details->>'referredByCode') = ?
                AND l.is_deleted = false
                """ + extraFilter + """
                ORDER BY l.created_at DESC
                LIMIT ? OFFSET ?
                """;

        try {
            List<Object> countParams = new ArrayList<>(params);
            countParams.addAll(extraParams);
            List<Object> dataParams = new ArrayList<>(params);
            dataParams.addAll(extraParams);
            dataParams.add(paginationRequest.getLimit());
            dataParams.add(paginationRequest.getOffset());

            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());
            long total = totalCount != null ? totalCount : 0L;
            List<AdvisorSelfLeadResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdvisorSelfLeadRowMapper(),
                    dataParams.toArray());
            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to search self-advisor leads by referral code", e);
        }
    }

    private String buildSelfLeadSearchDataSql(boolean hasName, boolean hasMobile, String extraFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append(" WITH matching_contacts AS ( SELECT c.id FROM n_contact c ");
        sql.append(" INNER JOIN n_person p ON p.id = c.person_id WHERE ");
        if (hasName) sql.append(" p.display_name ILIKE ? ");
        if (hasName && hasMobile) sql.append(" AND ");
        if (hasMobile) sql.append(" EXISTS (SELECT 1 FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) m WHERE m->>'number' LIKE ?) ");
        sql.append(" ) SELECT l.lead_identifier AS lead_identifier, ");
        sql.append(" primary_person.display_name AS primary_contact_name, ");
        sql.append(" (SELECT m->>'number' FROM jsonb_array_elements(COALESCE(primary_person.mobile_numbers, '[]'::jsonb)) m ");
        sql.append(" WHERE (m->>'isPrimary')::boolean = true LIMIT 1) AS primary_contact_phone, ");
        sql.append(" l.product_code AS product_code, l.status::text AS status, l.substatus::text AS substatus, l.requested_amount AS requested_amount, l.created_at AS created_at, ");
        sql.append(" CAST(NULLIF(l.disbursement_details->>'disbursedAmount', '') AS NUMERIC) AS disbursed_amount, ");
        sql.append(" TO_DATE(NULLIF(l.disbursement_details->>'disbursedDate', ''), 'DD-MM-YYYY') AS disbursed_date ");
        sql.append(" FROM n_lead l ");
        sql.append(" LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id ");
        sql.append(" LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint ");
        sql.append(" LEFT JOIN n_person primary_person ON primary_person.id = primary_contact.person_id ");
        sql.append(" WHERE COALESCE(l.referred_by_code, sc.marketing_details->>'referredByCode') = ? ");
        sql.append(" AND l.is_deleted = false ");
        sql.append(" AND (EXISTS (SELECT 1 FROM jsonb_array_elements_text(COALESCE(l.contacts, '[]'::jsonb)) AS e WHERE (e)::bigint IN (SELECT id FROM matching_contacts)) ");
        sql.append(" OR EXISTS (SELECT 1 FROM jsonb_array_elements_text(COALESCE(l.co_applicants, '[]'::jsonb)) AS e WHERE (e)::bigint IN (SELECT id FROM matching_contacts))) ");
        sql.append(extraFilter);
        sql.append(" ORDER BY l.created_at DESC LIMIT ? OFFSET ? ");
        return sql.toString();
    }

    public record ReferrerDisplayInfo(String name, String phone) {}

    /**
     * Admin search: returns all advisors (deleted + non-deleted) matching the phone number,
     * without office hierarchy filtering.
     */
    public PaginatedResponse<AdminAdvisorBasicResponse> adminSearchAdvisorsByPhoneNumber(
            PaginationRequest paginationRequest, AdminAdvisorSearchRequest request) {
        if (request == null || !StringUtils.hasText(request.getMobileNumber())) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }

        String mobileNumber = request.getMobileNumber().trim();
        String phoneJson = buildPhoneNumberJsonb(mobileNumber);

        String countSql = """
            WITH person_with_phone AS (SELECT id FROM n_person WHERE mobile_numbers @> ?::jsonb)
            SELECT COUNT(DISTINCT a.id)
            FROM person_with_phone pwp
            JOIN n_user u ON u.person_id = pwp.id
            JOIN n_advisor a ON a.username = u.username
            """;

        String dataSql = """
            WITH person_with_phone AS (SELECT id FROM n_person WHERE mobile_numbers @> ?::jsonb)
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                a.is_deleted
            FROM person_with_phone pwp
            JOIN n_user u ON u.person_id = pwp.id
            JOIN n_advisor a ON a.username = u.username
            JOIN n_person p ON p.id = u.person_id
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, phoneJson);
            long total = totalCount != null ? totalCount : 0L;

            List<AdminAdvisorBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdminAdvisorSearchRowMapper(),
                    phoneJson,
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset()
            );

            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Admin: returns only soft-deleted advisors, paginated.
     */
    public PaginatedResponse<AdminAdvisorBasicResponse> findDeletedAdvisors(PaginationRequest paginationRequest) {
        String countSql = "SELECT COUNT(*) FROM n_advisor a WHERE a.is_deleted = true";

        String dataSql = """
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at,
                a.office_key as office_key,
                a.username as advisor_username,
                a.is_deleted
            FROM n_advisor a
            JOIN n_user u ON u.username = a.username
            JOIN n_person p ON p.id = u.person_id
            WHERE a.is_deleted = true
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class);
            long total = totalCount != null ? totalCount : 0L;

            List<AdminAdvisorBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdminAdvisorSearchRowMapper(),
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset()
            );

            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<SelfPayoutResponse> getPayoutsByReferralCode(String referralCode, PaginationRequest paginationRequest) {
        String sql = """
                SELECT t.identifier AS transaction_identifier,
                       l.lead_identifier,
                       p.display_name AS lead_name,
                       prod.name->>'default' AS loan_type,
                       CAST(NULLIF(l.disbursement_details->>'disbursedAmount', '') AS NUMERIC) AS loan_disbursed,
                       t.amount, t.status, t.created_at
                FROM n_transaction t
                JOIN n_lead_transaction lt ON lt.transaction_id = t.id
                JOIN n_lead l ON l.id = lt.lead_id
                LEFT JOIN n_contact c ON c.id = (l.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person p ON p.id = c.person_id
                LEFT JOIN n_product prod ON prod.code = l.product_code
                WHERE lt.referral_code = ?
                ORDER BY t.created_at DESC
                LIMIT ? OFFSET ?
                """;
        try {
            return jdbcTemplate.query(sql, new SelfPayoutRowMapper(),
                    referralCode, paginationRequest.getLimit(), paginationRequest.getOffset());
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to query advisor payouts", e);
        }
    }

    public long countPayoutsByReferralCode(String referralCode) {
        String sql = """
                SELECT COUNT(*) FROM n_transaction t
                JOIN n_lead_transaction lt ON lt.transaction_id = t.id
                WHERE lt.referral_code = ?
                """;
        Long count = jdbcTemplate.queryForObject(sql, Long.class, referralCode);
        return count != null ? count : 0L;
    }

    public BigDecimal getTotalPaidByReferralCode(String referralCode) {
        String sql = """
                SELECT COALESCE(SUM(t.amount), 0)
                FROM n_transaction t
                JOIN n_lead_transaction lt ON lt.transaction_id = t.id
                WHERE lt.referral_code = ?
                  AND t.status = 'PAID'
                """;
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class, referralCode);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Optional<SelfPayoutDetailResponse> getPayoutDetailByIdentifierAndReferralCode(
            UUID transactionIdentifier, String referralCode) {
        String sql = """
                SELECT t.identifier AS transaction_identifier,
                       t.amount, t.status, t.created_at, t.remarks,
                       l.lead_identifier,
                       p.display_name AS lead_name,
                       (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS phone_number,
                       prod.name->>'default' AS loan_type,
                       latest_pmt.payment_mode, latest_pmt.external_reference,
                       latest_pmt.payment_status, latest_pmt.payment_date, latest_pmt.payment_data
                FROM n_transaction t
                JOIN n_lead_transaction lt ON lt.transaction_id = t.id
                JOIN n_lead l ON l.id = lt.lead_id
                LEFT JOIN n_contact c ON c.id = (l.other_details->>'primaryContactId')::bigint
                LEFT JOIN n_person p ON p.id = c.person_id
                LEFT JOIN n_product prod ON prod.code = l.product_code
                LEFT JOIN LATERAL (
                    SELECT pmnt.payment_mode, pmnt.external_reference, pmnt.payment_status,
                           pmnt.payment_date, pmnt.payment_data
                    FROM n_transaction_payment pmnt WHERE pmnt.transaction_id = t.id
                    ORDER BY pmnt.id DESC LIMIT 1
                ) latest_pmt ON true
                WHERE t.identifier = ?::uuid
                  AND lt.referral_code = ?
                """;
        try {
            List<SelfPayoutDetailResponse> results = jdbcTemplate.query(sql,
                    new SelfPayoutDetailRowMapper(),
                    transactionIdentifier.toString(), referralCode);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to query advisor payout detail", e);
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

    private static class AdvisorSelfLeadRowMapper implements RowMapper<AdvisorSelfLeadResponse> {
        @Override
        public AdvisorSelfLeadResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            String leadIdStr = rs.getString("lead_identifier");
            String statusStr = rs.getString("status");
            String subStatusStr = rs.getString("substatus");
            java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
            java.sql.Date disbursedDateSql = rs.getDate("disbursed_date");
            return AdvisorSelfLeadResponse.builder()
                    .leadIdentifier(leadIdStr != null ? UUID.fromString(leadIdStr) : null)
                    .leadName(rs.getString("primary_contact_name"))
                    .leadNumber(rs.getString("primary_contact_phone"))
                    .loanType(rs.getString("product_code"))
                    .leadStatus(statusStr != null ? LeadStatus.valueOf(statusStr) : null)
                    .leadSubStatus(subStatusStr != null ? LeadSubStatus.valueOf(subStatusStr) : null)
                    .requestedAmount(rs.getBigDecimal("requested_amount"))
                    .createdAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null)
                    .disbursedAmount(rs.getBigDecimal("disbursed_amount"))
                    .disbursedDate(disbursedDateSql != null ? disbursedDateSql.toLocalDate() : null)
                    .build();
        }
    }

    private static class SelfPayoutRowMapper implements RowMapper<SelfPayoutResponse> {
        @Override
        public SelfPayoutResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            String txnIdStr = rs.getString("transaction_identifier");
            String leadIdStr = rs.getString("lead_identifier");
            java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
            return SelfPayoutResponse.builder()
                    .transactionIdentifier(txnIdStr != null ? UUID.fromString(txnIdStr) : null)
                    .leadIdentifier(leadIdStr != null ? UUID.fromString(leadIdStr) : null)
                    .leadName(rs.getString("lead_name"))
                    .loanType(rs.getString("loan_type"))
                    .loanDisbursed(rs.getBigDecimal("loan_disbursed"))
                    .amount(rs.getBigDecimal("amount"))
                    .status(rs.getString("status"))
                    .createdAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null)
                    .build();
        }
    }

    private static class SelfPayoutDetailRowMapper implements RowMapper<SelfPayoutDetailResponse> {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        @Override
        public SelfPayoutDetailResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            String txnIdStr = rs.getString("transaction_identifier");
            java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
            java.sql.Date paymentDateSql = rs.getDate("payment_date");

            SelfPaymentDetails paymentDetails = null;
            String paymentMode = rs.getString("payment_mode");
            if (paymentMode != null) {
                Map<String, Object> paymentData = null;
                String paymentDataJson = rs.getString("payment_data");
                if (paymentDataJson != null) {
                    try {
                        paymentData = OBJECT_MAPPER.readValue(paymentDataJson,
                                new TypeReference<Map<String, Object>>() {});
                    } catch (Exception ignored) {
                        // skip malformed JSON
                    }
                }
                paymentDetails = SelfPaymentDetails.builder()
                        .paymentMode(paymentMode)
                        .externalReference(rs.getString("external_reference"))
                        .paymentStatus(rs.getString("payment_status"))
                        .paymentDate(paymentDateSql != null ? paymentDateSql.toLocalDate() : null)
                        .paymentData(paymentData)
                        .build();
            }

            String remarksStr = extractLatestRemarkText(rs.getString("remarks"));

            String leadIdStr = rs.getString("lead_identifier");
            AdvisorSelfLeadResponse leadDetails = AdvisorSelfLeadResponse.builder()
                    .leadIdentifier(leadIdStr != null ? UUID.fromString(leadIdStr) : null)
                    .leadName(rs.getString("lead_name"))
                    .leadNumber(rs.getString("phone_number"))
                    .loanType(rs.getString("loan_type"))
                    .build();

            return SelfPayoutDetailResponse.builder()
                    .transactionIdentifier(txnIdStr != null ? UUID.fromString(txnIdStr) : null)
                    .amount(rs.getBigDecimal("amount"))
                    .status(rs.getString("status"))
                    .createdAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null)
                    .leadDetails(leadDetails)
                    .paymentDetails(paymentDetails)
                    .remarks(remarksStr)
                    .build();
        }

        private static String extractLatestRemarkText(String remarksJson) {
            if (remarksJson == null || remarksJson.isBlank()) {
                return null;
            }
            try {
                List<Map<String, Object>> remarksList = OBJECT_MAPPER.readValue(remarksJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                if (remarksList.isEmpty()) {
                    return null;
                }
                Map<String, Object> latest = remarksList.get(remarksList.size() - 1);
                Object text = latest.get("text");
                return text != null ? text.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static class AdvisorSearchRowMapper implements RowMapper<AdvisorBasicResponse> {
        @Override
        public AdvisorBasicResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdvisorBasicResponse.AdvisorBasicResponseBuilder builder = AdvisorBasicResponse.builder();

            String advisorIdentifierStr = rs.getString("advisor_identifier");
            if (advisorIdentifierStr != null) {
                builder.advisorIdentifier(UUID.fromString(advisorIdentifierStr));
            }

            builder.name(rs.getString("person_name"));
            builder.mobileNumber(rs.getString("mobile_number"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(AdvisorStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                    // Invalid status, leave as null
                }
            }

            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                builder.createdAt(createdAt.toLocalDateTime());
            }

            java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                builder.updatedAt(updatedAt.toLocalDateTime());
            }

            builder.officeKey(rs.getString("office_key"));
            builder.username(rs.getString("advisor_username"));

            builder.referredByCode(rs.getString("referred_by_code"));
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
            builder.referredByName(rs.getString("referred_by_name"));
            builder.referredByNumber(rs.getString("referred_by_number"));

            return builder.build();
        }
    }

    private static class AdminAdvisorSearchRowMapper implements RowMapper<AdminAdvisorBasicResponse> {
        @Override
        public AdminAdvisorBasicResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdminAdvisorBasicResponse.AdminAdvisorBasicResponseBuilder builder = AdminAdvisorBasicResponse.builder();

            String advisorIdentifierStr = rs.getString("advisor_identifier");
            if (advisorIdentifierStr != null) {
                builder.advisorIdentifier(UUID.fromString(advisorIdentifierStr));
            }

            builder.name(rs.getString("person_name"));
            builder.mobileNumber(rs.getString("mobile_number"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(AdvisorStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                }
            }

            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                builder.createdAt(createdAt.toLocalDateTime());
            }

            java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                builder.updatedAt(updatedAt.toLocalDateTime());
            }

            builder.officeKey(rs.getString("office_key"));
            builder.username(rs.getString("advisor_username"));
            builder.deleted(rs.getBoolean("is_deleted"));

            return builder.build();
        }
    }
}
