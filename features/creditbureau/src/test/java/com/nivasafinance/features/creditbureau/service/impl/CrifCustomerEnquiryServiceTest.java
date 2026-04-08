package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.entity.CreditBureauCustomerEnquiry;
import com.nivasafinance.features.creditbureau.repository.CreditBureauCustomerEnquiryRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifCustomerEnquiryServiceTest {

    @Mock
    private CreditBureauCustomerEnquiryRepositoryWrapper creditBureauCustomerEnquiryRepositoryWrapper;

    @InjectMocks
    private CrifCustomerEnquiryService crifCustomerEnquiryService;

    private static final Long ENQUIRY_ID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parseAndStore() Tests ====================

    @Test
    void parseAndStore_whenStandardDataMissing_deletesOldAndReturnsEarly() throws Exception {
        // Arrange
        JsonNode reportData = mapper.readTree("{}");

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauCustomerEnquiryRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauCustomerEnquiryRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_whenInquiryHistoryIsEmpty_deletesOldAndDoesNotSave() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": []
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauCustomerEnquiryRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauCustomerEnquiryRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_withValidInquiryHistory_parsesAndSavesCustomerEnquiry() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": [
                        {
                            "LENDER-NAME": "Test Bank",
                            "INQUIRY-DT": "15-01-2025",
                            "OWNERSHIP-TYPE": "Individual",
                            "CREDIT-INQ-PURPS-TYPE": "Personal Loan",
                            "AMOUNT": "50000"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauCustomerEnquiryRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);

        ArgumentCaptor<CreditBureauCustomerEnquiry> captor =
                ArgumentCaptor.forClass(CreditBureauCustomerEnquiry.class);
        verify(creditBureauCustomerEnquiryRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauCustomerEnquiry saved = captor.getValue();

        assertEquals(ENQUIRY_ID, saved.getEnquiryId(), "Enquiry ID should match");
        assertEquals("Test Bank", saved.getLenderName(), "Lender name should be parsed");
        assertEquals(LocalDate.of(2025, 1, 15), saved.getInquiryDate(), "Inquiry date should be parsed (dd-MM-yyyy)");
        assertEquals("Individual", saved.getOwnershipType(), "Ownership type should be parsed");
        assertEquals("Personal Loan", saved.getCreditInquiryPurposeType(), "Credit inquiry purpose should be parsed");
        assertEquals(new BigDecimal("50000"), saved.getInquiryAmount(), "Inquiry amount should be parsed");
    }

    @Test
    void parseAndStore_withMultipleInquiries_savesEachOne() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": [
                        {"LENDER-NAME": "Bank A", "INQUIRY-DT": "01-06-2025", "AMOUNT": "10000"},
                        {"LENDER-NAME": "Bank B", "INQUIRY-DT": "15-06-2025", "AMOUNT": "20000"}
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauCustomerEnquiryRepositoryWrapper, times(2)).saveWithException(any());
    }

    @Test
    void parseAndStore_withIsoDateFormat_parsesDateCorrectly() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": [
                        {
                            "LENDER-NAME": "Bank C",
                            "INQUIRY-DT": "2025-03-20T10:30:00",
                            "AMOUNT": "30000"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauCustomerEnquiry> captor =
                ArgumentCaptor.forClass(CreditBureauCustomerEnquiry.class);
        verify(creditBureauCustomerEnquiryRepositoryWrapper).saveWithException(captor.capture());

        assertEquals(LocalDate.of(2025, 3, 20), captor.getValue().getInquiryDate(),
                "Should parse ISO date format (yyyy-MM-dd...) correctly");
    }

    @Test
    void parseAndStore_withNullDateAndAmount_savesWithNullValues() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": [
                        {
                            "LENDER-NAME": "Bank D"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauCustomerEnquiry> captor =
                ArgumentCaptor.forClass(CreditBureauCustomerEnquiry.class);
        verify(creditBureauCustomerEnquiryRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauCustomerEnquiry saved = captor.getValue();

        assertNull(saved.getInquiryDate(), "Date should be null when missing from JSON");
        assertNull(saved.getInquiryAmount(), "Amount should be null when missing from JSON");
    }

    @Test
    void parseAndStore_withInvalidAmountFormat_savesWithNullAmount() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": [
                        {
                            "LENDER-NAME": "Bank E",
                            "AMOUNT": "not-a-number"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauCustomerEnquiry> captor =
                ArgumentCaptor.forClass(CreditBureauCustomerEnquiry.class);
        verify(creditBureauCustomerEnquiryRepositoryWrapper).saveWithException(captor.capture());

        assertNull(captor.getValue().getInquiryAmount(),
                "Amount should be null when value is not a valid number");
    }

    @Test
    void parseAndStore_withCommaFormattedAmount_parsesCorrectly() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "INQUIRY-HISTORY": [
                        {
                            "LENDER-NAME": "Bank F",
                            "AMOUNT": "1,00,000"
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifCustomerEnquiryService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauCustomerEnquiry> captor =
                ArgumentCaptor.forClass(CreditBureauCustomerEnquiry.class);
        verify(creditBureauCustomerEnquiryRepositoryWrapper).saveWithException(captor.capture());

        assertEquals(new BigDecimal("100000"), captor.getValue().getInquiryAmount(),
                "Amount with commas should be parsed after removing commas");
    }
}
