package com.nivasafinance.notification.orchestrator.service;

import com.nivasafinance.notification.orchestrator.entity.DataProvider;
import com.nivasafinance.notification.orchestrator.repository.DataProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProviderExecutor {

    private final DataProviderRepository dataProviderRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Map<String, Object> executeQuery(String query, Map<String, Object> params) {
        log.debug("Executing data provider query: {}", query);
        log.info("Data provider query parameters: {}", params);
        validateParameters(params);
        return namedParameterJdbcTemplate.queryForMap(query, params);
    }

    public Map<String, Object> executeQuerySafely(String query, Map<String, Object> params) {
        try {
            return executeQuery(query, params);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Data provider query returned no results");
            return new HashMap<>();
        }
    }

    public String executeQueryAsCommaSeparatedString(String query, Map<String, Object> params) {
        Map<String, Object> result = executeQuery(query, params);
        return result.values().stream()
                .map(value -> value != null ? value.toString() : "")
                .collect(Collectors.joining(","));
    }

    public Map<String, String> executeQueryAsStringMap(String query, Map<String, Object> params) {
        Map<String, Object> result = executeQuery(query, params);
        return result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() != null
                        ? entry.getValue().toString()
                        : null));
    }

    public List<Map<String, Object>> executeQueryForList(String query, Map<String, Object> params) {
        validateParameters(params);
        return namedParameterJdbcTemplate.queryForList(query, params);
    }

    public Map<String, String> executeDataProvider(String providerName, Map<String, Object> params) {
        DataProvider provider = dataProviderRepository.findByName(providerName)
                .orElseThrow(() -> new IllegalArgumentException("Data provider not found: " + providerName));
        log.info("Executing data provider '{}' with parameters: {}", providerName, params);
        log.debug("Data provider query: {}", provider.getQuery());
        Map<String, Object> result = executeQuerySafely(provider.getQuery(), params);
        log.info("Data provider '{}' returned {} result(s). Keys: {}", providerName, result.size(), result.keySet());
        
        // Convert to Map<String, String> handling null values
        // Collectors.toMap doesn't allow null values, so convert nulls to empty strings
        Map<String, String> stringMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            stringMap.put(entry.getKey(), value);
        }
        return stringMap;
    }

    private void validateParameters(Map<String, Object> params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        params.forEach((key, value) -> {
            if (value == null) {
                log.warn("Parameter '{}' has null value", key);
            }
        });
    }
}


