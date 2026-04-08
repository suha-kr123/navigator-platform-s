package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.entity.CreditBureauSummary;
import com.nivasafinance.features.creditbureau.repository.CreditBureauSummaryRepository;
import com.nivasafinance.features.creditbureau.repository.CreditBureauSummaryRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifSummaryServiceTest {

    @Mock
    private CreditBureauSummaryRepositoryWrapper creditBureauSummaryRepositoryWrapper;

    @Mock
    private CreditBureauSummaryRepository creditBureauSummaryRepository;

    @InjectMocks
    private CrifSummaryService crifSummaryService;

    private static final Long ENQUIRY_ID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parseAndStore() Tests ====================

    @Test
    void parseAndStore_withScoreData_parsesScoreNameVersionAndValue() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "SCORE": [
                        {
                            "NAME": "CRIF Score",
                            "VERSION": "2.0",
                            "VALUE": "750",
                            "FACTORS": [
                                {"TYPE": "Factor1", "DESC": "High credit utilization"}
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(ENQUIRY_ID)).thenReturn(Optional.empty());

        // Act
        crifSummaryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauSummary> captor = ArgumentCaptor.forClass(CreditBureauSummary.class);
        verify(creditBureauSummaryRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauSummary saved = captor.getValue();

        assertEquals(ENQUIRY_ID, saved.getEnquiryId(), "Enquiry ID should match");
        assertEquals("CRIF Score", saved.getScoreName(), "Score name should be parsed");
        assertEquals("2.0", saved.getScoreVersion(), "Score version should be parsed");
        assertEquals(750, saved.getCreditScore(), "Credit score should be parsed");
        assertNotNull(saved.getScoreFactorDetails(), "Score factors should be parsed");
        assertEquals(1, saved.getScoreFactorDetails().size(), "Should have one score factor");
        assertEquals("Factor1", saved.getScoreFactorDetails().get(0).getFactorType(), "Factor type should match");
    }

    @Test
    void parseAndStore_withPrimaryAccountsSummary_parsesAccountCountsAndAmounts() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "PRIMARY-ACCOUNTS-SUMMARY": {
                        "NUMBER-OF-ACCOUNTS": 10,
                        "ACTIVE-ACCOUNTS": 5,
                        "OVERDUE-ACCOUNTS": 1,
                        "SECURED-ACCOUNTS": 3,
                        "UNSECURED-ACCOUNTS": 7,
                        "UNTAGGED-ACCOUNTS": 0,
                        "TOTAL-CURRENT-BALANCE": "150000.50",
                        "TOTAL-AMT-OVERDUE": "5000",
                        "TOTAL-SANCTIONED-AMT": "500000",
                        "TOTAL-DISBURSED-AMT": "450000"
                    }
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(ENQUIRY_ID)).thenReturn(Optional.empty());

        // Act
        crifSummaryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauSummary> captor = ArgumentCaptor.forClass(CreditBureauSummary.class);
        verify(creditBureauSummaryRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauSummary saved = captor.getValue();

        assertEquals(10, saved.getTotalAccounts(), "Total accounts should be parsed");
        assertEquals(5, saved.getActiveAccounts(), "Active accounts should be parsed");
        assertEquals(1, saved.getOverdueAccounts(), "Overdue accounts should be parsed");
        assertEquals(new BigDecimal("150000.50"), saved.getTotalCurrentBalance(), "Current balance should be parsed");
        assertEquals(new BigDecimal("5000"), saved.getTotalOverdueAmount(), "Overdue amount should be parsed");
        assertEquals(10, saved.getAccountCount(), "Account count should default to totalAccounts");
    }

    @Test
    void parseAndStore_withMfiGroupSummary_parsesMfiFields() throws Exception {
        // Arrange
        String json = """
            {
                "ACCOUNTS-SUMMARY": {
                    "MFI-GROUP-ACCOUNTS-SUMMARY": {
                        "CLOSED-ACCOUNTS": 2,
                        "NO-OF-OWN-MFIS": 1,
                        "NO-OF-OTHER-MFIS": 3,
                        "TOTAL-OWN-CURRENT-BALANCE": "10000",
                        "MAX-WORST-DELINQUENCY": 30
                    }
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(ENQUIRY_ID)).thenReturn(Optional.empty());

        // Act
        crifSummaryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauSummary> captor = ArgumentCaptor.forClass(CreditBureauSummary.class);
        verify(creditBureauSummaryRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauSummary saved = captor.getValue();

        assertEquals(2, saved.getClosedAccounts(), "Closed accounts should be parsed from MFI summary");
        assertEquals(1, saved.getNoOfOwnMfis(), "Own MFIs count should be parsed");
        assertEquals(3, saved.getNoOfOtherMfis(), "Other MFIs count should be parsed");
        assertEquals(30, saved.getMaxWorstDelinquency(), "Max worst delinquency should be parsed");
    }

    @Test
    void parseAndStore_whenExistingSummaryExists_deletesOldAndSavesNew() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "SCORE": [{"VALUE": "700"}]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauSummary existing = CreditBureauSummary.builder().enquiryId(ENQUIRY_ID).creditScore(650).build();
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(ENQUIRY_ID)).thenReturn(Optional.of(existing));

        // Act
        crifSummaryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauSummaryRepository).delete(existing);
        verify(creditBureauSummaryRepositoryWrapper).saveWithException(any(CreditBureauSummary.class));
    }

    @Test
    void parseAndStore_withInvalidScoreValue_setsNullCreditScore() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "SCORE": [{"VALUE": "not-a-number", "NAME": "CRIF"}]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(ENQUIRY_ID)).thenReturn(Optional.empty());

        // Act
        crifSummaryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauSummary> captor = ArgumentCaptor.forClass(CreditBureauSummary.class);
        verify(creditBureauSummaryRepositoryWrapper).saveWithException(captor.capture());

        assertNull(captor.getValue().getCreditScore(), "Credit score should be null for invalid value");
    }

    @Test
    void parseAndStore_withNoStandardDataOrAccountsSummary_savesEmptySummary() throws Exception {
        // Arrange
        JsonNode reportData = mapper.readTree("{}");
        when(creditBureauSummaryRepositoryWrapper.findByEnquiryId(ENQUIRY_ID)).thenReturn(Optional.empty());

        // Act
        crifSummaryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauSummary> captor = ArgumentCaptor.forClass(CreditBureauSummary.class);
        verify(creditBureauSummaryRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauSummary saved = captor.getValue();

        assertEquals(ENQUIRY_ID, saved.getEnquiryId(), "Enquiry ID should be set");
        assertNull(saved.getCreditScore(), "Credit score should be null when no data");
        assertNull(saved.getTotalAccounts(), "Total accounts should be null when no data");
    }
}
