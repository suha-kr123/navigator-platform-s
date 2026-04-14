package com.nivasafinance.features.person.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.person.dto.AdminPersonResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.exception.PersonExceptionFactory;
import java.util.Collections;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PersonRepositoryWrapper {

    private final PersonRepository personRepository;
    private final MessageSource messageSource;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Person saveWithException(Person person) {
        try {
            return personRepository.save(person);
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.createFailed(messageSource);
        }
    }

    public Person findByIdWithException(Long id) {
        try {
            return personRepository.findByIdAndNotDeleted(id)
                    .orElseThrow(() -> PersonExceptionFactory.notFound(id, messageSource));
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Optional<Person> findByPrimaryMobileNumber(String mobileNumber) {
        try {
            return personRepository.findByPrimaryMobileNumber(mobileNumber);
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    /**
     * Unfiltered — returns person including soft-deleted. Used by creation flows to prevent duplicates.
     */
    public Optional<Person> findByPrimaryMobileNumberIncludingDeleted(String mobileNumber) {
        try {
            return personRepository.findByPrimaryMobileNumberIncludingDeleted(mobileNumber);
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Optional<Person> findPersonByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            return personRepository.findByEmailIgnoreCase(email.trim()).stream().findFirst();
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public Person findByPrimaryMobileNumberWithException(String mobileNumber) {
        try {
            return personRepository.findByPrimaryMobileNumber(mobileNumber).orElseThrow(() ->
                    PersonExceptionFactory.mobileNumberNotFound(mobileNumber, messageSource));
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    /**
     * Finds active SUCCESS credit bureau enquiry for a person.
     * Checks latest_success_enquiry_id in person's cb_details and validates the enquiry is SUCCESS and not stale.
     * 
     * @param personId The person ID
     * @param stalePeriodInDays Number of days after which enquiry is considered stale
     * @return Optional containing enquiry ID and reportId if found, empty otherwise
     */
    public Optional<Map<String, Object>> findActiveSuccessCreditBureauEnquiry(Long personId, Integer stalePeriodInDays) {
        String query = """
                SELECT e.id AS enquiry_id,
                       e.report_id,
                       e.status,
                       e.identifier AS enquiry_identifier,
                       e.created_at
                FROM n_person p
                INNER JOIN n_cb_enquiry e
                       ON e.id = (p.cb_details->>'latestSuccessEnquiryId')::bigint
                WHERE p.id = :personId
                  AND p.is_deleted = false
                  AND p.cb_details->>'latestSuccessEnquiryId' IS NOT NULL
                  AND e.status = 'SUCCESS'
                  AND e.report_id IS NOT NULL
                  AND (
                    e.created_at IS NULL
                    OR e.created_at > CURRENT_TIMESTAMP - (CAST(:stalePeriodInDays AS integer) || ' days')::interval
                  )
                ORDER BY e.created_at DESC
                LIMIT 1
                """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("personId", personId);
            params.addValue("stalePeriodInDays", stalePeriodInDays);
            
            var results = namedParameterJdbcTemplate.queryForList(query, params);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find active success enquiry for person: " + personId, e);
        }
    }

    /**
     * Finds the person ID whose cb_enquiry_id array contains the given enquiry ID.
     * Used when n_cb_enquiry does not store person_id; the link is maintained via person.cb_enquiry_id.
     *
     * @param enquiryId The credit bureau enquiry ID
     * @return Optional of person ID if found, empty otherwise
     */
    public Optional<Long> findPersonIdByCbEnquiryId(Long enquiryId) {
        String query = """
                SELECT p.id FROM n_person p
                WHERE p.cb_enquiry_id IS NOT NULL
                  AND p.is_deleted = false
                  AND EXISTS (
                    SELECT 1 FROM jsonb_array_elements_text(p.cb_enquiry_id) AS elem
                    WHERE elem::bigint = :enquiryId
                  )
                LIMIT 1
                """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource("enquiryId", enquiryId);
            var results = namedParameterJdbcTemplate.queryForList(query, params);
            return results.isEmpty()
                    ? Optional.empty()
                    : Optional.of(((Number) results.get(0).get("id")).longValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find person by cb_enquiry_id for enquiry: " + enquiryId, e);
        }
    }

    /**
     * Finds a valid reusable consent for a person.
     * Checks person.consent_details for CB consent, then validates it's RECEIVED and within validity period.
     *
     * @param personId The person ID
     * @param validityPeriodInDays Number of days for which consent is considered valid
     * @return Optional containing consent ID and identifier if found, empty otherwise
     */
    public Optional<Map<String, Object>> findValidConsentForPerson(Long personId, Integer validityPeriodInDays) {
        String query = """
                SELECT c.id AS consent_id,
                       c.identifier AS consent_identifier,
                       c.status,
                       c.consent_received_details->>'consentReceivedTime' AS consent_received_time,
                       CASE
                         WHEN c.consent_received_details->>'consentReceivedTime' IS NULL THEN NULL
                         WHEN c.consent_received_details->>'consentReceivedTime' ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}' THEN
                           (c.consent_received_details->>'consentReceivedTime')::timestamp
                         ELSE
                           TO_TIMESTAMP(c.consent_received_details->>'consentReceivedTime', 'DD-MM-YYYY HH24:MI:SS')
                       END AS consent_received_timestamp
                FROM n_person p
                CROSS JOIN LATERAL jsonb_array_elements(p.consent_details) AS consent_elem
                INNER JOIN n_consent c
                       ON c.id = (consent_elem->>'id')::bigint
                WHERE p.id = :personId
                  AND p.is_deleted = false
                  AND p.consent_details IS NOT NULL
                  AND consent_elem->>'type' = 'CB'
                  AND c.status = 'RECEIVED'
                  AND c.consent_received_details IS NOT NULL
                  AND (
                    c.consent_received_details->>'consentReceivedTime' IS NULL
                    OR CASE
                         WHEN c.consent_received_details->>'consentReceivedTime' ~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}' THEN
                           (c.consent_received_details->>'consentReceivedTime')::timestamp
                         ELSE
                           TO_TIMESTAMP(c.consent_received_details->>'consentReceivedTime', 'DD-MM-YYYY HH24:MI:SS')
                       END > CURRENT_TIMESTAMP - (CAST(:validityPeriodInDays AS integer) || ' days')::interval
                  )
                ORDER BY consent_received_timestamp DESC NULLS LAST
                LIMIT 1
                """;
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("personId", personId);
            params.addValue("validityPeriodInDays", validityPeriodInDays);

            var results = namedParameterJdbcTemplate.queryForList(query, params);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find valid consent for person: " + personId, e);
        }
    }

    public List<Person> findByMobileNumberWithException(String mobileNumber) {
        try {
            return personRepository.findByMobileNumber(mobileNumber).stream()
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    /**
     * Admin search: returns all persons (deleted + non-deleted) matching the mobile number.
     */
    public PaginatedResponse<AdminPersonResponse> adminSearchPersonsByMobileNumber(
            PaginationRequest paginationRequest, String mobileNumber) {
        if (!StringUtils.hasText(mobileNumber)) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }

        String phoneJson = buildPhoneNumberJsonb(mobileNumber.trim());

        String countSql = """
            SELECT COUNT(*) FROM n_person p
            WHERE p.mobile_numbers @> :phoneJson::jsonb
            """;

        String dataSql = """
            SELECT p.first_name, p.middle_name, p.last_name, p.display_name,
                   p.email, p.created_at, p.created_by, p.updated_at, p.updated_by,
                   p.is_deleted,
                   (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_mobile_number
            FROM n_person p
            WHERE p.mobile_numbers @> :phoneJson::jsonb
            ORDER BY p.updated_at DESC
            LIMIT :limit OFFSET :offset
            """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("phoneJson", phoneJson);

            Long totalCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
            long total = totalCount != null ? totalCount : 0L;

            params.addValue("limit", paginationRequest.getLimit());
            params.addValue("offset", paginationRequest.getOffset());

            List<AdminPersonResponse> results = namedParameterJdbcTemplate.query(dataSql, params,
                    (rs, rowNum) -> AdminPersonResponse.builder()
                            .firstName(rs.getString("first_name"))
                            .middleName(rs.getString("middle_name"))
                            .lastName(rs.getString("last_name"))
                            .displayName(rs.getString("display_name"))
                            .email(rs.getString("email"))
                            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                            .createdBy(rs.getString("created_by"))
                            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                            .updatedBy(rs.getString("updated_by"))
                            .deleted(rs.getBoolean("is_deleted"))
                            .primaryMobileNumber(rs.getString("primary_mobile_number"))
                            .build());

            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    /**
     * Admin: returns only soft-deleted persons, paginated.
     */
    public PaginatedResponse<AdminPersonResponse> findDeletedPersons(PaginationRequest paginationRequest) {
        String countSql = "SELECT COUNT(*) FROM n_person p WHERE p.is_deleted = true";

        String dataSql = """
            SELECT p.first_name, p.middle_name, p.last_name, p.display_name,
                   p.email, p.created_at, p.created_by, p.updated_at, p.updated_by,
                   p.is_deleted,
                   (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primary_mobile_number
            FROM n_person p
            WHERE p.is_deleted = true
            ORDER BY p.updated_at DESC
            LIMIT :limit OFFSET :offset
            """;

        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            Long totalCount = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
            long total = totalCount != null ? totalCount : 0L;

            params.addValue("limit", paginationRequest.getLimit());
            params.addValue("offset", paginationRequest.getOffset());

            List<AdminPersonResponse> results = namedParameterJdbcTemplate.query(dataSql, params,
                    (rs, rowNum) -> AdminPersonResponse.builder()
                            .firstName(rs.getString("first_name"))
                            .middleName(rs.getString("middle_name"))
                            .lastName(rs.getString("last_name"))
                            .displayName(rs.getString("display_name"))
                            .email(rs.getString("email"))
                            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                            .createdBy(rs.getString("created_by"))
                            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                            .updatedBy(rs.getString("updated_by"))
                            .deleted(rs.getBoolean("is_deleted"))
                            .primaryMobileNumber(rs.getString("primary_mobile_number"))
                            .build());

            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (DataAccessException e) {
            throw PersonExceptionFactory.retrieveEntityFailed(messageSource);
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
}
