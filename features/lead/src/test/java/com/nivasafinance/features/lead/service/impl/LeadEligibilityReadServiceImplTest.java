package com.nivasafinance.features.lead.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.lead.dto.LeadBREEligibilityDetailResponse;
import com.nivasafinance.features.lead.dto.LeadBREResultResponse;
import com.nivasafinance.features.lead.dto.LeadEligibilityResponse;
import com.nivasafinance.features.lead.service.LeadBREResultReadService;
import com.nivasafinance.features.leadbre.enums.LeadBREResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadEligibilityReadServiceImplTest {

    @Mock
    private LeadBREResultReadService leadBREResultReadService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private LeadEligibilityReadServiceImpl leadEligibilityReadService;

    private UUID leadId;
    private UUID resultIdentifier;

    @BeforeEach
    void setUp() {
        leadId = UUID.randomUUID();
        resultIdentifier = UUID.randomUUID();
    }

    // ==================== getLatestEligibility() Tests ====================

    @Test
    void getLatestEligibility_withFullOutput_returnsPopulatedResponse() {
        String output = """
                {
                  "profile_match": {
                    "profile_name": "Salaried Premium",
                    "roi_min": 8.5,
                    "roi_max": 12.0
                  },
                  "loan_calculation": {
                    "eligible_emi": 25000.50,
                    "income_max_loan": 3000000,
                    "eligible_loan_amount": 2500000,
                    "min_tenure_months": 60,
                    "tenure_display_range": "5-20 years",
                    "emi_range_min": 15000,
                    "emi_range_max": 35000
                  },
                  "soft_offer_eligible": true,
                  "consumer_visible": false
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        assertTrue(result.isPresent());
        LeadEligibilityResponse response = result.get();
        assertEquals(resultIdentifier, response.getIdentifier());
        assertEquals(LeadBREResultStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("25000.5"), response.getEligibleEmi());
        assertEquals(new BigDecimal("3000000"), response.getIncomeMaxLoan());
        assertEquals(new BigDecimal("2500000"), response.getEligibleLoanAmount());
        assertEquals(60, response.getMinTenureMonths());
        assertEquals("5-20 years", response.getTenureDisplayRange());
        assertEquals(new BigDecimal("15000"), response.getEmiRangeMin());
        assertEquals(new BigDecimal("35000"), response.getEmiRangeMax());
        assertEquals(new BigDecimal("8.5"), response.getRoiMin());
        assertEquals(new BigDecimal("12.0"), response.getRoiMax());
        assertEquals(true, response.getSoftOfferEligible());
        assertEquals(false, response.getConsumerVisible());
        assertEquals("Salaried Premium", response.getProfileName());
        verify(leadBREResultReadService).getResults(leadId, "eligibility");
    }

    @Test
    void getLatestEligibility_withEmptyResults_returnsEmptyOptional() {
        // Arrange
        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(Collections.emptyList());

        // Act
        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when no BRE results exist");
        verify(leadBREResultReadService).getResults(leadId, "eligibility");
    }

    @Test
    void getLatestEligibility_withNullOutput_returnsResponseWithNullFields() {
        // Arrange
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(null).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        // Assert
        assertTrue(result.isPresent(), "Should return a response even with null output");
        LeadEligibilityResponse response = result.get();
        assertEquals(resultIdentifier, response.getIdentifier(), "Identifier should still be set");
        assertEquals(LeadBREResultStatus.SUCCESS, response.getStatus(), "Status should still be set");
        assertNull(response.getEligibleEmi(), "Eligible EMI should be null when output is null");
        assertNull(response.getEligibleLoanAmount(), "Eligible loan amount should be null when output is null");
    }

    @Test
    void getLatestEligibility_withMalformedJson_returnsResponseWithNullFields() {
        // Arrange
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.FAILED).output("{invalid json").build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        // Assert
        assertTrue(result.isPresent(), "Should return a response even with malformed JSON");
        LeadEligibilityResponse response = result.get();
        assertEquals(resultIdentifier, response.getIdentifier(), "Identifier should still be set");
        assertNull(response.getEligibleEmi(), "Eligible EMI should be null when JSON is malformed");
    }

    @Test
    void getLatestEligibility_withMultipleResults_returnsFirst() {
        // Arrange
        UUID secondIdentifier = UUID.randomUUID();
        LeadBREResultResponse first = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(null).build();
        LeadBREResultResponse second = LeadBREResultResponse.builder()
                .identifier(secondIdentifier).status(LeadBREResultStatus.FAILED).output(null).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(first, second));

        // Act
        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        // Assert
        assertTrue(result.isPresent(), "Should return the first result");
        assertEquals(resultIdentifier, result.get().getIdentifier(), "Should return the first BRE result's identifier");
    }

    // ==================== getLatestEligibilityDetail() Tests ====================

    @Test
    void getLatestEligibilityDetail_withFullOutput_returnsPopulatedResponse() {
        String output = """
                {
                  "location_tier": "Tier 1",
                  "bureau_track_score": 750,
                  "Crif_Credit_Score": 720.5,
                  "deed_ok": true,
                  "property_khata_group": "A",
                  "roof_type": "RCC",
                  "Crif_obligation": 5000,
                  "profile_match": {
                    "profile_name": "Salaried Premium",
                    "roi_min": 8.5,
                    "roi_max": 12.0,
                    "profile_match_status": "MATCHED",
                    "matching_profiles": ["Salaried Premium", "Salaried Standard"],
                    "consumer_visible_Profile": true
                  },
                  "loan_calculation": {
                    "property_value": 5000000,
                    "ltv_max_loan": 4000000,
                    "total_applicable_income": 150000,
                    "total_obligations": 20000,
                    "eligible_emi": 25000,
                    "income_max_loan": 3000000,
                    "eligible_loan_amount": 2500000,
                    "min_tenure_months": 60,
                    "tenure_display_range": "5-20 years",
                    "emi_range_min": 15000,
                    "emi_range_max": 35000,
                    "binding_constraint": "income"
                  },
                  "soft_offer_eligible": true,
                  "consumer_visible": false,
                  "Bank_ITR_GST_income_per": 15000
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        assertTrue(result.isPresent());
        LeadBREEligibilityDetailResponse response = result.get();
        assertEquals(resultIdentifier, response.getIdentifier());
        assertEquals(LeadBREResultStatus.SUCCESS, response.getStatus());

        // Root-level fields
        assertEquals("Tier 1", response.getLocationTier());
        assertEquals(750, response.getBureauTrackScore());
        assertEquals(new BigDecimal("720.5"), response.getCrifCreditScore());
        assertEquals(true, response.getDeedOk());
        assertEquals("A", response.getPropertyKhataGroup());
        assertEquals("RCC", response.getRoofType());
        assertEquals(new BigDecimal("5000"), response.getCrifObligation());
        assertEquals(true, response.getSoftOfferEligible());
        assertEquals(false, response.getConsumerVisible());

        // Profile match
        assertNotNull(response.getProfileMatch());
        assertEquals("Salaried Premium", response.getProfileMatch().getProfileName());
        assertEquals(new BigDecimal("8.5"), response.getProfileMatch().getRoiMin());
        assertEquals(new BigDecimal("12.0"), response.getProfileMatch().getRoiMax());
        assertEquals("MATCHED", response.getProfileMatch().getProfileMatchStatus());
        assertEquals(List.of("Salaried Premium", "Salaried Standard"), response.getProfileMatch().getMatchingProfiles());
        assertEquals(true, response.getProfileMatch().getConsumerVisibleProfile());
        assertEquals(new BigDecimal("15000"), response.getBankItrGstIncomePer());

        // Loan calculation
        assertNotNull(response.getLoanCalculation());
        assertEquals(new BigDecimal("5000000"), response.getLoanCalculation().getPropertyValue());
        assertEquals(new BigDecimal("4000000"), response.getLoanCalculation().getLtvMaxLoan());
        assertEquals(new BigDecimal("150000"), response.getLoanCalculation().getTotalApplicableIncome());
        assertEquals(new BigDecimal("20000"), response.getLoanCalculation().getTotalObligations());
        assertEquals(new BigDecimal("2500000"), response.getLoanCalculation().getEligibleLoanAmount());
        assertEquals(60, response.getLoanCalculation().getMinTenureMonths());
        assertEquals("5-20 years", response.getLoanCalculation().getTenureDisplayRange());
        assertEquals("income", response.getLoanCalculation().getBindingConstraint());
        verify(leadBREResultReadService).getResults(leadId, "eligibility");
    }

    @Test
    void getLatestEligibilityDetail_withEmptyResults_returnsEmptyOptional() {
        // Arrange
        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(Collections.emptyList());

        // Act
        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        // Assert
        assertTrue(result.isEmpty(), "Should return empty when no BRE results exist");
        verify(leadBREResultReadService).getResults(leadId, "eligibility");
    }

    @Test
    void getLatestEligibilityDetail_withNullOutput_returnsResponseWithNullNestedObjects() {
        // Arrange
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(null).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        assertTrue(result.isPresent());
        LeadBREEligibilityDetailResponse response = result.get();
        assertEquals(resultIdentifier, response.getIdentifier());
        assertNull(response.getLocationTier());
        assertNull(response.getBureauTrackScore());
        assertNull(response.getCrifCreditScore());
        assertNull(response.getDeedOk());
        assertNull(response.getPropertyKhataGroup());
        assertNull(response.getRoofType());
        assertNull(response.getCrifObligation());
        assertNull(response.getProfileMatch());
        assertNull(response.getLoanCalculation());
    }

    @Test
    void getLatestEligibilityDetail_withMalformedJson_returnsResponseWithNullNestedObjects() {
        // Arrange
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.FAILED).output("not valid json!").build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        // Assert
        assertTrue(result.isPresent(), "Should return a response even with malformed JSON");
        LeadBREEligibilityDetailResponse response = result.get();
        assertEquals(resultIdentifier, response.getIdentifier(), "Identifier should still be set");
        assertNull(response.getProfileMatch(), "Profile match should be null when JSON is malformed");
        assertNull(response.getLoanCalculation(), "Loan calculation should be null when JSON is malformed");
    }

    @Test
    void getLatestEligibilityDetail_withMissingNestedNodes_returnsResponseWithNullNestedObjects() {
        // Arrange
        String output = """
                {
                  "soft_offer_eligible": true,
                  "consumer_visible": true
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        // Assert
        assertTrue(result.isPresent(), "Should return a response when nested nodes are absent");
        LeadBREEligibilityDetailResponse response = result.get();
        assertNull(response.getProfileMatch(), "Profile match should be null when node is missing from JSON");
        assertNull(response.getLoanCalculation(), "Loan calculation should be null when node is missing from JSON");
        assertEquals(true, response.getSoftOfferEligible(), "Root-level soft_offer_eligible should still be parsed");
        assertEquals(true, response.getConsumerVisible(), "Root-level consumer_visible should still be parsed");
    }

    // ==================== getLatestEligibility() – branch coverage ====================

    @Test
    void getLatestEligibility_withMissingLoanCalculation_returnsNullFields() {
        String output = """
                {
                  "soft_offer_eligible": false,
                  "consumer_visible": true
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        assertTrue(result.isPresent());
        LeadEligibilityResponse response = result.get();
        assertNull(response.getEligibleEmi());
        assertNull(response.getEligibleLoanAmount());
        assertNull(response.getMinTenureMonths());
        assertNull(response.getRoiMin(), "roi_min should be null when profile_match is missing");
        assertNull(response.getRoiMax(), "roi_max should be null when profile_match is missing");
        assertNull(response.getProfileName(), "profileName should be null when profile_match is missing");
        assertEquals(false, response.getSoftOfferEligible());
        assertEquals(true, response.getConsumerVisible());
    }

    @Test
    void getLatestEligibility_withNullJsonValues_returnsNullFields() {
        // Arrange
        String output = """
                {
                  "loan_calculation": {
                    "eligible_emi": null,
                    "eligible_loan_amount": null,
                    "min_tenure_months": null,
                    "tenure_display_range": null,
                    "emi_range_min": null,
                    "emi_range_max": null
                  },
                  "soft_offer_eligible": null,
                  "consumer_visible": null
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadEligibilityResponse> result = leadEligibilityReadService.getLatestEligibility(leadId);

        // Assert
        assertTrue(result.isPresent());
        LeadEligibilityResponse response = result.get();
        assertNull(response.getEligibleEmi(), "Null JSON value should map to null");
        assertNull(response.getEligibleLoanAmount(), "Null JSON value should map to null");
        assertNull(response.getMinTenureMonths(), "Null JSON value should map to null");
        assertNull(response.getTenureDisplayRange(), "Null JSON value should map to null");
        assertNull(response.getSoftOfferEligible(), "Null JSON value should map to null");
    }

    // ==================== getLatestEligibilityDetail() – branch coverage ====================

    @Test
    void getLatestEligibilityDetail_withProfileMatchOnly_returnsProfileMatchAndNullLoanCalculation() {
        // Arrange
        String output = """
                {
                  "profile_match": {
                    "profile_name": "Self Employed",
                    "profile_match_status": "NOT_MATCHED",
                    "matching_profiles": null,
                    "consumer_visible_Profile": false
                  },
                  "soft_offer_eligible": false
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        // Assert
        assertTrue(result.isPresent());
        LeadBREEligibilityDetailResponse response = result.get();
        assertNotNull(response.getProfileMatch(), "Profile match should be populated");
        assertEquals("Self Employed", response.getProfileMatch().getProfileName());
        assertEquals("NOT_MATCHED", response.getProfileMatch().getProfileMatchStatus());
        assertNull(response.getProfileMatch().getMatchingProfiles(),
                "Null JSON array should map to null");
        assertNull(response.getLoanCalculation(), "Loan calculation should be null when node is missing");
    }

    @Test
    void getLatestEligibilityDetail_withNonArrayMatchingProfiles_returnsNull() {
        // Arrange
        String output = """
                {
                  "profile_match": {
                    "matching_profiles": "not_an_array"
                  }
                }
                """;
        LeadBREResultResponse breResult = LeadBREResultResponse.builder()
                .identifier(resultIdentifier).status(LeadBREResultStatus.SUCCESS).output(output).build();

        when(leadBREResultReadService.getResults(leadId, "eligibility")).thenReturn(List.of(breResult));

        // Act
        Optional<LeadBREEligibilityDetailResponse> result =
                leadEligibilityReadService.getLatestEligibilityDetail(leadId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getProfileMatch().getMatchingProfiles(),
                "Non-array matching_profiles should map to null");
    }
}
