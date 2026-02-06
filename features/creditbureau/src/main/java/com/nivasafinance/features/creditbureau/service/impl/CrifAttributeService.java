package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nivasafinance.features.creditbureau.entity.CreditBureauAttribute;
import com.nivasafinance.features.creditbureau.enums.CbAttributeCategory;
import com.nivasafinance.features.creditbureau.repository.CreditBureauAttributeRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrifAttributeService {

    private static final int ATTR_NAME_MAX_LENGTH = 100;
    private static final int ATTR_VALUE_MAX_LENGTH = 100;

    private final CreditBureauAttributeRepositoryWrapper creditBureauAttributeRepositoryWrapper;

    public void parseAndStore(Long enquiryId, JsonNode reportData) {
        try {
            log.info("Parsing credit bureau attributes for enquiry ID: {}", enquiryId);

            JsonNode accountsSummary = reportData.path("ACCOUNTS-SUMMARY");
            if (accountsSummary.isMissingNode()) {
                log.debug("ACCOUNTS-SUMMARY not found for enquiry ID: {}", enquiryId);
                return;
            }

            creditBureauAttributeRepositoryWrapper.deleteByEnquiryId(enquiryId);

            List<CreditBureauAttribute> attributes = new ArrayList<>();

            parseAttributeArray(
                    accountsSummary.path("ADDITIONAL-SUMMARY"),
                    enquiryId,
                    CbAttributeCategory.ADDITIONAL_SUMMARY_ATTRIBUTES,
                    attributes);

            parseAttributeArray(
                    accountsSummary.path("PERFORM-ATTRIBUTES"),
                    enquiryId,
                    CbAttributeCategory.PERFORM_ATTRIBUTES,
                    attributes);

            if (!attributes.isEmpty()) {
                creditBureauAttributeRepositoryWrapper.saveAllWithException(attributes);
                log.info("Parsed and stored {} attributes for enquiry ID: {}", attributes.size(), enquiryId);
            }
        } catch (Exception e) {
            log.error("Failed to parse credit bureau attributes for enquiry ID: {}", enquiryId, e);
            throw new RuntimeException("Failed to parse credit bureau attributes for enquiry ID: " + enquiryId, e);
        }
    }

    private void parseAttributeArray(
            JsonNode arrayNode,
            Long enquiryId,
            CbAttributeCategory category,
            List<CreditBureauAttribute> attributes) {
        if (!arrayNode.isArray()) {
            return;
        }
        for (JsonNode item : arrayNode) {
            String attrName = getTextValue(item, "ATTR-NAME");
            if (attrName == null || attrName.isBlank()) {
                continue;
            }
            String attrValue = getTextValue(item, "ATTR-VALUE");
            CreditBureauAttribute attribute = CreditBureauAttribute.builder()
                    .enquiryId(enquiryId)
                    .category(category)
                    .attrName(truncate(attrName, ATTR_NAME_MAX_LENGTH))
                    .attrValue(truncate(attrValue, ATTR_VALUE_MAX_LENGTH))
                    .build();
            attributes.add(attribute);
        }
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText(null);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
