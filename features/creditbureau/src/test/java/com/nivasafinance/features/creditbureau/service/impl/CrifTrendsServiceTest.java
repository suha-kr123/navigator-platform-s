package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.entity.CreditBureauTrends;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTrendsRepository;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTrendsRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifTrendsServiceTest {

    @Mock
    private CreditBureauTrendsRepositoryWrapper creditBureauTrendsRepositoryWrapper;

    @Mock
    private CreditBureauTrendsRepository creditBureauTrendsRepository;

    @InjectMocks
    private CrifTrendsService crifTrendsService;

    private static final Long ENQUIRY_ID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parseAndStore() Tests ====================

    @Test
    void parseAndStore_whenTrendsMissing_returnsEarly() throws Exception {
        // Arrange
        JsonNode reportData = mapper.readTree("{}");

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTrendsRepositoryWrapper, never()).saveWithException(any());
        verify(creditBureauTrendsRepositoryWrapper, never()).findByEnquiryIdEntity(any());
    }

    @Test
    void parseAndStore_whenDatesEmpty_returnsEarlyWithoutSaving() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "Score",
                    "DATES": "",
                    "VALUES": ""
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID)).thenReturn(List.of());

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTrendsRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_withValidPipeSeparatedData_savesEachTrendEntry() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "CRIF Score",
                    "DATES": "30-09-2024|31-10-2024|30-11-2024",
                    "VALUES": "720|730|740"
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID)).thenReturn(List.of());

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTrends> captor = ArgumentCaptor.forClass(CreditBureauTrends.class);
        verify(creditBureauTrendsRepositoryWrapper, times(3)).saveWithException(captor.capture());
        List<CreditBureauTrends> saved = captor.getAllValues();

        assertEquals(ENQUIRY_ID, saved.get(0).getEnquiryId(), "Enquiry ID should match");
        assertEquals("CRIF Score", saved.get(0).getTrendName(), "Trend name should be parsed");
        assertEquals(LocalDate.of(2024, 9, 30), saved.get(0).getDate(), "First date should be parsed");
        assertEquals(720, saved.get(0).getScoreValue(), "First score value should match");
        assertEquals(LocalDate.of(2024, 10, 31), saved.get(1).getDate(), "Second date should be parsed");
        assertEquals(740, saved.get(2).getScoreValue(), "Third score value should match");
    }

    @Test
    void parseAndStore_withDescriptions_mapsDescriptionsToEntries() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "Score",
                    "DATES": "30-09-2024|31-10-2024",
                    "VALUES": "720|730",
                    "DESCRIPTION": "Good|Very Good"
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID)).thenReturn(List.of());

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTrends> captor = ArgumentCaptor.forClass(CreditBureauTrends.class);
        verify(creditBureauTrendsRepositoryWrapper, times(2)).saveWithException(captor.capture());
        List<CreditBureauTrends> saved = captor.getAllValues();

        assertEquals("Good", saved.get(0).getDescription(), "First description should match");
        assertEquals("Very Good", saved.get(1).getDescription(), "Second description should match");
    }

    @Test
    void parseAndStore_withMismatchedDatesAndValues_truncatesToMinimumLength() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "Score",
                    "DATES": "30-09-2024|31-10-2024|30-11-2024",
                    "VALUES": "720|730"
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID)).thenReturn(List.of());

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTrendsRepositoryWrapper, times(2)).saveWithException(any());
    }

    @Test
    void parseAndStore_withInvalidDateOrScore_skipsEntry() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "Score",
                    "DATES": "invalid-date|31-10-2024",
                    "VALUES": "720|abc"
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID)).thenReturn(List.of());

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTrendsRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_deletesExistingTrendsBeforeSaving() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "Score",
                    "DATES": "30-09-2024",
                    "VALUES": "720"
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauTrends existingTrend = CreditBureauTrends.builder()
                .enquiryId(ENQUIRY_ID).scoreValue(700).build();
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID))
                .thenReturn(List.of(existingTrend));

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTrendsRepository).deleteAll(List.of(existingTrend));
        verify(creditBureauTrendsRepositoryWrapper).saveWithException(any());
    }

    @Test
    void parseAndStore_withCommaInScoreValue_parsesCorrectly() throws Exception {
        // Arrange
        String json = """
            {
                "TRENDS": {
                    "NAME": "Score",
                    "DATES": "30-09-2024",
                    "VALUES": "1,749"
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTrendsRepositoryWrapper.findByEnquiryIdEntity(ENQUIRY_ID)).thenReturn(List.of());

        // Act
        crifTrendsService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTrends> captor = ArgumentCaptor.forClass(CreditBureauTrends.class);
        verify(creditBureauTrendsRepositoryWrapper).saveWithException(captor.capture());

        assertEquals(1749, captor.getValue().getScoreValue(),
                "Score value with comma should be parsed after removing commas");
    }
}
