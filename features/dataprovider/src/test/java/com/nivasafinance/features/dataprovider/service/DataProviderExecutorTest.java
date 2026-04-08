package com.nivasafinance.features.dataprovider.service;

import com.nivasafinance.features.dataprovider.entity.DataProvider;
import com.nivasafinance.features.dataprovider.repository.DataProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataProviderExecutorTest {

    @Mock
    private DataProviderRepository dataProviderRepository;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private DataProviderExecutor dataProviderExecutor;

    private static final String TEST_QUERY = "SELECT name, age FROM users WHERE id = :id";
    private static final String PROVIDER_NAME = "user-details";

    private Map<String, Object> params;
    private DataProvider dataProvider;

    @BeforeEach
    void setUp() {
        params = new HashMap<>();
        params.put("id", 1L);

        dataProvider = DataProvider.builder()
                .name(PROVIDER_NAME)
                .query(TEST_QUERY)
                .status("ACTIVE")
                .build();
    }

    // ========== executeQuery ==========

    @Test
    void executeQuery_success_returnsResultMap() {
        Map<String, Object> expected = Map.of("name", "John", "age", 25);
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(expected);

        Map<String, Object> result = dataProviderExecutor.executeQuery(TEST_QUERY, params);

        assertEquals(expected, result);
        verify(namedParameterJdbcTemplate).queryForMap(TEST_QUERY, params);
    }

    @Test
    void executeQuery_nullParams_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dataProviderExecutor.executeQuery(TEST_QUERY, null));

        verifyNoInteractions(namedParameterJdbcTemplate);
    }

    @Test
    void executeQuery_paramWithNullValue_stillExecutes() {
        params.put("nullParam", null);
        Map<String, Object> expected = Map.of("name", "John");
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(expected);

        Map<String, Object> result = dataProviderExecutor.executeQuery(TEST_QUERY, params);

        assertEquals(expected, result);
    }

    // ========== executeQuerySafely ==========

    @Test
    void executeQuerySafely_success_returnsResultMap() {
        Map<String, Object> expected = Map.of("name", "John");
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(expected);

        Map<String, Object> result = dataProviderExecutor.executeQuerySafely(TEST_QUERY, params);

        assertEquals(expected, result);
    }

    @Test
    void executeQuerySafely_emptyResult_returnsEmptyMap() {
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params))
                .thenThrow(new EmptyResultDataAccessException(1));

        Map<String, Object> result = dataProviderExecutor.executeQuerySafely(TEST_QUERY, params);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== executeQueryAsCommaSeparatedString ==========

    @Test
    void executeQueryAsCommaSeparatedString_multipleValues_returnsCommaSeparated() {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("name", "John");
        resultMap.put("city", "Bangalore");
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(resultMap);

        String result = dataProviderExecutor.executeQueryAsCommaSeparatedString(TEST_QUERY, params);

        assertEquals("John,Bangalore", result);
    }

    @Test
    void executeQueryAsCommaSeparatedString_nullValues_convertsToEmptyString() {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("name", "John");
        resultMap.put("city", null);
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(resultMap);

        String result = dataProviderExecutor.executeQueryAsCommaSeparatedString(TEST_QUERY, params);

        assertEquals("John,", result);
    }

    // ========== executeQueryAsStringMap ==========

    @Test
    void executeQueryAsStringMap_success_preservesNullValues() {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("name", "John");
        resultMap.put("city", null);
        resultMap.put("age", 25);
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(resultMap);

        Map<String, String> result = dataProviderExecutor.executeQueryAsStringMap(TEST_QUERY, params);

        assertEquals("John", result.get("name"));
        assertNull(result.get("city"));
        assertEquals("25", result.get("age"));
    }

    // ========== executeQueryForList ==========

    @Test
    void executeQueryForList_success_returnsList() {
        List<Map<String, Object>> expected = List.of(
                Map.of("name", "John"),
                Map.of("name", "Jane")
        );
        when(namedParameterJdbcTemplate.queryForList(TEST_QUERY, params)).thenReturn(expected);

        List<Map<String, Object>> result = dataProviderExecutor.executeQueryForList(TEST_QUERY, params);

        assertEquals(2, result.size());
    }

    @Test
    void executeQueryForList_nullParams_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> dataProviderExecutor.executeQueryForList(TEST_QUERY, null));
    }

    // ========== executeDataProviderForList ==========

    @Test
    void executeDataProviderForList_success_looksUpProviderAndExecutes() {
        List<Map<String, Object>> expected = List.of(Map.of("name", "John"));
        when(dataProviderRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.of(dataProvider));
        when(namedParameterJdbcTemplate.queryForList(TEST_QUERY, params)).thenReturn(expected);

        List<Map<String, Object>> result = dataProviderExecutor.executeDataProviderForList(PROVIDER_NAME, params);

        assertEquals(1, result.size());
        verify(dataProviderRepository).findByName(PROVIDER_NAME);
    }

    @Test
    void executeDataProviderForList_providerNotFound_throwsIllegalArgumentException() {
        when(dataProviderRepository.findByName("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dataProviderExecutor.executeDataProviderForList("unknown", params));

        assertTrue(exception.getMessage().contains("Data provider not found: unknown"));
    }

    // ========== executeDataProvider ==========

    @Test
    void executeDataProvider_success_convertsNullToEmptyString() {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("name", "John");
        resultMap.put("city", null);
        when(dataProviderRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.of(dataProvider));
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(resultMap);

        Map<String, String> result = dataProviderExecutor.executeDataProvider(PROVIDER_NAME, params);

        assertEquals("John", result.get("name"));
        assertEquals("", result.get("city"));
    }

    @Test
    void executeDataProvider_emptyResult_returnsEmptyMap() {
        when(dataProviderRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.of(dataProvider));
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params))
                .thenThrow(new EmptyResultDataAccessException(1));

        Map<String, String> result = dataProviderExecutor.executeDataProvider(PROVIDER_NAME, params);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void executeDataProvider_providerNotFound_throwsIllegalArgumentException() {
        when(dataProviderRepository.findByName("missing")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> dataProviderExecutor.executeDataProvider("missing", params));
    }

    @Test
    void executeDataProvider_allValuesNonNull_convertsToStrings() {
        LinkedHashMap<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put("count", 42);
        resultMap.put("active", true);
        when(dataProviderRepository.findByName(PROVIDER_NAME)).thenReturn(Optional.of(dataProvider));
        when(namedParameterJdbcTemplate.queryForMap(TEST_QUERY, params)).thenReturn(resultMap);

        Map<String, String> result = dataProviderExecutor.executeDataProvider(PROVIDER_NAME, params);

        assertEquals("42", result.get("count"));
        assertEquals("true", result.get("active"));
    }
}
