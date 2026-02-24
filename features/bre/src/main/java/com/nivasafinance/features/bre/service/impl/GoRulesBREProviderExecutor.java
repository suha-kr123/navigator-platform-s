package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.service.BREProviderExecutor;
import com.nivasafinance.features.document.service.DocumentReadService;
import io.gorules.zen_engine.JsonBuffer;
import io.gorules.zen_engine.ZenDecision;
import io.gorules.zen_engine.ZenEngine;
import io.gorules.zen_engine.ZenEngineResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoRulesBREProviderExecutor implements BREProviderExecutor {

    private final DocumentReadService documentReadService;

    @Override
    public String evaluate(BREConfigs config, String inputJson) {
        BREConfigs.GoRulesProviderDetails goRules = config.getConfigs() != null
                ? config.getConfigs().getGoRulesProviderDetails()
                : null;
        Long ruleJsonFileId = goRules != null ? goRules.getRuleJsonFileId() : null;

        if (ruleJsonFileId == null) {
            return "{\"error\":\"No rule file linked to BRE config\"}";
        }

        byte[] ruleBytes;
        var fileResponse = documentReadService.getDocumentFile(ruleJsonFileId);
        if (fileResponse == null || fileResponse.getFile() == null) {
            return "{\"error\":\"Rule document or file stream not found\"}";
        }
        try (InputStream is = fileResponse.getFile()) {
            ruleBytes = is.readAllBytes();
        } catch (Exception e) {
            log.error("Failed to read rule file for config {}", config.getUname(), e);
            return "{\"error\":\"Failed to read rule file: " + e.getMessage() + "\"}";
        }

        try (ZenEngine engine = new ZenEngine(null, null)) {
            ZenDecision decision = engine.createDecision(new JsonBuffer(ruleBytes));
            ZenEngineResponse response = decision.evaluate(new JsonBuffer(inputJson), null).join();
            return response.result().toString();
        } catch (Exception e) {
            log.error("GoRules evaluation failed for config {}", config.getUname(), e);
            throw new RuntimeException("GoRules evaluation failed: " + e.getMessage(), e);
        }
    }
}
