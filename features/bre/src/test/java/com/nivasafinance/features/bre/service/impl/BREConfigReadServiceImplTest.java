package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigResponse;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.bre.repository.BREConfigRepositoryWrapper;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BREConfigReadServiceImplTest {

    @Mock
    private BREConfigRepositoryWrapper breConfigRepositoryWrapper;

    @Mock
    private DocumentReadService documentReadService;

    @InjectMocks
    private BREConfigReadServiceImpl service;

    // ── getAllBREConfigs ──

    @Test
    void getAllBREConfigs_whenConfigsExist_returnsMappedPaginatedResponse() {
        BREConfigs entity = buildConfigEntity("cfg-1", BREProvider.GORULES);
        PaginationInfo paginationInfo = new PaginationInfo(0, 10, 1L, 1, 0, false, false);
        PaginatedResponse<BREConfigs> paginatedEntities =
                new PaginatedResponse<>(List.of(entity), paginationInfo);
        PaginationRequest request = new PaginationRequest();
        when(breConfigRepositoryWrapper.findAllWithException(request)).thenReturn(paginatedEntities);

        PaginatedResponse<BREConfigResponse> result = service.getAllBREConfigs(request);

        assertEquals(1, result.getContent().size(),
                "Should return one mapped config response");
        assertEquals("cfg-1", result.getContent().get(0).getUname(),
                "Mapped response should preserve the entity uname");
    }

    @Test
    void getAllBREConfigs_whenNoConfigs_returnsEmptyContent() {
        PaginationInfo paginationInfo = new PaginationInfo(0, 10, 0L, 0, 0, false, false);
        PaginatedResponse<BREConfigs> paginatedEntities =
                new PaginatedResponse<>(List.of(), paginationInfo);
        PaginationRequest request = new PaginationRequest();
        when(breConfigRepositoryWrapper.findAllWithException(request)).thenReturn(paginatedEntities);

        PaginatedResponse<BREConfigResponse> result = service.getAllBREConfigs(request);

        assertTrue(result.getContent().isEmpty(),
                "Should return empty content when no configs exist");
    }

    // ── getBREConfigByUname ──

    @Test
    void getBREConfigByUname_whenConfigHasRuleFile_returnsResponseWithDocument() {
        BREConfigs entity = buildConfigEntityWithRuleFile("cfg-1", BREProvider.GORULES, 42L);
        DocumentResponse docResponse = new DocumentResponse();
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-1")).thenReturn(entity);
        when(documentReadService.getDocumentById(42L)).thenReturn(docResponse);

        BREConfigDetailedResponse result = service.getBREConfigByUname("cfg-1");

        assertEquals("cfg-1", result.getUname(),
                "Response should have the requested uname");
        assertNotNull(result.getRuleFileDocument(),
                "Response should include the rule file document when file ID exists");
        verify(documentReadService).getDocumentById(42L);
    }

    @Test
    void getBREConfigByUname_whenConfigHasNoRuleFile_returnsResponseWithoutDocument() {
        BREConfigs entity = buildConfigEntity("cfg-2", BREProvider.GORULES);
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-2")).thenReturn(entity);

        BREConfigDetailedResponse result = service.getBREConfigByUname("cfg-2");

        assertEquals("cfg-2", result.getUname(),
                "Response should have the requested uname");
        assertNull(result.getRuleFileDocument(),
                "Response should not include a rule file document when no file ID is configured");
        verifyNoInteractions(documentReadService);
    }

    @Test
    void getBREConfigByUname_whenConfigsIsNull_returnsResponseWithoutDocument() {
        BREConfigs entity = BREConfigs.builder().uname("cfg-3").configs(null).build();
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-3")).thenReturn(entity);

        BREConfigDetailedResponse result = service.getBREConfigByUname("cfg-3");

        assertNull(result.getRuleFileDocument(),
                "Response should not include a rule file document when configs is null");
        verifyNoInteractions(documentReadService);
    }

    @Test
    void getBREConfigByUname_whenGoRulesDetailsIsNull_returnsResponseWithoutDocument() {
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(BREProvider.GORULES)
                .goRulesProviderDetails(null)
                .build();
        BREConfigs entity = BREConfigs.builder().uname("cfg-4").configs(configs).build();
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-4")).thenReturn(entity);

        BREConfigDetailedResponse result = service.getBREConfigByUname("cfg-4");

        assertNull(result.getRuleFileDocument(),
                "Response should not include a rule file document when GoRules details is null");
        verifyNoInteractions(documentReadService);
    }

    @Test
    void getBREConfigByUname_whenRuleJsonFileIdIsNull_returnsResponseWithoutDocument() {
        BREConfigs.GoRulesProviderDetails details = new BREConfigs.GoRulesProviderDetails(null);
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(BREProvider.GORULES)
                .goRulesProviderDetails(details)
                .build();
        BREConfigs entity = BREConfigs.builder().uname("cfg-5").configs(configs).build();
        when(breConfigRepositoryWrapper.findByUnameWithException("cfg-5")).thenReturn(entity);

        BREConfigDetailedResponse result = service.getBREConfigByUname("cfg-5");

        assertNull(result.getRuleFileDocument(),
                "Response should not include a rule file document when ruleJsonFileId is null");
        verifyNoInteractions(documentReadService);
    }

    // ── helpers ──

    private BREConfigs buildConfigEntity(String uname, BREProvider provider) {
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(provider)
                .build();
        return BREConfigs.builder().uname(uname).configs(configs).build();
    }

    private BREConfigs buildConfigEntityWithRuleFile(String uname, BREProvider provider, Long ruleFileId) {
        BREConfigs.GoRulesProviderDetails details = new BREConfigs.GoRulesProviderDetails(ruleFileId);
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(provider)
                .goRulesProviderDetails(details)
                .build();
        return BREConfigs.builder().uname(uname).configs(configs).build();
    }
}
