package com.nivasafinance.features.creditbureau.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrifReportParserTest {

    @Mock
    private CrifSummaryService crifSummaryService;

    @Mock
    private CrifAttributeService crifAttributeService;

    @Mock
    private CrifTradelineService crifTradelineService;

    @Mock
    private CrifCustomerEnquiryService crifCustomerEnquiryService;

    @Mock
    private CrifDemographicVariationService crifDemographicVariationService;

    @Mock
    private CrifTrendsService crifTrendsService;

    @InjectMocks
    private CrifReportParser crifReportParser;

    private static final Long ENQUIRY_ID = 1L;
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== parse() Tests ====================

    @Test
    void parse_whenB2cReportMissing_returnsFalse() throws Exception {
        // Arrange
        JsonNode json = mapper.readTree("{}");

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, json);

        // Assert
        assertFalse(result, "Should return false when B2C-REPORT is missing");
        verifyNoInteractions(crifSummaryService, crifAttributeService, crifTradelineService,
                crifCustomerEnquiryService, crifDemographicVariationService, crifTrendsService);
    }

    @Test
    void parse_whenReportDataMissing_returnsFalse() throws Exception {
        // Arrange
        JsonNode json = mapper.readTree("{\"B2C-REPORT\":{}}");

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, json);

        // Assert
        assertFalse(result, "Should return false when REPORT-DATA is missing");
    }

    @Test
    void parse_withScoreData_callsSummaryServiceAndReturnsTrue() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "STANDARD-DATA": {
                            "SCORE": [{"VALUE": "750", "NAME": "CRIF"}]
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when SCORE data is found");
        verify(crifSummaryService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withTradelines_callsTradelineServiceAndReturnsTrue() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "STANDARD-DATA": {
                            "TRADELINES": [{"ACCT-NUMBER": "123"}]
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when TRADELINES data is found");
        verify(crifTradelineService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withInquiryHistory_callsCustomerEnquiryServiceAndReturnsTrue() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "STANDARD-DATA": {
                            "INQUIRY-HISTORY": [{"LENDER-NAME": "Bank"}]
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when INQUIRY-HISTORY is found");
        verify(crifCustomerEnquiryService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withDemogs_callsDemographicVariationServiceAndReturnsTrue() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "STANDARD-DATA": {
                            "DEMOGS": {"VARIATIONS": []}
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when DEMOGS is found");
        verify(crifDemographicVariationService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withEmploymentDetails_callsDemographicVariationService() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "STANDARD-DATA": {
                            "EMPLOYMENT-DETAILS": [{"EMPLOYMENT-DETAIL": {}}]
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when EMPLOYMENT-DETAILS is found");
        verify(crifDemographicVariationService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withTrendsData_callsTrendsServiceAndReturnsTrue() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "TRENDS": {
                            "DATES": "30-09-2025|31-10-2025",
                            "VALUES": "720|730"
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when TRENDS with data is found");
        verify(crifTrendsService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withTrendsEmptyValues_doesNotCallTrendsService() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "TRENDS": {
                            "DATES": "",
                            "VALUES": ""
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        verifyNoInteractions(crifTrendsService);
    }

    @Test
    void parse_withAccountsSummaryOnly_callsAttributeServiceAndReturnsTrue() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {
                        "ACCOUNTS-SUMMARY": {
                            "PRIMARY-ACCOUNTS-SUMMARY": {"NUMBER-OF-ACCOUNTS": 3}
                        }
                    }
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertTrue(result, "Should return true when ACCOUNTS-SUMMARY exists even without other data");
        verify(crifAttributeService).parseAndStore(eq(ENQUIRY_ID), any(JsonNode.class));
    }

    @Test
    void parse_withNoMeaningfulData_returnsFalse() throws Exception {
        // Arrange
        String json = """
            {
                "B2C-REPORT": {
                    "REPORT-DATA": {}
                }
            }
            """;
        JsonNode jsonNode = mapper.readTree(json);

        // Act
        boolean result = crifReportParser.parse(ENQUIRY_ID, jsonNode);

        // Assert
        assertFalse(result, "Should return false when no meaningful data sections exist (NO_HIT)");
    }
}
