package com.nivasafinance.features.person.repository;

import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.exception.PersonMobileNumberNotFoundException;
import com.nivasafinance.features.person.exception.PersonNotFoundException;
import com.nivasafinance.features.person.exception.PersonOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonRepositoryWrapperTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private PersonRepositoryWrapper personRepositoryWrapper;

    private Person person;
    private static final Long TEST_PERSON_ID = 1L;
    private static final String TEST_MOBILE = "9876543210";

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(TEST_PERSON_ID);
        person.setFirstName("John");

        lenient().when(messageSource.getMessage(any(), any(), any())).thenReturn("Error message");
    }

    // ========== saveWithException ==========

    @Test
    void saveWithException_success_returnsSavedPerson() {
        when(personRepository.save(person)).thenReturn(person);

        Person result = personRepositoryWrapper.saveWithException(person);

        assertEquals(TEST_PERSON_ID, result.getId());
        verify(personRepository).save(person);
    }

    @Test
    void saveWithException_dataAccessException_throwsPersonOperationException() {
        when(personRepository.save(person)).thenThrow(new QueryTimeoutException("Timeout"));

        assertThrows(PersonOperationException.class,
                () -> personRepositoryWrapper.saveWithException(person));
    }

    // ========== findByIdWithException ==========

    @Test
    void findByIdWithException_found_returnsPerson() {
        when(personRepository.findById(TEST_PERSON_ID)).thenReturn(Optional.of(person));

        Person result = personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID);

        assertEquals(TEST_PERSON_ID, result.getId());
    }

    @Test
    void findByIdWithException_notFound_throwsPersonNotFoundException() {
        when(personRepository.findById(TEST_PERSON_ID)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class,
                () -> personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID));
    }

    @Test
    void findByIdWithException_dataAccessException_throwsPersonOperationException() {
        when(personRepository.findById(TEST_PERSON_ID)).thenThrow(new QueryTimeoutException("Timeout"));

        assertThrows(PersonOperationException.class,
                () -> personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID));
    }

    // ========== findByPrimaryMobileNumber ==========

    @Test
    void findByPrimaryMobileNumber_found_returnsOptionalWithPerson() {
        when(personRepository.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(person));

        Optional<Person> result = personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE);

        assertTrue(result.isPresent());
        assertEquals(TEST_PERSON_ID, result.get().getId());
    }

    @Test
    void findByPrimaryMobileNumber_notFound_returnsEmptyOptional() {
        when(personRepository.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.empty());

        Optional<Person> result = personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByPrimaryMobileNumber_dataAccessException_throwsPersonOperationException() {
        when(personRepository.findByPrimaryMobileNumber(TEST_MOBILE))
                .thenThrow(new QueryTimeoutException("Timeout"));

        assertThrows(PersonOperationException.class,
                () -> personRepositoryWrapper.findByPrimaryMobileNumber(TEST_MOBILE));
    }

    // ========== findPersonByEmail ==========

    @Test
    void findPersonByEmail_found_returnsOptionalWithPerson() {
        when(personRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(List.of(person));

        Optional<Person> result = personRepositoryWrapper.findPersonByEmail("john@test.com");

        assertTrue(result.isPresent());
    }

    @Test
    void findPersonByEmail_notFound_returnsEmptyOptional() {
        when(personRepository.findByEmailIgnoreCase("unknown@test.com")).thenReturn(List.of());

        Optional<Person> result = personRepositoryWrapper.findPersonByEmail("unknown@test.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findPersonByEmail_nullEmail_returnsEmptyOptional() {
        Optional<Person> result = personRepositoryWrapper.findPersonByEmail(null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(personRepository);
    }

    @Test
    void findPersonByEmail_blankEmail_returnsEmptyOptional() {
        Optional<Person> result = personRepositoryWrapper.findPersonByEmail("   ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(personRepository);
    }

    @Test
    void findPersonByEmail_trimsEmail_beforeQuery() {
        when(personRepository.findByEmailIgnoreCase("john@test.com")).thenReturn(List.of(person));

        Optional<Person> result = personRepositoryWrapper.findPersonByEmail("  john@test.com  ");

        assertTrue(result.isPresent());
        verify(personRepository).findByEmailIgnoreCase("john@test.com");
    }

    @Test
    void findPersonByEmail_multipleResults_returnsFirst() {
        Person person2 = new Person();
        person2.setId(2L);
        when(personRepository.findByEmailIgnoreCase("dup@test.com")).thenReturn(List.of(person, person2));

        Optional<Person> result = personRepositoryWrapper.findPersonByEmail("dup@test.com");

        assertTrue(result.isPresent());
        assertEquals(TEST_PERSON_ID, result.get().getId());
    }

    @Test
    void findPersonByEmail_dataAccessException_throwsPersonOperationException() {
        when(personRepository.findByEmailIgnoreCase("john@test.com"))
                .thenThrow(new QueryTimeoutException("Timeout"));

        assertThrows(PersonOperationException.class,
                () -> personRepositoryWrapper.findPersonByEmail("john@test.com"));
    }

    // ========== findByPrimaryMobileNumberWithException ==========

    @Test
    void findByPrimaryMobileNumberWithException_found_returnsPerson() {
        when(personRepository.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(person));

        Person result = personRepositoryWrapper.findByPrimaryMobileNumberWithException(TEST_MOBILE);

        assertEquals(TEST_PERSON_ID, result.getId());
    }

    @Test
    void findByPrimaryMobileNumberWithException_notFound_throwsMobileNotFoundException() {
        when(personRepository.findByPrimaryMobileNumber(TEST_MOBILE)).thenReturn(Optional.empty());

        assertThrows(PersonMobileNumberNotFoundException.class,
                () -> personRepositoryWrapper.findByPrimaryMobileNumberWithException(TEST_MOBILE));
    }

    @Test
    void findByPrimaryMobileNumberWithException_dataAccessException_throwsPersonOperationException() {
        when(personRepository.findByPrimaryMobileNumber(TEST_MOBILE))
                .thenThrow(new QueryTimeoutException("Timeout"));

        assertThrows(PersonOperationException.class,
                () -> personRepositoryWrapper.findByPrimaryMobileNumberWithException(TEST_MOBILE));
    }

    // ========== findActiveSuccessCreditBureauEnquiry ==========

    @Test
    void findActiveSuccessCreditBureauEnquiry_found_returnsResult() {
        Map<String, Object> row = Map.of("enquiry_id", 100L, "status", "SUCCESS");
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenReturn(List.of(row));

        Optional<Map<String, Object>> result =
                personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().get("enquiry_id"));
    }

    @Test
    void findActiveSuccessCreditBureauEnquiry_notFound_returnsEmpty() {
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenReturn(List.of());

        Optional<Map<String, Object>> result =
                personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30);

        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveSuccessCreditBureauEnquiry_exception_throwsRuntimeException() {
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(TEST_PERSON_ID, 30));
    }

    // ========== findPersonIdByCbEnquiryId ==========

    @Test
    void findPersonIdByCbEnquiryId_found_returnsPersonId() {
        Map<String, Object> row = Map.of("id", 1L);
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenReturn(List.of(row));

        Optional<Long> result = personRepositoryWrapper.findPersonIdByCbEnquiryId(100L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get());
    }

    @Test
    void findPersonIdByCbEnquiryId_notFound_returnsEmpty() {
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenReturn(List.of());

        Optional<Long> result = personRepositoryWrapper.findPersonIdByCbEnquiryId(100L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findPersonIdByCbEnquiryId_exception_throwsRuntimeException() {
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> personRepositoryWrapper.findPersonIdByCbEnquiryId(100L));
    }

    // ========== findValidConsentForPerson ==========

    @Test
    void findValidConsentForPerson_found_returnsResult() {
        Map<String, Object> row = Map.of("consent_id", 50L, "status", "RECEIVED");
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenReturn(List.of(row));

        Optional<Map<String, Object>> result =
                personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90);

        assertTrue(result.isPresent());
        assertEquals(50L, result.get().get("consent_id"));
    }

    @Test
    void findValidConsentForPerson_notFound_returnsEmpty() {
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenReturn(List.of());

        Optional<Map<String, Object>> result =
                personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90);

        assertTrue(result.isEmpty());
    }

    @Test
    void findValidConsentForPerson_exception_throwsRuntimeException() {
        when(namedParameterJdbcTemplate.queryForList(anyString(), any(SqlParameterSource.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class,
                () -> personRepositoryWrapper.findValidConsentForPerson(TEST_PERSON_ID, 90));
    }

    // ========== findByMobileNumberWithException ==========

    @Test
    void findByMobileNumberWithException_found_returnsList() {
        when(personRepository.findByMobileNumber(TEST_MOBILE)).thenReturn(Optional.of(person));

        List<Person> result = personRepositoryWrapper.findByMobileNumberWithException(TEST_MOBILE);

        assertEquals(1, result.size());
        assertEquals(TEST_PERSON_ID, result.get(0).getId());
    }

    @Test
    void findByMobileNumberWithException_notFound_returnsEmptyList() {
        when(personRepository.findByMobileNumber(TEST_MOBILE)).thenReturn(Optional.empty());

        List<Person> result = personRepositoryWrapper.findByMobileNumberWithException(TEST_MOBILE);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByMobileNumberWithException_dataAccessException_throwsPersonOperationException() {
        when(personRepository.findByMobileNumber(TEST_MOBILE))
                .thenThrow(new QueryTimeoutException("Timeout"));

        assertThrows(PersonOperationException.class,
                () -> personRepositoryWrapper.findByMobileNumberWithException(TEST_MOBILE));
    }
}
