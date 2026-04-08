package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.dto.CbDerivedQueryConfig;
import com.nivasafinance.features.creditbureau.entity.CbConfig;
import com.nivasafinance.features.creditbureau.entity.CreditBureauDerivedAttribute;
import com.nivasafinance.features.creditbureau.repository.CbConfigRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauDerivedAttributeRepositoryWrapper;
import com.nivasafinance.features.dataprovider.service.DataProviderExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauDerivedAttributeWriteServiceImplTest {

    @Mock
    private CbConfigRepositoryWrapper cbConfigRepositoryWrapper;

    @Mock
    private CreditBureauDerivedAttributeRepositoryWrapper cbDerivedAttributeRepositoryWrapper;

    @Mock
    private DataProviderExecutor dataProviderExecutor;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CreditBureauDerivedAttributeWriteServiceImpl service;

    private static final Long ENQUIRY_ID = 10L;
    private static final Long LEAD_ID = 20L;
    private static final String PROVIDER_NAME = "cb_derived_provider";

    // ==================== saveDerivedAttributesForEnquiry() Tests ====================

    @Test
    void saveDerivedAttributesForEnquiry_whenConfigNotFound_returnsEarlyWithoutSave() {
        // Arrange
        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.empty());

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        verifyNoInteractions(dataProviderExecutor);
        verifyNoInteractions(cbDerivedAttributeRepositoryWrapper);
    }

    @Test
    void saveDerivedAttributesForEnquiry_whenConfigValueIsBlank_returnsEarlyWithoutSave() throws Exception {
        // Arrange
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue("  ").build();
        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        verifyNoInteractions(dataProviderExecutor);
        verifyNoInteractions(cbDerivedAttributeRepositoryWrapper);
    }

    @Test
    void saveDerivedAttributesForEnquiry_whenConfigParsesToEmptyList_returnsEarlyWithoutSave() throws Exception {
        // Arrange
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue("[]").build();
        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));
        when(objectMapper.readValue(eq("[]"), any(TypeReference.class))).thenReturn(List.of());

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        verifyNoInteractions(dataProviderExecutor);
        verifyNoInteractions(cbDerivedAttributeRepositoryWrapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveDerivedAttributesForEnquiry_withValidConfig_executesProviderAndSavesAttributes() throws Exception {
        // Arrange
        CbDerivedQueryConfig queryConfig = new CbDerivedQueryConfig(PROVIDER_NAME);
        String configJson = "[{\"providerName\":\"" + PROVIDER_NAME + "\"}]";
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue(configJson).build();

        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));
        when(objectMapper.readValue(eq(configJson), any(TypeReference.class))).thenReturn(List.of(queryConfig));

        Map<String, Object> row = Map.of("attr_name", "score_band", "attr_value", "HIGH");
        when(dataProviderExecutor.executeDataProviderForList(eq(PROVIDER_NAME), any(Map.class)))
                .thenReturn(List.of(row));

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        ArgumentCaptor<List<CreditBureauDerivedAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(cbDerivedAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauDerivedAttribute> saved = captor.getValue();
        assertEquals(1, saved.size(), "Should save one derived attribute");
        assertEquals("score_band", saved.get(0).getAttrName(), "Attribute name should match");
        assertEquals("HIGH", saved.get(0).getAttrValue(), "Attribute value should match");
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveDerivedAttributesForEnquiry_whenRowMissingAttrName_skipsRow() throws Exception {
        // Arrange
        CbDerivedQueryConfig queryConfig = new CbDerivedQueryConfig(PROVIDER_NAME);
        String configJson = "[{\"providerName\":\"" + PROVIDER_NAME + "\"}]";
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue(configJson).build();

        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));
        when(objectMapper.readValue(eq(configJson), any(TypeReference.class))).thenReturn(List.of(queryConfig));

        Map<String, Object> rowWithoutName = Map.of("attr_value", "SOME_VALUE");
        when(dataProviderExecutor.executeDataProviderForList(eq(PROVIDER_NAME), any(Map.class)))
                .thenReturn(List.of(rowWithoutName));

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        verifyNoInteractions(cbDerivedAttributeRepositoryWrapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveDerivedAttributesForEnquiry_whenProviderNameIsBlank_skipsProvider() throws Exception {
        // Arrange
        CbDerivedQueryConfig blankProvider = new CbDerivedQueryConfig("  ");
        String configJson = "[{\"providerName\":\"  \"}]";
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue(configJson).build();

        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));
        when(objectMapper.readValue(eq(configJson), any(TypeReference.class))).thenReturn(List.of(blankProvider));

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        verifyNoInteractions(dataProviderExecutor);
        verifyNoInteractions(cbDerivedAttributeRepositoryWrapper);
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveDerivedAttributesForEnquiry_whenLeadIdIsNull_savesWithNullDataExt() throws Exception {
        // Arrange
        CbDerivedQueryConfig queryConfig = new CbDerivedQueryConfig(PROVIDER_NAME);
        String configJson = "[{\"providerName\":\"" + PROVIDER_NAME + "\"}]";
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue(configJson).build();

        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));
        when(objectMapper.readValue(eq(configJson), any(TypeReference.class))).thenReturn(List.of(queryConfig));

        Map<String, Object> row = Map.of("attr_name", "risk_flag", "attr_value", "LOW");
        when(dataProviderExecutor.executeDataProviderForList(eq(PROVIDER_NAME), any(Map.class)))
                .thenReturn(List.of(row));

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, null);

        // Assert
        ArgumentCaptor<List<CreditBureauDerivedAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(cbDerivedAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauDerivedAttribute> saved = captor.getValue();
        assertNull(saved.get(0).getDataExt(), "dataExt should be null when leadId is null");
    }

    @SuppressWarnings("unchecked")
    @Test
    void saveDerivedAttributesForEnquiry_whenLeadIdProvided_savesWithDataExtContainingLeadId() throws Exception {
        // Arrange
        CbDerivedQueryConfig queryConfig = new CbDerivedQueryConfig(PROVIDER_NAME);
        String configJson = "[{\"providerName\":\"" + PROVIDER_NAME + "\"}]";
        CbConfig config = CbConfig.builder().configKey("CB_DERIVED_QUERIES").configValue(configJson).build();

        when(cbConfigRepositoryWrapper.findByConfigKey(CreditBureauDerivedAttributeWriteServiceImpl.CB_DERIVED_QUERIES_KEY))
                .thenReturn(Optional.of(config));
        when(objectMapper.readValue(eq(configJson), any(TypeReference.class))).thenReturn(List.of(queryConfig));

        Map<String, Object> row = Map.of("attr_name", "risk_flag", "attr_value", "LOW");
        when(dataProviderExecutor.executeDataProviderForList(eq(PROVIDER_NAME), any(Map.class)))
                .thenReturn(List.of(row));

        // Act
        service.saveDerivedAttributesForEnquiry(ENQUIRY_ID, LEAD_ID);

        // Assert
        ArgumentCaptor<List<CreditBureauDerivedAttribute>> captor = ArgumentCaptor.forClass(List.class);
        verify(cbDerivedAttributeRepositoryWrapper).saveAllWithException(captor.capture());
        List<CreditBureauDerivedAttribute> saved = captor.getValue();
        assertNotNull(saved.get(0).getDataExt(), "dataExt should not be null when leadId is provided");
        assertEquals(LEAD_ID, saved.get(0).getDataExt().get("leadId"), "dataExt should contain leadId");
    }
}
