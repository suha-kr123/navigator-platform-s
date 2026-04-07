package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.dto.CbDerivedQueryConfig;
import com.nivasafinance.features.creditbureau.entity.CreditBureauDerivedAttribute;
import com.nivasafinance.features.creditbureau.repository.CbConfigRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauDerivedAttributeRepositoryWrapper;
import com.nivasafinance.features.creditbureau.service.CreditBureauDerivedAttributeWriteService;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditBureauDerivedAttributeWriteServiceImpl implements CreditBureauDerivedAttributeWriteService {

    static final String CB_DERIVED_QUERIES_KEY = "CB_DERIVED_QUERIES";

    private final CbConfigRepositoryWrapper cbConfigRepositoryWrapper;
    private final CreditBureauDerivedAttributeRepositoryWrapper cbDerivedAttributeRepositoryWrapper;
    private final DataProviderExecutor dataProviderExecutor;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void saveDerivedAttributesForEnquiry(Long enquiryId, Long leadId) {
        List<CbDerivedQueryConfig> queryConfigs = loadDerivedQueryConfigs();
        if (queryConfigs.isEmpty()) {
            log.warn("No {} config or empty list; skipping derived attributes for enquiry {}", CB_DERIVED_QUERIES_KEY, enquiryId);
            return;
        }

        Map<String, Object> dataExt = leadId != null ? Map.of("leadId", leadId) : Collections.emptyMap();

        Map<String, Object> parameters = Map.of("enquiryId", enquiryId);

        List<CompletableFuture<List<Map<String, Object>>>> futures = new ArrayList<>();
        for (CbDerivedQueryConfig cfg : queryConfigs) {
            if (cfg.getProviderName() == null || cfg.getProviderName().isBlank()) {
                continue;
            }
            CompletableFuture<List<Map<String, Object>>> future = CompletableFuture
                    .supplyAsync(() -> dataProviderExecutor.executeDataProviderForList(cfg.getProviderName(), parameters))
                    .exceptionally(ex -> {
                        log.error("Data provider query failed for enquiryId {}, provider {}", enquiryId, cfg.getProviderName(), ex);
                        return List.of();
                    });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

        List<CreditBureauDerivedAttribute> toSave = new ArrayList<>();
        for (CompletableFuture<List<Map<String, Object>>> future : futures) {
            for (Map<String, Object> row : future.join()) {
                CreditBureauDerivedAttribute entity = mapRowToEntity(enquiryId, row, dataExt);
                if (entity != null) {
                    toSave.add(entity);
                }
            }
        }

        if (!toSave.isEmpty()) {
            cbDerivedAttributeRepositoryWrapper.saveAllWithException(toSave);
            log.info("Saved {} derived attribute row(s) for enquiry {}", toSave.size(), enquiryId);
        }
    }

    private List<CbDerivedQueryConfig> loadDerivedQueryConfigs() {
        return cbConfigRepositoryWrapper.findByConfigKey(CB_DERIVED_QUERIES_KEY)
                .map(config -> {
                    try {
                        String json = config.getConfigValue();
                        if (json == null || json.isBlank()) {
                            return List.<CbDerivedQueryConfig>of();
                        }
                        List<CbDerivedQueryConfig> list =
                                objectMapper.readValue(json, new TypeReference<List<CbDerivedQueryConfig>>() { });
                        return list != null ? list : List.<CbDerivedQueryConfig>of();
                    } catch (Exception e) {
                        log.error("Failed to parse {} config_value", CB_DERIVED_QUERIES_KEY, e);
                        return List.<CbDerivedQueryConfig>of();
                    }
                })
                .orElse(List.<CbDerivedQueryConfig>of());
    }

    private CreditBureauDerivedAttribute mapRowToEntity(Long enquiryId, Map<String, Object> row, Map<String, Object> dataExt) {
        String attrName = firstString(row, "attr_name", "attrName", "attribute_name");
        if (attrName == null || attrName.isBlank()) {
            log.warn("Skipping row without attr_name/attribute_name for enquiry {}", enquiryId);
            return null;
        }
        String attrValue = firstString(row, "attr_value", "attrValue", "attribute_value");
        return CreditBureauDerivedAttribute.builder()
                .enquiryId(enquiryId)
                .attrName(attrName.trim())
                .attrValue(attrValue != null ? attrValue : null)
                .dataExt(dataExt.isEmpty() ? null : dataExt)
                .build();
    }

    private static String firstString(Map<String, Object> row, String... keys) {
        for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            for (String wanted : keys) {
                if (wanted.equalsIgnoreCase(e.getKey())) {
                    return Objects.toString(e.getValue(), null);
                }
            }
        }
        return null;
    }
}
