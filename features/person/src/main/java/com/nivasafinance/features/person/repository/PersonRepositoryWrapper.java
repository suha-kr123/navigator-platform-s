package com.nivasafinance.features.person.repository;

import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.exception.PersonExceptionFactory;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

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
            return personRepository.findById(id)
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
}
