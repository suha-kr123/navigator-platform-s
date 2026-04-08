package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterOperationException;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeValueMasterServiceImplTest {

    @Mock
    private MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;

    @Mock
    private MasterCodeRepositoryWrapper masterCodeRepositoryWrapper;

    @Mock
    private DocumentWriteService documentWriteService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CodeValueMasterServiceImpl codeValueMasterService;

    private static final String TEST_MASTER_CODE_KEY = "LOAN_TYPE";
    private static final String TEST_VALUE_KEY = "HOME_LOAN_MASTER_CODE_VALUE";

    private MasterCode masterCode;
    private MasterCodeValue masterCodeValue;

    @BeforeEach
    void setUp() {
        masterCode = MasterCode.builder()
                .id(1L)
                .key(TEST_MASTER_CODE_KEY)
                .name(MasterLanguageData.builder().defaultValue("Loan Type").build())
                .description(MasterLanguageData.builder().defaultValue("Types of loans").build())
                .isSystemDefined(false)
                .build();

        masterCodeValue = MasterCodeValue.builder()
                .id(10L)
                .key(TEST_VALUE_KEY)
                .codeKey(TEST_MASTER_CODE_KEY)
                .value(MasterLanguageData.builder().defaultValue("Home Loan").build())
                .description(MasterLanguageData.builder().defaultValue("Home loan desc").build())
                .isActive(true)
                .displayOrder(0)
                .build();
    }

    // ── getByKey ─────────────────────────────────────────────────────

    @Test
    void getByKey_existingKey_returnsCodeValueResponse() {
        when(masterCodeValueRepositoryWrapper.findByKeyWithException(TEST_VALUE_KEY)).thenReturn(masterCodeValue);

        CodeValueResponse result = codeValueMasterService.getByKey(TEST_VALUE_KEY);

        assertNotNull(result, "Response should not be null for an existing key");
        assertEquals(TEST_VALUE_KEY, result.getKey(), "Returned key should match the requested key");
        verify(masterCodeValueRepositoryWrapper).findByKeyWithException(TEST_VALUE_KEY);
    }

    // ── getByKeyOrNull ───────────────────────────────────────────────

    @Test
    void getByKeyOrNull_validKeyFound_returnsResponse() {
        when(masterCodeValueRepositoryWrapper.findByKey(TEST_VALUE_KEY))
                .thenReturn(Optional.of(masterCodeValue));

        CodeValueResponse result = codeValueMasterService.getByKeyOrNull(TEST_VALUE_KEY);

        assertNotNull(result, "Should return a response when key is found");
        assertEquals(TEST_VALUE_KEY, result.getKey(), "Returned key should match the requested key");
    }

    @Test
    void getByKeyOrNull_nullKey_returnsNull() {
        CodeValueResponse result = codeValueMasterService.getByKeyOrNull(null);

        assertNull(result, "Should return null when key is null");
        verifyNoInteractions(masterCodeValueRepositoryWrapper);
    }

    @Test
    void getByKeyOrNull_blankKey_returnsNull() {
        CodeValueResponse result = codeValueMasterService.getByKeyOrNull("   ");

        assertNull(result, "Should return null when key is blank");
        verifyNoInteractions(masterCodeValueRepositoryWrapper);
    }

    @Test
    void getByKeyOrNull_keyNotFound_returnsNull() {
        when(masterCodeValueRepositoryWrapper.findByKey("NONEXISTENT")).thenReturn(Optional.empty());

        CodeValueResponse result = codeValueMasterService.getByKeyOrNull("NONEXISTENT");

        assertNull(result, "Should return null when key does not exist in the repository");
    }

    // ── getByKeys ────────────────────────────────────────────────────

    @Test
    void getByKeys_multipleKeys_returnsAllResponses() {
        MasterCodeValue secondValue = MasterCodeValue.builder()
                .id(11L).key("SECOND_KEY").codeKey(TEST_MASTER_CODE_KEY)
                .value(MasterLanguageData.builder().defaultValue("Second").build())
                .description(MasterLanguageData.builder().defaultValue("Second desc").build())
                .isActive(true).displayOrder(1).build();

        when(masterCodeValueRepositoryWrapper.findByKeyWithException(TEST_VALUE_KEY)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.findByKeyWithException("SECOND_KEY")).thenReturn(secondValue);

        List<CodeValueResponse> result = codeValueMasterService.getByKeys(
                List.of(TEST_VALUE_KEY, "SECOND_KEY"));

        assertEquals(2, result.size(), "Should return a response for each requested key");
        assertEquals(TEST_VALUE_KEY, result.get(0).getKey(), "First response key should match");
        assertEquals("SECOND_KEY", result.get(1).getKey(), "Second response key should match");
    }

    // ── getCodeValueByKeyAndCodeKey ──────────────────────────────────

    @Test
    void getCodeValueByKeyAndCodeKey_existingKeyAndCodeKey_returnsResponse() {
        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(TEST_VALUE_KEY, TEST_MASTER_CODE_KEY))
                .thenReturn(masterCodeValue);

        CodeValueResponse result = codeValueMasterService.getCodeValueByKeyAndCodeKey(
                TEST_VALUE_KEY, TEST_MASTER_CODE_KEY);

        assertNotNull(result, "Response should not be null for an existing key and code key combination");
        assertEquals(TEST_VALUE_KEY, result.getKey(), "Returned key should match");
        assertEquals(TEST_MASTER_CODE_KEY, result.getCodeKey(), "Returned code key should match");
    }

    // ── getCodeValueByKeysAndCodeKey ─────────────────────────────────

    @Test
    void getCodeValueByKeysAndCodeKey_multipleKeys_returnsAllResponses() {
        MasterCodeValue secondValue = MasterCodeValue.builder()
                .id(11L).key("SECOND_KEY").codeKey(TEST_MASTER_CODE_KEY)
                .value(MasterLanguageData.builder().defaultValue("Second").build())
                .description(MasterLanguageData.builder().defaultValue("Second desc").build())
                .isActive(true).displayOrder(1).build();

        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(TEST_VALUE_KEY, TEST_MASTER_CODE_KEY))
                .thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException("SECOND_KEY", TEST_MASTER_CODE_KEY))
                .thenReturn(secondValue);

        List<CodeValueResponse> result = codeValueMasterService.getCodeValueByKeysAndCodeKey(
                List.of(TEST_VALUE_KEY, "SECOND_KEY"), TEST_MASTER_CODE_KEY);

        assertEquals(2, result.size(), "Should return a response for each requested key and code key");
    }

    // ── createMasterCodeValues ───────────────────────────────────────

    @Test
    void createMasterCodeValues_happyPath_createsAndReturnsResponse() {
        MasterCodeValueRequest valueRequest = MasterCodeValueRequest.builder()
                .valueMap(Map.of("default", "New Value"))
                .descriptionMap(Map.of("default", "New desc"))
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_MASTER_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findAllKeysWithException()).thenReturn(new HashSet<>());
        when(masterCodeValueRepositoryWrapper.saveAllWithException(anyList()))
                .thenReturn(List.of(masterCodeValue));

        MasterCodeValueResponse result = codeValueMasterService.createMasterCodeValues(
                TEST_MASTER_CODE_KEY, List.of(valueRequest));

        assertNotNull(result, "Create response should not be null");
        assertEquals(TEST_MASTER_CODE_KEY, result.getKey(),
                "Response key should match the master code key");
        verify(masterCodeValueRepositoryWrapper).saveAllWithException(anyList());
    }

    @Test
    void createMasterCodeValues_multipleValues_savesAll() {
        MasterCodeValueRequest req1 = MasterCodeValueRequest.builder()
                .valueMap(Map.of("default", "Val1"))
                .descriptionMap(Map.of("default", "Desc1"))
                .build();
        MasterCodeValueRequest req2 = MasterCodeValueRequest.builder()
                .valueMap(Map.of("default", "Val2"))
                .descriptionMap(Map.of("default", "Desc2"))
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_MASTER_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findAllKeysWithException()).thenReturn(new HashSet<>());
        when(masterCodeValueRepositoryWrapper.saveAllWithException(anyList()))
                .thenReturn(List.of(masterCodeValue));

        MasterCodeValueResponse result = codeValueMasterService.createMasterCodeValues(
                TEST_MASTER_CODE_KEY, List.of(req1, req2));

        assertNotNull(result, "Response should not be null when creating multiple values");
        verify(masterCodeValueRepositoryWrapper).saveAllWithException(argThat(list -> list.size() == 2));
    }

    // ── enableDisableMasterCodeValue ─────────────────────────────────

    @Test
    void enableDisableMasterCodeValue_activeValue_togglesToInactive() {
        masterCodeValue.setIsActive(true);

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_MASTER_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByKeyWithException(TEST_VALUE_KEY)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_MASTER_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        codeValueMasterService.enableDisableMasterCodeValue(TEST_MASTER_CODE_KEY, TEST_VALUE_KEY);

        assertFalse(masterCodeValue.getIsActive(),
                "Active value should be toggled to inactive");
        verify(masterCodeValueRepositoryWrapper).saveWithException(masterCodeValue);
    }

    @Test
    void enableDisableMasterCodeValue_inactiveValue_togglesToActive() {
        masterCodeValue.setIsActive(false);

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_MASTER_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByKeyWithException(TEST_VALUE_KEY)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_MASTER_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        codeValueMasterService.enableDisableMasterCodeValue(TEST_MASTER_CODE_KEY, TEST_VALUE_KEY);

        assertTrue(masterCodeValue.getIsActive(),
                "Inactive value should be toggled to active");
    }

    @Test
    void enableDisableMasterCodeValue_returnsFullResponse() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_MASTER_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByKeyWithException(TEST_VALUE_KEY)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_MASTER_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        MasterCodeValueResponse result = codeValueMasterService.enableDisableMasterCodeValue(
                TEST_MASTER_CODE_KEY, TEST_VALUE_KEY);

        assertNotNull(result, "Toggle response should not be null");
        assertNotNull(result.getChildren(), "Response children (values) should not be null");
        assertFalse(result.getChildren().isEmpty(),
                "Response should include code values after toggle");
    }

    // ── uploadIcon ───────────────────────────────────────────────────

    @Test
    void uploadIcon_nullFile_throwsException() {
        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("default")
                        .size("medium")
                        .build();

        assertThrows(CodeMasterOperationException.class,
                () -> codeValueMasterService.uploadIcon(request, null),
                "Null file should throw CodeMasterOperationException");
    }

    @Test
    void uploadIcon_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("default")
                        .size("medium")
                        .build();

        assertThrows(CodeMasterOperationException.class,
                () -> codeValueMasterService.uploadIcon(request, file),
                "Empty file should throw CodeMasterOperationException");
    }

    @Test
    void uploadIcon_invalidContentType_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("default")
                        .size("medium")
                        .build();

        assertThrows(CodeMasterOperationException.class,
                () -> codeValueMasterService.uploadIcon(request, file),
                "Non-image content type should throw CodeMasterOperationException");
    }

    @Test
    void uploadIcon_blankIconBaseUrl_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");

        ReflectionTestUtils.setField(codeValueMasterService, "iconBaseUrl", "");

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("default")
                        .size("medium")
                        .build();

        assertThrows(CodeMasterOperationException.class,
                () -> codeValueMasterService.uploadIcon(request, file),
                "Blank icon base URL should throw CodeMasterOperationException");
    }

    @Test
    void uploadIcon_happyPath_savesIconAndReturnsResponse() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("icon.png");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ReflectionTestUtils.setField(codeValueMasterService, "iconBaseUrl", "https://cdn.example.com/icons");

        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(TEST_VALUE_KEY, TEST_MASTER_CODE_KEY))
                .thenReturn(masterCodeValue);
        when(documentWriteService.createDocument(
                any(com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream.class)))
                .thenReturn(DocumentCreateResponse.builder().id(100L).build());
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("default")
                        .size("medium")
                        .build();

        CodeValueResponse result = codeValueMasterService.uploadIcon(request, file);

        assertNotNull(result, "Upload response should not be null on successful icon upload");
        verify(documentWriteService).createDocument(
                any(com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream.class));
        verify(masterCodeValueRepositoryWrapper).saveWithException(masterCodeValue);
    }

    @Test
    void uploadIcon_existingIcon_deletesOldDocumentBeforeUpload() throws IOException {
        MasterCodeValue.IconAsset existingAsset = MasterCodeValue.IconAsset.builder()
                .url("https://cdn.example.com/icons/old.png").documentId(50L).build();
        MasterCodeValue.IconSizeData sizeData = MasterCodeValue.IconSizeData.builder()
                .medium(existingAsset).build();
        MasterCodeValue.IconsData iconsData = MasterCodeValue.IconsData.builder()
                .defaultIcon(sizeData).build();
        masterCodeValue.setIcons(iconsData);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("new-icon.png");
        when(file.getSize()).thenReturn(2048L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ReflectionTestUtils.setField(codeValueMasterService, "iconBaseUrl", "https://cdn.example.com/icons");

        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(TEST_VALUE_KEY, TEST_MASTER_CODE_KEY))
                .thenReturn(masterCodeValue);
        when(documentWriteService.createDocument(
                any(com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream.class)))
                .thenReturn(DocumentCreateResponse.builder().id(101L).build());
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("default")
                        .size("medium")
                        .build();

        codeValueMasterService.uploadIcon(request, file);

        verify(documentWriteService).deleteDocumentById(50L);
        verify(documentWriteService).createDocument(
                any(com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream.class));
    }

    @Test
    void uploadIcon_fileWithNoExtension_defaultsToPng() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("iconfile");
        when(file.getSize()).thenReturn(512L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ReflectionTestUtils.setField(codeValueMasterService, "iconBaseUrl", "https://cdn.example.com/icons");

        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(TEST_VALUE_KEY, TEST_MASTER_CODE_KEY))
                .thenReturn(masterCodeValue);
        when(documentWriteService.createDocument(
                any(com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream.class)))
                .thenReturn(DocumentCreateResponse.builder().id(102L).build());
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("crm")
                        .size("large")
                        .build();

        CodeValueResponse result = codeValueMasterService.uploadIcon(request, file);

        assertNotNull(result, "Response should not be null even when file has no extension");
        verify(masterCodeValueRepositoryWrapper).saveWithException(masterCodeValue);
    }

    @Test
    void uploadIcon_nullOriginalFilename_defaultsToPng() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getSize()).thenReturn(512L);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        ReflectionTestUtils.setField(codeValueMasterService, "iconBaseUrl", "https://cdn.example.com/icons");

        when(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(TEST_VALUE_KEY, TEST_MASTER_CODE_KEY))
                .thenReturn(masterCodeValue);
        when(documentWriteService.createDocument(
                any(com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream.class)))
                .thenReturn(DocumentCreateResponse.builder().id(103L).build());
        when(masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue)).thenReturn(masterCodeValue);

        com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest request =
                com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest.builder()
                        .masterCodeKey(TEST_MASTER_CODE_KEY)
                        .masterCodeValueKey(TEST_VALUE_KEY)
                        .context("web")
                        .size("small")
                        .build();

        CodeValueResponse result = codeValueMasterService.uploadIcon(request, file);

        assertNotNull(result, "Response should not be null even when original filename is null");
    }
}
