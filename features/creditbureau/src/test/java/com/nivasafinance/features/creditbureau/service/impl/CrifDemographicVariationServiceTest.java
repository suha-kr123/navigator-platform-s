package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.creditbureau.entity.CreditBureauDemographicVariation;
import com.nivasafinance.features.creditbureau.repository.CreditBureauDemographicVariationRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifDemographicVariationServiceTest {

    @Mock
    private CreditBureauDemographicVariationRepositoryWrapper creditBureauDemographicVariationRepositoryWrapper;

    @InjectMocks
    private CrifDemographicVariationService crifDemographicVariationService;

    private static final Long ENQUIRY_ID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parseAndStore() Tests ====================

    @Test
    void parseAndStore_whenStandardDataMissing_deletesOldAndReturnsEarly() throws Exception {
        // Arrange
        JsonNode reportData = mapper.readTree("{}");

        // Act
        crifDemographicVariationService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauDemographicVariationRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauDemographicVariationRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_withDemogsVariations_parsesAndSavesVariations() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "DEMOGS": {
                        "VARIATIONS": [
                            {
                                "TYPE": "ADDRESS",
                                "VARIATION": [
                                    {
                                        "VALUE": "123 Main St",
                                        "REPORTED-DT": "15-01-2025",
                                        "FIRST-REPORTED-DT": "01-06-2024",
                                        "LOAN-TYPE-ASSOC": "Personal",
                                        "SOURCE-INDICATOR": "CRIF"
                                    }
                                ]
                            }
                        ]
                    }
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifDemographicVariationService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauDemographicVariationRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);

        ArgumentCaptor<CreditBureauDemographicVariation> captor =
                ArgumentCaptor.forClass(CreditBureauDemographicVariation.class);
        verify(creditBureauDemographicVariationRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauDemographicVariation saved = captor.getValue();

        assertEquals(ENQUIRY_ID, saved.getEnquiryId(), "Enquiry ID should match");
        assertEquals("ADDRESS", saved.getVariationType(), "Variation type should be parsed");
        assertEquals("123 Main St", saved.getVariationValue(), "Variation value should be parsed");
        assertEquals(LocalDate.of(2025, 1, 15), saved.getReportedDate(), "Reported date should be parsed");
        assertEquals(LocalDate.of(2024, 6, 1), saved.getFirstReportedDate(), "First reported date should be parsed");
        assertEquals("Personal", saved.getLoanTypeAssociated(), "Loan type should be parsed");
        assertEquals("CRIF", saved.getSourceIndicator(), "Source indicator should be parsed");
    }

    @Test
    void parseAndStore_withEmploymentDetails_savesAsEmploymentDetailsType() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "EMPLOYMENT-DETAILS": [
                        {
                            "EMPLOYMENT-DETAIL": {
                                "OCCUPATION": "Engineer",
                                "LAST-REPORTED-DT": "20-03-2025",
                                "FIRST-REPORTED-DT": "10-01-2023",
                                "ACCT-TYPE": "Salary",
                                "SOURCE-INDICATOR": "EMPLOYER"
                            }
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifDemographicVariationService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        ArgumentCaptor<CreditBureauDemographicVariation> captor =
                ArgumentCaptor.forClass(CreditBureauDemographicVariation.class);
        verify(creditBureauDemographicVariationRepositoryWrapper).saveWithException(captor.capture());
        CreditBureauDemographicVariation saved = captor.getValue();

        assertEquals("EMPLOYMENT-DETAILS", saved.getVariationType(),
                "Variation type should be EMPLOYMENT-DETAILS for employment records");
        assertEquals("Engineer", saved.getVariationValue(), "Occupation should be mapped to variation value");
        assertEquals("Salary", saved.getLoanTypeAssociated(), "Account type should be mapped to loan type");
    }

    @Test
    void parseAndStore_withBothDemogsAndEmployment_savesBoth() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "DEMOGS": {
                        "VARIATIONS": [
                            {
                                "TYPE": "PHONE",
                                "VARIATION": [
                                    {"VALUE": "9876543210"}
                                ]
                            }
                        ]
                    },
                    "EMPLOYMENT-DETAILS": [
                        {
                            "EMPLOYMENT-DETAIL": {
                                "OCCUPATION": "Manager"
                            }
                        }
                    ]
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifDemographicVariationService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauDemographicVariationRepositoryWrapper, times(2)).saveWithException(any());
    }

    @Test
    void parseAndStore_withEmptyVariationsArray_doesNotSave() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "DEMOGS": {
                        "VARIATIONS": []
                    }
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifDemographicVariationService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauDemographicVariationRepositoryWrapper).deleteByEnquiryId(ENQUIRY_ID);
        verify(creditBureauDemographicVariationRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void parseAndStore_withMultipleVariationsUnderSameType_savesEach() throws Exception {
        // Arrange
        String json = """
            {
                "STANDARD-DATA": {
                    "DEMOGS": {
                        "VARIATIONS": [
                            {
                                "TYPE": "ADDRESS",
                                "VARIATION": [
                                    {"VALUE": "Address 1"},
                                    {"VALUE": "Address 2"}
                                ]
                            }
                        ]
                    }
                }
            }
            """;
        JsonNode reportData = mapper.readTree(json);

        // Act
        crifDemographicVariationService.parseAndStore(ENQUIRY_ID, reportData);

        // Assert
        verify(creditBureauDemographicVariationRepositoryWrapper, times(2)).saveWithException(any());
    }
}
