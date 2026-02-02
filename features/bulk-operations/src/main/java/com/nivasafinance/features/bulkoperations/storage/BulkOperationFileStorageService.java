package com.nivasafinance.features.bulkoperations.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.document.storage.ContentRepository;
import com.nivasafinance.features.document.storage.ContentRepositoryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkOperationFileStorageService {

    private static final String DEFAULT_FILE_NAME = "upload.csv";
    private static final String PATH_PREFIX = "bulk-operations/%s/%s";
    private static final String SANITIZE_REGEX = "[^a-zA-Z0-9._-]";

    private static final String SAVED_BULK_OPERATION_CSV_FILE_FOR_OPERATION = "Saved bulk operation CSV file for operation {} to storage: {}";
    private static final String FAILED_TO_SAVE_BULK_OPERATION_CSV_FILE_FOR_OPERATION = "Failed to save bulk operation CSV file for operation {}: {}";
    private static final String FAILED_TO_FETCH_BULK_OPERATION_CSV_FILE_FROM_STORAGE = "Failed to fetch bulk operation CSV file from storage: {}: {}";
    private static final String DELETED_BULK_OPERATION_CSV_FILE_FROM_STORAGE = "Deleted bulk operation CSV file from storage: {}";
    private static final String FAILED_TO_DELETE_BULK_OPERATION_CSV_FILE_FROM_STORAGE = "Failed to delete bulk operation CSV file from storage: {}: {}";
    private static final String SAVED_CSV_REPORT_FOR_OPERATION = "Saved CSV report for operation {} to storage: {}";
    private static final String FAILED_TO_SAVE_CSV_REPORT_FOR_OPERATION = "Failed to save CSV report for operation {}: {}";
    private static final String VALIDATION_ERRORS_FILE_NAME = "validation_errors.json";

    @Value("${document.storage.provider:LOCAL}")
    private String storageProvider;

    private final ContentRepositoryFactory contentRepositoryFactory;
    private final BulkOperationExceptionFactory exceptionFactory;
    private final ObjectMapper objectMapper;

    public String saveFile(MultipartFile file, UUID operationId) {
        ValidationUtils.requireNonNull(operationId, () -> exceptionFactory.operationIdRequiredException());
        ValidationUtils.requireNonNull(file, () -> exceptionFactory.storageFileRequiredException());

        ContentRepository repository = contentRepositoryFactory.getRepository(storageProvider);
        String filePath = generateFilePath(operationId, file.getOriginalFilename());
        try {
            String storageKey = repository.saveFile(file.getInputStream(), filePath);
            log.info(SAVED_BULK_OPERATION_CSV_FILE_FOR_OPERATION, operationId, storageKey);
            return storageKey;
        } catch (Exception e) {
            log.error(FAILED_TO_SAVE_BULK_OPERATION_CSV_FILE_FOR_OPERATION, operationId, e);
            throw exceptionFactory.storageSaveFailedException(operationId, e);
        }
    }

    public InputStream fetchFile(String storageKey) {
        ValidationUtils.requireNonNullOrEmpty(storageKey, () -> exceptionFactory.storageKeyRequiredException());

        ContentRepository repository = contentRepositoryFactory.getRepository(storageProvider);
        try {
            return repository.fetchFile(storageKey);
        } catch (Exception e) {
            log.error(FAILED_TO_FETCH_BULK_OPERATION_CSV_FILE_FROM_STORAGE, storageKey, e);
            throw exceptionFactory.storageFetchFailedException(storageKey, e);
        }
    }

    public void deleteFile(String storageKey) {
        if (ValidationUtils.isNullOrEmpty(storageKey)) {
            return;
        }
        try {
            ContentRepository repository = contentRepositoryFactory.getRepository(storageProvider);
            repository.deleteFile(storageKey);
            log.info(DELETED_BULK_OPERATION_CSV_FILE_FROM_STORAGE, storageKey);
        } catch (Exception e) {
            log.warn(FAILED_TO_DELETE_BULK_OPERATION_CSV_FILE_FROM_STORAGE, storageKey, e);
        }
    }

    public String saveCsvContent(String csvContent, UUID operationId, String fileName) {
        ValidationUtils.requireNonNull(operationId, () -> exceptionFactory.operationIdRequiredException());
        ValidationUtils.requireNonNullOrEmpty(csvContent, () -> exceptionFactory.storageCsvContentRequiredException());

        ContentRepository repository = contentRepositoryFactory.getRepository(storageProvider);
        String filePath = generateFilePath(operationId, fileName);
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
            String storageKey = repository.saveFile(inputStream, filePath);
            log.info(SAVED_CSV_REPORT_FOR_OPERATION, operationId, storageKey);
            return storageKey;
        } catch (Exception e) {
            log.error(FAILED_TO_SAVE_CSV_REPORT_FOR_OPERATION, operationId, e);
            throw exceptionFactory.storageSaveReportFailedException(operationId, e);
        }
    }

    /**
     * Saves validation errors to object storage as JSON. Returns storage key to store on BulkOperation
     * (e.g. validationErrorsStorageKey). Caller persists the key; nothing is stored in metadata.
     */
    public String saveValidationErrors(UUID operationId, List<CsvValidationError> errors) {
        if (ValidationUtils.isNullOrEmpty(errors)) {
            return null;
        }
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (CsvValidationError err : errors) {
                list.add(err.toMap());
            }
            String json = objectMapper.writeValueAsString(list);
            return saveCsvContent(json, operationId, VALIDATION_ERRORS_FILE_NAME);
        } catch (Exception e) {
            log.error("Failed to save validation errors for operation {}: {}", operationId, e.getMessage(), e);
            throw exceptionFactory.storageSaveReportFailedException(operationId, e);
        }
    }

    /**
     * Fetches validation errors from object storage. Returns empty list if key is null/empty or file cannot be read.
     */
    public List<CsvValidationError> fetchValidationErrors(String storageKey) {
        if (ValidationUtils.isNullOrEmpty(storageKey)) {
            return Collections.emptyList();
        }
        try (InputStream in = fetchFile(storageKey)) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<>() {});
            return CsvValidationError.fromMapList(list != null ? list : Collections.emptyList());
        } catch (Exception e) {
            log.warn("Failed to fetch validation errors from storage {}: {}", storageKey, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String generateFilePath(UUID operationId, String originalFileName) {
        String sanitizedFileName = ValidationUtils.isNullOrEmpty(originalFileName)
                ? DEFAULT_FILE_NAME
                : originalFileName.replaceAll(SANITIZE_REGEX, "_");
        return String.format(PATH_PREFIX, operationId, sanitizedFileName);
    }
}
