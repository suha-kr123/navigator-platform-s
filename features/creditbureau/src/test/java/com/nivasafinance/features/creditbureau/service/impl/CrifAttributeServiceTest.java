package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.entity.CreditBureauAttribute;
import com.nivasafinance.features.creditbureau.enums.CbAttributeCategory;
import com.nivasafinance.features.creditbureau.repository.CreditBureauAttributeRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifAttributeServiceTest {

    @Mock
    private CreditBureauAttributeRepositoryWrapper creditBureauAttributeRepositoryWrapper;

    @InjectMocks
    private CrifAttributeService crifAttributeService;

    private static final Long ENQUIRY_ID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parseAndStore() Tests ====================

    @Test
    void parseAndStore_whenAccountsSummaryMissing_returnsWithoutSaving() throws Exception {
        // Arrange
        JsonNode reportData = mapper.readTree("{}");

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAttributeRepositoryWrapper, never()).deleteByEnquiryId(any());
        verify(creditBureauAttributeRepositoryWrapper, never()).saveAllWithException(anyList());
    }

    @Test
    void parseAndStore_withAdditionalSummaryAttributes_deletesOldAndSavesNew() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "ADDITIONAL-SUMMARY": [
                        {"ATTR-NAME": "TotalAccounts", "ATTR-VALUE": "5"},
                        {"ATTR-NAME": "ActiveAccounts", "ATTR-VALUE": "3"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAttributeRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditBureauAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(creditBureauAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauAttribute> saved = captor.getValue();

        assertEquals(2, saved.size(), "Should save two attributes from ADDITIONAL-SUMMARY");
        assertEquals(CbAttributeCategory.ADDITIONAL_SUMMARY_ATTRIBUTES, saved.get(0).getCategory(),
                "Category should be ADDITIONAL_SUMMARY_ATTRIBUTES");
        assertEquals("TotalAccounts", saved.get(0).getAttrName(), "First attribute name should match");
        assertEquals("5", saved.get(0).getAttrValue(), "First attribute value should match");
    }

    @Test
    void parseAndStore_withPerformAttributes_savesWithCorrectCategory() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "PERFORM-ATTRIBUTES": [
                        {"ATTR-NAME": "Metric1", "ATTR-VALUE": "100"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditBureauAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(creditBureauAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauAttribute> saved = captor.getValue();

        assertEquals(1, saved.size(), "Should save one attribute from PERFORM-ATTRIBUTES");
        assertEquals(CbAttributeCategory.PERFORM_ATTRIBUTES, saved.get(0).getCategory(),
                "Category should be PERFORM_ATTRIBUTES");
    }

    @Test
    void parseAndStore_withBothSections_savesCombinedAttributes() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "ADDITIONAL-SUMMARY": [
                        {"ATTR-NAME": "Attr1", "ATTR-VALUE": "Val1"}
                    ],
                    "PERFORM-ATTRIBUTES": [
                        {"ATTR-NAME": "Attr2", "ATTR-VALUE": "Val2"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditBureauAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(creditBureauAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauAttribute> saved = captor.getValue();

        assertEquals(2, saved.size(), "Should save attributes from both sections");
    }

    @Test
    void parseAndStore_whenAttrNameIsBlank_skipsRow() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "ADDITIONAL-SUMMARY": [
                        {"ATTR-NAME": "", "ATTR-VALUE": "SomeValue"},
                        {"ATTR-NAME": "ValidName", "ATTR-VALUE": "ValidValue"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditBureauAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(creditBureauAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauAttribute> saved = captor.getValue();

        assertEquals(1, saved.size(), "Should skip row with blank ATTR-NAME");
        assertEquals("ValidName", saved.get(0).getAttrName(), "Only the valid attribute should be saved");
    }

    @Test
    void parseAndStore_whenAttrNameExceedsMaxLength_truncatesName() throws Exception {
        // Arrange
        String longName = "A".repeat(150);
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "ADDITIONAL-SUMMARY": [
                        {"ATTR-NAME": "%s", "ATTR-VALUE": "Val"}
                    ]
                }
            }
            """.formatted(longName);
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditBureauAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(creditBureauAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauAttribute> saved = captor.getValue();

        assertEquals(100, saved.get(0).getAttrName().length(),
                "Attribute name should be truncated to 100 characters");
    }

    @Test
    void parseAndStore_whenNoAttributesFound_doesNotSave() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "ADDITIONAL-SUMMARY": [],
                    "PERFORM-ATTRIBUTES": []
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAttributeRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauAttributeRepositoryWrapper, never()).saveAllWithException(anyList());
    }

    @Test
    void parseAndStore_whenAttrValueIsMissing_savesWithNullValue() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "ADDITIONAL-SUMMARY": [
                        {"ATTR-NAME": "SomeAttr"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifAttributeService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CreditBureauAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(creditBureauAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauAttribute> saved = captor.getValue();

        assertEquals(1, saved.size(), "Should save attribute even without value");
        assertNull(saved.get(0).getAttrValue(), "Attribute value should be null when missing from JSON");
    }
}
