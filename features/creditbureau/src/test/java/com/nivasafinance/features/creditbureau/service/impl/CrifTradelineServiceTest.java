package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountPaymentHistory;
import com.nivasafinance.features.creditbureau.entity.CreditBureauAccountSecurityDetails;
import com.nivasafinance.features.creditbureau.entity.CreditBureauTradeline;
import com.nivasafinance.features.creditbureau.repository.CreditBureauAccountPaymentHistoryRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauAccountSecurityDetailsRepositoryWrapper;
import com.nivasafinance.features.creditbureau.repository.CreditBureauTradelineRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifTradelineServiceTest {

    @Mock
    private CreditBureauTradelineRepositoryWrapper creditBureauTradelineRepositoryWrapper;

    @Mock
    private CreditBureauAccountPaymentHistoryRepositoryWrapper creditBureauAccountPaymentHistoryRepositoryWrapper;

    @Mock
    private CreditBureauAccountSecurityDetailsRepositoryWrapper creditBureauAccountSecurityDetailsRepositoryWrapper;

    @InjectMocks
    private CrifTradelineService crifTradelineService;

    private static final Long ENQUIRY_ID = 1L;
    private static final Long TRADELINE_ID = 10L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parseAndStore() Tests ====================

    @Test
    void parseAndStore_whenStandardDataMissing_returnsEarly() throws Exception {
        // Arrange
        JsonNode reportData = mapper.readTree("{}");

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTradelineRepositoryWrapper, never()).deleteByEnquiryId(any());
        verify(creditBureauTradelineRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_whenTradelinesEmpty_deletesOldAndDoesNotSave() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": []
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTradelineRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauTradelineRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_withValidTradeline_parsesKeyFieldsAndSaves() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-001",
                            "CREDIT-GRANTOR": "Test Bank",
                            "ACCT-TYPE": "Personal Loan",
                            "ACCOUNT-STATUS": "Active",
                            "DISBURSED-AMT": "100000",
                            "CURRENT-BAL": "75000.50",
                            "OVERDUE-AMT": "0",
                            "DISBURSED-DT": "15-01-2024",
                            "REPORTED-DT": "01-03-2025",
                            "OWNERSHIP-TYPE": "Individual"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauTradeline savedTradeline = CreditBureauTradeline.builder()
                .enquiryId(ENQUIRY_ID).accountNumber("ACC-001").build();
        savedTradeline.setId(TRADELINE_ID);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenReturn(savedTradeline);

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauTradeline saved = captor.getValue();

        assertEquals(ENQUIRY_ID, saved.getEnquiryId(), "Enquiry ID should match");
        assertEquals("ACC-001", saved.getAccountNumber(), "Account number should be parsed");
        assertEquals("Test Bank", saved.getCreditGrantor(), "Credit grantor should be parsed");
        assertEquals("Personal Loan", saved.getAccountType(), "Account type should be parsed");
        assertEquals("Active", saved.getAccountStatus(), "Account status should be parsed");
        assertEquals(new BigDecimal("100000"), saved.getDisbursedAmount(), "Disbursed amount should be parsed");
        assertEquals(new BigDecimal("75000.50"), saved.getCurrentBalance(), "Current balance should be parsed");
        assertEquals(LocalDate.of(2024, 1, 15), saved.getDisbursedDate(), "Disbursed date should be parsed");
        assertEquals(LocalDate.of(2025, 3, 1), saved.getReportedDate(), "Reported date should be parsed");
    }

    @Test
    void parseAndStore_withPaymentHistory_parsesAndSavesHistoryEntries() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-002",
                            "HISTORY": [
                                {
                                    "NAME": "DPD",
                                    "DATES": "Jan-2025|Feb-2025",
                                    "VALUES": "0|30"
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauTradeline savedTradeline = CreditBureauTradeline.builder()
                .enquiryId(ENQUIRY_ID).build();
        savedTradeline.setId(TRADELINE_ID);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenReturn(savedTradeline);

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper).deleteByTradelineId(TRADELINE_ID);
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper, times(2))
                .saveWithException(any(CreditBureauAccountPaymentHistory.class));
    }

    @Test
    void parseAndStore_withSecurityDetails_parsesAndSaves() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-003",
                            "SECURITY-DETAILS": [
                                {
                                    "SECURITY-TYPE": "Property",
                                    "OWNER-NAME": "John Doe",
                                    "SECURITY-VALUATION": "500000"
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauTradeline savedTradeline = CreditBureauTradeline.builder()
                .enquiryId(ENQUIRY_ID).build();
        savedTradeline.setId(TRADELINE_ID);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenReturn(savedTradeline);

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAccountSecurityDetailsRepositoryWrapper).deleteByTradelineId(TRADELINE_ID);
        verify(creditBureauAccountSecurityDetailsRepositoryWrapper)
                .saveWithException(any(CreditBureauAccountSecurityDetails.class));
    }

    @Test
    void parseAndStore_withMultipleTradelines_savesEach() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {"ACCT-NUMBER": "ACC-A"},
                        {"ACCT-NUMBER": "ACC-B"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauTradeline savedTradeline = CreditBureauTradeline.builder()
                .enquiryId(ENQUIRY_ID).build();
        savedTradeline.setId(TRADELINE_ID);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenReturn(savedTradeline);

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauTradelineRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauTradelineRepositoryWrapper, times(2)).saveWithException(any(CreditBureauTradeline.class));
    }

    @Test
    void parseAndStore_deletesExistingTradelinesBeforeSaving() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {"ACCT-NUMBER": "ACC-X"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        CreditBureauTradeline savedTradeline = CreditBureauTradeline.builder()
                .enquiryId(ENQUIRY_ID).build();
        savedTradeline.setId(TRADELINE_ID);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenReturn(savedTradeline);

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        var inOrder = inOrder(creditBureauTradelineRepositoryWrapper);
        inOrder.verify(creditBureauTradelineRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        inOrder.verify(creditBureauTradelineRepositoryWrapper).saveWithException(any());
    }

    // ==================== parseDate branch coverage ====================

    @Test
    void parseAndStore_withIsoFormatDate_parsesCorrectly() throws Exception {
        // Arrange — covers trimmed.contains("-") && trimmed.length() > 10 branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-ISO",
                            "DISBURSED-DT": "2024-01-15T10:30:00"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        CreditBureauTradeline saved = stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(LocalDate.of(2024, 1, 15), captor.getValue().getDisbursedDate(),
                "Should parse ISO-prefixed date using yyyy-MM-dd format");
    }

    @Test
    void parseAndStore_withEmptyDateString_returnsNullDate() throws Exception {
        // Arrange — covers dateString.trim().isEmpty() branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-EMPTY-DT",
                            "DISBURSED-DT": "   "
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getDisbursedDate(), "Blank date string should result in null");
    }

    @Test
    void parseAndStore_withInvalidDateFormat_returnsNullDate() throws Exception {
        // Arrange — covers DateTimeParseException branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-BAD-DT",
                            "DISBURSED-DT": "99-99-9999"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getDisbursedDate(), "Invalid date should result in null");
    }

    @Test
    void parseAndStore_withDateNoHyphen_returnsNullDate() throws Exception {
        // Arrange — covers fall-through: no "-" in date string
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-NO-HYPHEN",
                            "DISBURSED-DT": "20240115"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getDisbursedDate(), "Date without hyphens should result in null");
    }

    // ==================== parseDateTime branch coverage ====================

    @Test
    void parseAndStore_withValidDateTime_parsesLastPaymentDate() throws Exception {
        // Arrange — covers valid yyyy-MM-dd HH:mm:ss branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-DT",
                            "LAST-PAYMENT-DT": "2025-03-15 14:30:00"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30, 0), captor.getValue().getLastPaymentDate(),
                "Should parse valid datetime");
    }

    @Test
    void parseAndStore_withShortDateTimeString_returnsNull() throws Exception {
        // Arrange — covers trimmed.length() < 19 or no space branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-SHORT-DT",
                            "LAST-PAYMENT-DT": "2025-03-15"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getLastPaymentDate(), "Short datetime string should result in null");
    }

    @Test
    void parseAndStore_withInvalidDateTime_returnsNull() throws Exception {
        // Arrange — covers DateTimeParseException in parseDateTime
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-BAD-DTM",
                            "LAST-PAYMENT-DT": "9999-99-99 99:99:99"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getLastPaymentDate(), "Invalid datetime should result in null");
    }

    // ==================== parseBoolean branch coverage ====================

    @Test
    void parseAndStore_withBooleanTrueVariants_parsesAsTrue() throws Exception {
        // Arrange — covers "true", "Y", "1" branches
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-BOOL-Y",
                            "ACCT-IN-DISPUTE": "Y"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().getAccountInDispute(), "Y should parse as true");
    }

    @Test
    void parseAndStore_withBooleanFalseVariants_parsesAsFalse() throws Exception {
        // Arrange — covers "false", "N", "0" branches
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-BOOL-N",
                            "ACCT-IN-DISPUTE": "N"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getAccountInDispute(), "N should parse as false");
    }

    @Test
    void parseAndStore_withBooleanUnrecognizedValue_returnsNull() throws Exception {
        // Arrange — covers unrecognized value fall-through in parseBoolean
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-BOOL-UNK",
                            "ACCT-IN-DISPUTE": "MAYBE"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getAccountInDispute(), "Unrecognized boolean value should result in null");
    }

    // ==================== parseInteger branch coverage ====================

    @Test
    void parseAndStore_withIntegerNodeValue_parsesOriginalTerm() throws Exception {
        // Arrange — covers fieldNode.isInt() branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-INT",
                            "ORIGINAL-TERM": 36
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(36, captor.getValue().getOriginalTerm(), "Integer node should be parsed directly");
    }

    @Test
    void parseAndStore_withTextualIntegerValue_parsesOriginalTerm() throws Exception {
        // Arrange — covers fieldNode.isTextual() + valid Integer.parseInt branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-INT-TXT",
                            "ORIGINAL-TERM": "24"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(24, captor.getValue().getOriginalTerm(), "Textual integer should be parsed");
    }

    @Test
    void parseAndStore_withInvalidIntegerText_returnsNull() throws Exception {
        // Arrange — covers NumberFormatException in parseInteger
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-INT-BAD",
                            "ORIGINAL-TERM": "not-a-number"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getOriginalTerm(), "Invalid integer text should result in null");
    }

    // ==================== parseNumericFromString branch coverage ====================

    @Test
    void parseAndStore_withCommaFormattedAmount_parsesCorrectly() throws Exception {
        // Arrange — covers trimmed.replace(",", "") branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-COMMA",
                            "DISBURSED-AMT": "1,00,000.50"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertEquals(new BigDecimal("100000.50"), captor.getValue().getDisbursedAmount(),
                "Comma-formatted number should be parsed after removing commas");
    }

    @Test
    void parseAndStore_withInvalidNumericString_returnsNull() throws Exception {
        // Arrange — covers NumberFormatException in parseNumericFromString
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-BAD-NUM",
                            "DISBURSED-AMT": "abc-xyz"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getDisbursedAmount(), "Invalid numeric string should result in null");
    }

    // ==================== parseAndStorePaymentHistory branch coverage ====================

    @Test
    void parseAndStore_withNullHistoryFields_skipsEntry() throws Exception {
        // Arrange — covers historyType/datesString/valuesString null check
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-HIST-NULL",
                            "HISTORY": [
                                {
                                    "NAME": "DPD"
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper).deleteByTradelineId(TRADELINE_ID);
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper, never())
                .saveWithException(any(CreditBureauAccountPaymentHistory.class));
    }

    @Test
    void parseAndStore_withEmptyHistoryValues_skipsEmptyEntries() throws Exception {
        // Arrange — covers empty monthYear/historyValue continue branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-HIST-EMPTY",
                            "HISTORY": [
                                {
                                    "NAME": "DPD",
                                    "DATES": "Jan-2025||Mar-2025",
                                    "VALUES": "0||30"
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert — only 2 entries saved, the empty middle entry is skipped
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper, times(2))
                .saveWithException(any(CreditBureauAccountPaymentHistory.class));
    }

    @Test
    void parseAndStore_withMismatchedHistoryLengths_usesMinLength() throws Exception {
        // Arrange — covers Math.min(dates.length, values.length) branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-HIST-MISMATCH",
                            "HISTORY": [
                                {
                                    "NAME": "DPD",
                                    "DATES": "Jan-2025|Feb-2025|Mar-2025",
                                    "VALUES": "0|30"
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert — only 2 entries saved (min of 3 dates and 2 values)
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper, times(2))
                .saveWithException(any(CreditBureauAccountPaymentHistory.class));
    }

    @Test
    void parseAndStore_withNoHistoryArray_deletesButDoesNotSaveHistory() throws Exception {
        // Arrange — covers !historyArray.isArray() branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-NO-HIST"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper).deleteByTradelineId(TRADELINE_ID);
        verify(creditBureauAccountPaymentHistoryRepositoryWrapper, never())
                .saveWithException(any(CreditBureauAccountPaymentHistory.class));
    }

    // ==================== parseAndStoreSecurityDetails branch coverage ====================

    @Test
    void parseAndStore_withFullSecurityDetails_parsesAllFields() throws Exception {
        // Arrange — covers all security detail fields including date/integer parsing
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-SEC-FULL",
                            "SECURITY-DETAILS": [
                                {
                                    "SECURITY-TYPE": "Vehicle",
                                    "OWNER-NAME": "Jane Smith",
                                    "SECURITY-VALUATION": "300000",
                                    "DATE-OF-VALUATION": "10-06-2024",
                                    "SECURITY-CHARGE": "First",
                                    "PROPERTY-ADDRESS": "123 Main St",
                                    "AUTOMOBILE-TYPE": "Car",
                                    "YEAR-OF-MANUFACTURING": 2022,
                                    "REGISTRATION-NUMBER": "KA01AB1234",
                                    "ENGINE-NUMBER": "ENG123456",
                                    "CHASSIE-NUMBER": "CHS789012"
                                }
                            ]
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauAccountSecurityDetails> captor =
                ArgumentCaptor.forClass(CreditBureauAccountSecurityDetails.class);
        verify(creditBureauAccountSecurityDetailsRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauAccountSecurityDetails saved = captor.getValue();

        assertEquals(TRADELINE_ID, saved.getTradelineId(), "Tradeline ID should match");
        assertEquals("Vehicle", saved.getSecurityType(), "Security type should be parsed");
        assertEquals("Jane Smith", saved.getOwnerName(), "Owner name should be parsed");
        assertEquals(new BigDecimal("300000"), saved.getSecurityValuation(), "Valuation should be parsed");
        assertEquals(LocalDate.of(2024, 6, 10), saved.getDateOfValuation(), "Valuation date should be parsed");
        assertEquals("First", saved.getSecurityCharge(), "Security charge should be parsed");
        assertEquals("123 Main St", saved.getPropertyAddress(), "Property address should be parsed");
        assertEquals("Car", saved.getAutomobileType(), "Automobile type should be parsed");
        assertEquals(2022, saved.getYearOfManufacturing(), "Year should be parsed");
        assertEquals("KA01AB1234", saved.getRegistrationNumber(), "Registration number should be parsed");
        assertEquals("ENG123456", saved.getEngineNumber(), "Engine number should be parsed");
        assertEquals("CHS789012", saved.getChassisNumber(), "Chassis number should be parsed");
    }

    @Test
    void parseAndStore_withNoSecurityDetailsArray_deletesButDoesNotSave() throws Exception {
        // Arrange — covers !securityDetailsArray.isArray() branch
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-NO-SEC"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauAccountSecurityDetailsRepositoryWrapper).deleteByTradelineId(TRADELINE_ID);
        verify(creditBureauAccountSecurityDetailsRepositoryWrapper, never())
                .saveWithException(any(CreditBureauAccountSecurityDetails.class));
    }

    // ==================== getTextValue null node branch ====================

    @Test
    void parseAndStore_withNullFieldValue_returnsNullForTextField() throws Exception {
        // Arrange — covers fieldNode.isNull() branch in getTextValue
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-NULL-FIELD",
                            "CREDIT-GRANTOR": null
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getCreditGrantor(), "Null JSON value should result in null");
    }

    // ==================== parseAndStore outer exception coverage ====================

    @Test
    void parseAndStore_whenSaveThrowsException_propagatesAsRuntimeException() throws Exception {
        // Arrange — covers outer catch block
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-ERR"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> crifTradelineService.parseAndStore(ENQUIRY_ID, reportData),
                "Should propagate as RuntimeException");
        assertTrue(exception.getMessage().contains("Failed to parse credit bureau tradelines"),
                "Exception message should describe the failure");
    }

    // ==================== Tradeline with all extended fields ====================

    @Test
    void parseAndStore_withAllTradelineFields_parsesExtendedFields() throws Exception {
        // Arrange — covers remaining fields: closedDate, creditLimit, cashLimit, installmentAmount,
        // installmentFrequency, termToMaturity, repaymentTenure, interestRate, lastPaidAmount,
        // actualPayment, writeOffAmount, principalWriteOffAmount, settlementAmount, writeOffDate,
        // securityStatus, obligation, accountRemarks, suitFiledWilfulDefaultStatus,
        // writtenOffSettledStatus, suitFiledDate, occupation, incomeFrequency, incomeAmount
        String json = """
            {
                "STANDARD-DATA": {
                    "TRADELINES": [
                        {
                            "ACCT-NUMBER": "ACC-FULL",
                            "CREDIT-GRANTOR": "Full Bank",
                            "CREDIT-GRANTOR-GROUP": "Full Group",
                            "CREDIT-GRANTOR-TYPE": "Bank",
                            "ACCT-TYPE": "Home Loan",
                            "ACCOUNT-STATUS": "Closed",
                            "REPORTED-DT": "01-02-2025",
                            "CLOSED-DT": "15-12-2024",
                            "OWNERSHIP-TYPE": "Joint",
                            "DISBURSED-AMT": "5000000",
                            "DISBURSED-DT": "01-01-2020",
                            "CURRENT-BAL": "0",
                            "CREDIT-LIMIT": "5500000",
                            "CASH-LIMIT": "100000",
                            "OVERDUE-AMT": "0",
                            "INSTALLMENT-AMT": "45000",
                            "INSTALLMENT-FREQUENCY": "Monthly",
                            "ORIGINAL-TERM": 240,
                            "TERM-TO-MATURITY": "0",
                            "REPAYMENT-TENURE": "20 Years",
                            "INTEREST-RATE": "8.5",
                            "LAST-PAYMENT-DT": "2024-12-01 10:00:00",
                            "LAST-PAID-AMOUNT": "45000",
                            "ACTUAL-PAYMENT": "45000",
                            "WRITE-OFF-AMT": "0",
                            "PRINCIPAL-WRITE-OFF-AMT": "0",
                            "SETTLEMENT-AMT": "0",
                            "WRITE-OFF-DT": "01-01-2025",
                            "SECURITY-STATUS": "Secured",
                            "OBLIGATION": "45000",
                            "ACCOUNT-REMARKS": "Closed on time",
                            "ACCT-IN-DISPUTE": "false",
                            "SUIT-FILED-WILFUL-DEFAULT-STATUS": "None",
                            "WRITTEN-OFF-SETTLED-STATUS": "N/A",
                            "SUIT-FILED-DT": "2025-01-20T00:00:00",
                            "OCCUPATION": "Salaried",
                            "INCOME-FREQUENCY": "Monthly",
                            "INCOME-AMOUNT": "1,50,000"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);
        stubTradelineSave();

        // Act
        crifTradelineService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauTradeline> captor = ArgumentCaptor.forClass(CreditBureauTradeline.class);
        verify(creditBureauTradelineRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauTradeline saved = captor.getValue();

        assertEquals("Full Group", saved.getCreditGrantorGroup(), "Credit grantor group should be parsed");
        assertEquals("Bank", saved.getCreditGrantorType(), "Credit grantor type should be parsed");
        assertEquals(LocalDate.of(2024, 12, 15), saved.getClosedDate(), "Closed date should be parsed");
        assertEquals(new BigDecimal("5500000"), saved.getCreditLimit(), "Credit limit should be parsed");
        assertEquals(new BigDecimal("100000"), saved.getCashLimit(), "Cash limit should be parsed");
        assertEquals("45000", saved.getInstallmentAmount(), "Installment amount should be parsed");
        assertEquals("Monthly", saved.getInstallmentFrequency(), "Installment frequency should be parsed");
        assertEquals(240, saved.getOriginalTerm(), "Original term should be parsed");
        assertEquals("20 Years", saved.getRepaymentTenure(), "Repayment tenure should be parsed");
        assertEquals("8.5", saved.getInterestRate(), "Interest rate should be parsed");
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 0, 0), saved.getLastPaymentDate(),
                "Last payment date should be parsed");
        assertEquals(new BigDecimal("45000"), saved.getLastPaidAmount(), "Last paid amount should be parsed");
        assertEquals(new BigDecimal("45000"), saved.getActualPayment(), "Actual payment should be parsed");
        assertEquals(new BigDecimal("0"), saved.getWriteOffAmount(), "Write-off amount should be parsed");
        assertEquals(new BigDecimal("0"), saved.getPrincipalWriteOffAmount(), "Principal write-off should be parsed");
        assertEquals(new BigDecimal("0"), saved.getSettlementAmount(), "Settlement amount should be parsed");
        assertEquals(LocalDate.of(2025, 1, 1), saved.getWriteOffDate(), "Write-off date should be parsed");
        assertEquals("Secured", saved.getSecurityStatus(), "Security status should be parsed");
        assertEquals(new BigDecimal("45000"), saved.getObligation(), "Obligation should be parsed");
        assertEquals("Closed on time", saved.getAccountRemarks(), "Account remarks should be parsed");
        assertEquals(Boolean.FALSE, saved.getAccountInDispute(), "Account in dispute should be false");
        assertEquals("None", saved.getSuitFiledWilfulDefaultStatus(), "Suit filed status should be parsed");
        assertEquals("N/A", saved.getWrittenOffSettledStatus(), "Written-off status should be parsed");
        assertEquals(LocalDate.of(2025, 1, 20), saved.getSuitFiledDate(), "Suit filed date should be parsed (ISO)");
        assertEquals("Salaried", saved.getOccupation(), "Occupation should be parsed");
        assertEquals("Monthly", saved.getIncomeFrequency(), "Income frequency should be parsed");
        assertEquals(new BigDecimal("150000"), saved.getIncomeAmount(), "Comma-formatted income should be parsed");
    }

    // ==================== Helper ====================

    private CreditBureauTradeline stubTradelineSave() {
        CreditBureauTradeline savedTradeline = CreditBureauTradeline.builder()
                .enquiryId(ENQUIRY_ID).build();
        savedTradeline.setId(TRADELINE_ID);
        when(creditBureauTradelineRepositoryWrapper.saveWithException(any(CreditBureauTradeline.class)))
                .thenReturn(savedTradeline);
        return savedTradeline;
    }
}
