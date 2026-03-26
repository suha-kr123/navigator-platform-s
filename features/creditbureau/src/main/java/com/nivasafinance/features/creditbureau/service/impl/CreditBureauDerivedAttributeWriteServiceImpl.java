package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.dto.RedashDerivedQueryConfig;
import com.nivasafinance.features.creditbureau.entity.CreditBureauDerivedAttribute;
import com.nivasafinance.features.creditbureau.repository.CbConfigRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauDerivedAttributeRepositoryWrapper;
import com.nivasafinance.features.creditbureau.service.CreditBureauDerivedAttributeWriteService;
import com.nivasafinance.redash.service.RedashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditBureauDerivedAttributeWriteServiceImpl implements CreditBureauDerivedAttributeWriteService {

    static final String REDASH_CB_DERIVED_QUERIES_KEY = "REDASH_CB_DERIVED_QUERIES";

    private final CbConfigRepositoryWrapper cbConfigRepositoryWrapper;
    private final CreditBureauDerivedAttributeRepositoryWrapper cbDerivedAttributeRepositoryWrapper;
    private final RedashService redashService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void saveDerivedAttributesForEnquiry(Long enquiryId, Long leadId) {
        List<RedashDerivedQueryConfig> queryConfigs = loadDerivedQueryConfigs();
        if (queryConfigs.isEmpty()) {
            log.warn("No {} config or empty list; skipping derived attributes for enquiry {}", REDASH_CB_DERIVED_QUERIES_KEY, enquiryId);
            return;
        }

        Map<String, Object> dataExt = leadId != null ? Map.of("leadId", leadId) : Collections.emptyMap();

        List<CreditBureauDerivedAttribute> toSave = new ArrayList<>();
        Map<String, Object> parameters = Map.of("enquiryId", enquiryId);

        for (RedashDerivedQueryConfig cfg : queryConfigs) {
            if (cfg.getQueryId() == null) {
                log.warn("Skipping derived query config with null queryId for enquiry {}", enquiryId);
                continue;
            }
            try {
                List<Map<String, Object>> rows = redashService.getQueryResultRows(cfg.getQueryId(), parameters);
                for (Map<String, Object> row : rows) {
                    CreditBureauDerivedAttribute entity = mapRowToEntity(enquiryId, row, dataExt);
                    if (entity != null) {
                        toSave.add(entity);
                    }
                }
            } catch (Exception e) {
                log.error("Redash derived query failed for enquiryId {}, queryId {}", enquiryId, cfg.getQueryId(), e);
            }
        }

        if (!toSave.isEmpty()) {
            cbDerivedAttributeRepositoryWrapper.saveAllWithException(toSave);
            log.info("Saved {} derived attribute row(s) for enquiry {}", toSave.size(), enquiryId);
        }
    }

    private List<RedashDerivedQueryConfig> loadDerivedQueryConfigs() {
        return cbConfigRepositoryWrapper.findByConfigKey(REDASH_CB_DERIVED_QUERIES_KEY)
                .map(config -> {
                    try {
                        String json = config.getConfigValue();
                        if (json == null || json.isBlank()) {
                            return List.<RedashDerivedQueryConfig>of();
                        }
                        List<RedashDerivedQueryConfig> list =
                                objectMapper.readValue(json, new TypeReference<List<RedashDerivedQueryConfig>>() { });
                        return list != null ? list : List.<RedashDerivedQueryConfig>of();
                    } catch (Exception e) {
                        log.error("Failed to parse {} config_value", REDASH_CB_DERIVED_QUERIES_KEY, e);
                        return List.<RedashDerivedQueryConfig>of();
                    }
                })
                .orElse(List.<RedashDerivedQueryConfig>of());
    }

    private CreditBureauDerivedAttribute mapRowToEntity(Long enquiryId, Map<String, Object> row, Map<String, Object> dataExt) {
        String attrName = firstString(row, "attr_name", "attrName", "attribute_name");
        if (attrName == null || attrName.isBlank()) {
            log.warn("Skipping Redash row without attr_name/attribute_name for enquiry {}", enquiryId);
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
