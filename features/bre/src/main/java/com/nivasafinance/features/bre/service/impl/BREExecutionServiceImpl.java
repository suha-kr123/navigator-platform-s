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
import com.nivasafinance.features.bre.service.BREExecutionService;
import com.nivasafinance.features.bre.service.BREProviderExecutor;
import com.nivasafinance.features.bre.service.BREProviderExecutorFactory;
import com.nivasafinance.redash.service.RedashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BREExecutionServiceImpl implements BREExecutionService {

    private final BREConfigRepositoryWrapper breConfigRepositoryWrapper;
    private final BRELogRepositoryWrapper breLogRepositoryWrapper;
    private final RedashService redashService;
    private final BREProviderExecutorFactory providerExecutorFactory;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    @Async
    public CompletableFuture<BREExecutionResponse> execute(String uname, BREExecutionRequest request) {
        BREConfigs config = breConfigRepositoryWrapper.findByUnameWithException(uname);

        BRELogs breLog = BRELogs.builder()
                .entityType(request.getEntity())
                .entityId(request.getEntityId() != null ? request.getEntityId() : null)
                .breConfigId(config.getId())
                .request(null)
                .response(null)
                .build();
        BRELogs savedLog = breLogRepositoryWrapper.saveWithException(breLog);
        Long logId = savedLog.getId();

        return executeAsync(savedLog, config, request.getParams())
                .thenApply(result -> {
                    try {
                        BRELogs updatedLog = breLogRepositoryWrapper.findByIdWithException(logId);
                        updatedLog.setResponse(result);
                        breLogRepositoryWrapper.saveWithException(updatedLog);
                        log.info("BRE execution completed for logId {}", logId);

                        Map<String, Object> responseMap = objectMapper.readValue(result, new TypeReference<>() {});
                        Map<String, Object> requestMap = updatedLog.getRequest() != null
                                ? objectMapper.readValue(updatedLog.getRequest(), new TypeReference<>() {}) : null;

                        return BREExecutionResponse.builder()
                                .logId(logId)
                                .response(responseMap)
                                .request(requestMap)
                                .build();
                    } catch (Exception e) {
                        log.error("Error saving BRE result for logId {}", logId, e);
                        return BREExecutionResponse.builder()
                                .logId(logId)
                                .error(e.getMessage())
                                .build();
                    }
                })
                .exceptionally(throwable -> {
                    String errorMessage = throwable.getCause() != null
                            ? throwable.getCause().getMessage()
                            : throwable.getMessage();
                    log.error("BRE execution failed for logId {}", logId, throwable);
                    handleExecutionError(logId, errorMessage);
                    return BREExecutionResponse.builder()
                            .logId(logId)
                            .error(errorMessage)
                            .build();
                });
    }

    @Async
    public CompletableFuture<String> executeAsync(BRELogs breLog, BREConfigs config, Map<String, Object> parameters) {
        Map<String, Object> inputMap = buildInputFromDataProvider(config.getConfigs().getDataProviderId(), parameters);
        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(inputMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize BRE input", e);
        }

        BRELogs updatedLog = breLogRepositoryWrapper.findByIdWithException(breLog.getId());
        updatedLog.setRequest(inputJson);
        breLogRepositoryWrapper.saveWithException(updatedLog);

        BREProvider provider = config.getConfigs() != null ? config.getConfigs().getProvider() : null;
        BREProviderExecutor executor = providerExecutorFactory.getExecutor(provider);
        String result = executor.evaluate(config, inputJson);

        return CompletableFuture.completedFuture(result);
    }

    private Map<String, Object> buildInputFromDataProvider(Long dataProviderId, Map<String, Object> parameters) {
        if (dataProviderId == null) {
            return Map.of();
        }
        try {
            Map<String, Object> row = redashService.getQueryResult(dataProviderId, parameters);
            return row != null ? row : Map.of();
        } catch (Exception e) {
            throw new RedashDataProviderException(
                    "Redash getQueryResult failed for dataProviderId=" + dataProviderId + ": " + e.getMessage(), e);
        }
    }

    private void handleExecutionError(Long logId, String errorMessage) {
        try {
            BRELogs breLog = breLogRepositoryWrapper.findByIdWithException(logId);
            String errorMsg = errorMessage != null ? errorMessage.replace("\"", "\\\"") : "Unknown error";
            breLog.setResponse("{\"error\":\"" + errorMsg + "\"}");
            breLogRepositoryWrapper.saveWithException(breLog);
        } catch (Exception saveEx) {
            log.error("Failed to save error response to breLog id {}", logId, saveEx);
        }
    }

}
