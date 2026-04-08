package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.document.dto.DocumentFileResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoRulesBREProviderExecutorTest {

    @Mock
    private DocumentReadService documentReadService;

    @InjectMocks
    private GoRulesBREProviderExecutor executor;

    // ── evaluate: null guard paths ──

    @Test
    void evaluate_whenConfigsIsNull_returnsErrorJson() {
        BREConfigs config = BREConfigs.builder().uname("cfg").configs(null).build();

        String result = executor.evaluate(config, "{}");

        assertTrue(result.contains("No rule file linked"),
                "Should return error JSON when configs is null");
    }

    @Test
    void evaluate_whenGoRulesDetailsIsNull_returnsErrorJson() {
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(BREProvider.GORULES)
                .goRulesProviderDetails(null)
                .build();
        BREConfigs config = BREConfigs.builder().uname("cfg").configs(configs).build();

        String result = executor.evaluate(config, "{}");

        assertTrue(result.contains("No rule file linked"),
                "Should return error JSON when GoRules provider details is null");
    }

    @Test
    void evaluate_whenRuleJsonFileIdIsNull_returnsErrorJson() {
        BREConfigs.GoRulesProviderDetails details = new BREConfigs.GoRulesProviderDetails(null);
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(BREProvider.GORULES)
                .goRulesProviderDetails(details)
                .build();
        BREConfigs config = BREConfigs.builder().uname("cfg").configs(configs).build();

        String result = executor.evaluate(config, "{}");

        assertTrue(result.contains("No rule file linked"),
                "Should return error JSON when rule file ID is null");
    }

    // ── evaluate: document retrieval paths ──

    @Test
    void evaluate_whenFileResponseIsNull_returnsErrorJson() {
        BREConfigs config = buildConfigWithRuleFileId(42L);
        when(documentReadService.getDocumentFile(42L)).thenReturn(null);

        String result = executor.evaluate(config, "{}");

        assertTrue(result.contains("Rule document or file stream not found"),
                "Should return error JSON when document file response is null");
    }

    @Test
    void evaluate_whenFileStreamIsNull_returnsErrorJson() {
        BREConfigs config = buildConfigWithRuleFileId(42L);
        DocumentFileResponse fileResponse = new DocumentFileResponse();
        fileResponse.setFile(null);
        when(documentReadService.getDocumentFile(42L)).thenReturn(fileResponse);

        String result = executor.evaluate(config, "{}");

        assertTrue(result.contains("Rule document or file stream not found"),
                "Should return error JSON when file input stream is null");
    }

    @Test
    void evaluate_whenFileReadThrowsIOException_returnsErrorJson() {
        BREConfigs config = buildConfigWithRuleFileId(42L);
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("disk failure");
            }
        };
        DocumentFileResponse fileResponse = new DocumentFileResponse();
        fileResponse.setFile(failingStream);
        when(documentReadService.getDocumentFile(42L)).thenReturn(fileResponse);

        String result = executor.evaluate(config, "{}");

        assertTrue(result.contains("Failed to read rule file"),
                "Should return error JSON when reading the rule file stream fails");
    }

    // ── helper ──

    private BREConfigs buildConfigWithRuleFileId(Long ruleFileId) {
        BREConfigs.GoRulesProviderDetails details = new BREConfigs.GoRulesProviderDetails(ruleFileId);
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(BREProvider.GORULES)
                .goRulesProviderDetails(details)
                .build();
        return BREConfigs.builder().uname("cfg").configs(configs).build();
    }
}
