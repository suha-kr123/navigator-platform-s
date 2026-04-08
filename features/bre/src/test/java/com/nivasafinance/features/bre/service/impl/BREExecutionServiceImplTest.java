package com.nivasafinance.features.bre.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.bre.dto.BREExecutionRequest;
import com.nivasafinance.features.bre.dto.BREExecutionResponse;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.entity.BRELogs;
import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.bre.exception.RedashDataProviderException;
import com.nivasafinance.features.bre.repository.BREConfigRepositoryWrapper;
import com.nivasafinance.features.bre.repository.BRELogRepositoryWrapper;
import com.nivasafinance.features.bre.service.BREProviderExecutor;
import com.nivasafinance.features.bre.service.BREProviderExecutorFactory;
import com.nivasafinance.redash.service.RedashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BREExecutionServiceImplTest {

    @Mock
    private BREConfigRepositoryWrapper breConfigRepositoryWrapper;

    @Mock
    private BRELogRepositoryWrapper breLogRepositoryWrapper;

    @Mock
    private RedashService redashService;

    @Mock
    private BREProviderExecutorFactory providerExecutorFactory;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BREExecutionServiceImpl service;

    // ── executeAsync ──

    @Test
    void executeAsync_whenDataProviderIdIsNull_usesEmptyInputMap() throws Exception {
        BREConfigs config = buildConfig("cfg-1", BREProvider.GORULES, null);
        BRELogs log = buildLog(10L);
        when(breLogRepositoryWrapper.findByIdWithException(10L)).thenReturn(log);
        when(breLogRepositoryWrapper.saveWithException(any(BRELogs.class))).thenReturn(log);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        BREProviderExecutor executor = mock(BREProviderExecutor.class);
        when(providerExecutorFactory.getExecutor(BREProvider.GORULES)).thenReturn(executor);
        when(executor.evaluate(config, "{}")).thenReturn("{\"result\":\"ok\"}");

        CompletableFuture<String> result = service.executeAsync(log, config, null);

        assertEquals("{\"result\":\"ok\"}", result.get(),
                "Should return the executor result when data provider ID is null");
        verify(redashService, never()).getQueryResult(anyLong(), anyMap());
    }

    @Test
    void executeAsync_whenDataProviderIdIsPresent_fetchesFromRedash() throws Exception {
        BREConfigs config = buildConfig("cfg-1", BREProvider.GORULES, 5L);
        BRELogs log = buildLog(10L);
        Map<String, Object> params = Map.of("key", "val");
        Map<String, Object> redashRow = Map.of("field1", "value1");

        when(redashService.getQueryResult(5L, params)).thenReturn(redashRow);
        when(breLogRepositoryWrapper.findByIdWithException(10L)).thenReturn(log);
        when(breLogRepositoryWrapper.saveWithException(any(BRELogs.class))).thenReturn(log);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"field1\":\"value1\"}");

        BREProviderExecutor executor = mock(BREProviderExecutor.class);
        when(providerExecutorFactory.getExecutor(BREProvider.GORULES)).thenReturn(executor);
        when(executor.evaluate(config, "{\"field1\":\"value1\"}")).thenReturn("{\"score\":100}");

        CompletableFuture<String> result = service.executeAsync(log, config, params);

        assertEquals("{\"score\":100}", result.get(),
                "Should return executor result with redash-provided input");
        verify(redashService).getQueryResult(5L, params);
    }

    @Test
    void executeAsync_whenRedashReturnsNull_usesEmptyMap() throws Exception {
        BREConfigs config = buildConfig("cfg-1", BREProvider.GORULES, 5L);
        BRELogs log = buildLog(10L);

        when(redashService.getQueryResult(eq(5L), any())).thenReturn(null);
        when(breLogRepositoryWrapper.findByIdWithException(10L)).thenReturn(log);
        when(breLogRepositoryWrapper.saveWithException(any(BRELogs.class))).thenReturn(log);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        BREProviderExecutor executor = mock(BREProviderExecutor.class);
        when(providerExecutorFactory.getExecutor(BREProvider.GORULES)).thenReturn(executor);
        when(executor.evaluate(config, "{}")).thenReturn("{\"result\":\"empty\"}");

        CompletableFuture<String> result = service.executeAsync(log, config, null);

        assertEquals("{\"result\":\"empty\"}", result.get(),
                "Should use empty map when redash returns null");
    }

    @Test
    void executeAsync_whenRedashThrows_throwsRedashDataProviderException() {
        BREConfigs config = buildConfig("cfg-1", BREProvider.GORULES, 5L);
        BRELogs log = buildLog(10L);

        when(redashService.getQueryResult(eq(5L), any()))
                .thenThrow(new RuntimeException("Redash unavailable"));

        assertThrows(RedashDataProviderException.class,
                () -> service.executeAsync(log, config, Map.of()),
                "Should throw RedashDataProviderException when redash call fails");
    }

    @Test
    void executeAsync_whenSerializationFails_throwsRuntimeException() throws Exception {
        BREConfigs config = buildConfig("cfg-1", BREProvider.GORULES, null);
        BRELogs log = buildLog(10L);

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("ser error") {});

        assertThrows(RuntimeException.class,
                () -> service.executeAsync(log, config, null),
                "Should throw RuntimeException when input serialization fails");
    }

    // ── helpers ──

    private BREConfigs buildConfig(String uname, BREProvider provider, Long dataProviderId) {
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(provider)
                .dataProviderId(dataProviderId)
                .build();
        return BREConfigs.builder().uname(uname).configs(configs).build();
    }

    private BRELogs buildLog(Long id) {
        BRELogs log = BRELogs.builder().breConfigId(1L).build();
        try {
            var field = log.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(log, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set log ID via reflection", e);
        }
        return log;
    }
}
