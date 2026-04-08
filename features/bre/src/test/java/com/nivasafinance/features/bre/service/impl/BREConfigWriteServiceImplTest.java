package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigRuleFileUploadResponse;
import com.nivasafinance.features.bre.dto.CreateBREConfigRequest;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.bre.repository.BREConfigRepositoryWrapper;
import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.service.DocumentWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BREConfigWriteServiceImplTest {

    @Mock
    private BREConfigRepositoryWrapper breConfigRepositoryWrapper;

    @Mock
    private DocumentWriteService documentWriteService;

    @InjectMocks
    private BREConfigWriteServiceImpl service;

    // ── createBREConfig ──

    @Test
    void createBREConfig_withValidRequest_savesAndReturnsDetailedResponse() {
        CreateBREConfigRequest request = CreateBREConfigRequest.builder()
                .uname("new-config")
                .provider(BREProvider.GORULES)
                .dataProviderId(10L)
                .build();

        BREConfigs savedEntity = BREConfigs.builder()
                .uname("new-config")
                .configs(BREConfigs.Configs.builder()
                        .provider(BREProvider.GORULES)
                        .dataProviderId(10L)
                        .build())
                .build();
        when(breConfigRepositoryWrapper.saveWithException(any(BREConfigs.class))).thenReturn(savedEntity);

        BREConfigDetailedResponse result = service.createBREConfig(request);

        assertEquals("new-config", result.getUname(),
                "Response uname should match the request");
        assertEquals(BREProvider.GORULES, result.getProvider(),
                "Response provider should match the request");
        assertNull(result.getRuleFileDocument(),
                "Rule file document should be null on initial creation");
    }

    @Test
    void createBREConfig_setsGoRulesProviderDetailsToNull() {
        CreateBREConfigRequest request = CreateBREConfigRequest.builder()
                .uname("cfg")
                .provider(BREProvider.GORULES)
                .build();
        when(breConfigRepositoryWrapper.saveWithException(any(BREConfigs.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.createBREConfig(request);

        ArgumentCaptor<BREConfigs> captor = ArgumentCaptor.forClass(BREConfigs.class);
        verify(breConfigRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getConfigs().getGoRulesProviderDetails(),
                "GoRules provider details should be null when creating a new config");
    }

    // ── uploadRuleFile ──

    @Test
    void uploadRuleFile_whenNoExistingRuleFile_uploadsAndReturnsResponse() {
        BREConfigs config = buildConfigWithId(1L, "cfg-1", null);
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-1")).thenReturn(config);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("my-rule.json");

        UUID docIdentifier = UUID.randomUUID();
        DocumentCreateResponse docResponse = new DocumentCreateResponse();
        docResponse.setId(55L);
        docResponse.setIdentifier(docIdentifier);
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(breConfigRepositoryWrapper.saveWithException(any(BREConfigs.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BREConfigRuleFileUploadResponse result = service.uploadRuleFile("cfg-1", file);

        assertEquals(55L, result.getRuleJsonFileId(),
                "Response should contain the new document ID");
        assertEquals(docIdentifier, result.getDocumentIdentifier(),
                "Response should contain the new document identifier");
        verify(documentWriteService, never()).deleteDocumentById(anyLong());
    }

    @Test
    void uploadRuleFile_whenExistingRuleFilePresent_deletesOldAndUploadsNew() {
        BREConfigs config = buildConfigWithId(1L, "cfg-1", 99L);
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-1")).thenReturn(config);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("updated-rule.json");

        DocumentCreateResponse docResponse = new DocumentCreateResponse();
        docResponse.setId(100L);
        docResponse.setIdentifier(UUID.randomUUID());
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(breConfigRepositoryWrapper.saveWithException(any(BREConfigs.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.uploadRuleFile("cfg-1", file);

        verify(documentWriteService).deleteDocumentById(99L);
    }

    @Test
    void uploadRuleFile_whenConfigsIsNull_uploadsWithoutDeletion() {
        BREConfigs config = BREConfigs.builder().uname("cfg-2").configs(null).build();
        setEntityId(config, 2L);
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-2")).thenReturn(config);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);

        DocumentCreateResponse docResponse = new DocumentCreateResponse();
        docResponse.setId(60L);
        docResponse.setIdentifier(UUID.randomUUID());
        when(documentWriteService.createDocument(any(DocumentCreateRequest.class))).thenReturn(docResponse);
        when(breConfigRepositoryWrapper.saveWithException(any(BREConfigs.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BREConfigRuleFileUploadResponse result = service.uploadRuleFile("cfg-2", file);

        assertEquals(60L, result.getRuleJsonFileId(),
                "Should upload new file even when configs is null");
        verify(documentWriteService, never()).deleteDocumentById(anyLong());
    }

    // ── helpers ──

    private BREConfigs buildConfigWithId(Long id, String uname, Long existingRuleFileId) {
        BREConfigs.GoRulesProviderDetails details = existingRuleFileId != null
                ? new BREConfigs.GoRulesProviderDetails(existingRuleFileId)
                : null;
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(BREProvider.GORULES)
                .goRulesProviderDetails(details)
                .build();
        BREConfigs entity = BREConfigs.builder().uname(uname).configs(configs).build();
        setEntityId(entity, id);
        return entity;
    }

    private void setEntityId(BREConfigs entity, Long id) {
        try {
            var field = entity.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID via reflection", e);
        }
    }
}
