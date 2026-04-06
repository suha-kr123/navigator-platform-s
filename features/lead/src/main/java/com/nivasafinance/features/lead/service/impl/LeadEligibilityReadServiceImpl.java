package com.nivasafinance.features.lead.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.features.lead.dto.LeadBREEligibilityDetailResponse;
import com.nivasafinance.features.lead.dto.LeadBREResultResponse;
import com.nivasafinance.features.lead.dto.LeadEligibilityResponse;
import com.nivasafinance.features.lead.service.LeadBREResultReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadEligibilityReadServiceImpl implements LeadEligibilityReadService {

    private static final String ELIGIBILITY_CONFIG = "eligibility";

    private final LeadBREResultReadService leadBREResultReadService;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<LeadEligibilityResponse> getLatestEligibility(UUID leadId) {
        return leadBREResultReadService.getResults(leadId, ELIGIBILITY_CONFIG).stream()
                .findFirst()
                .map(this::toEligibilityResponse);
    }

    @Override
    public Optional<LeadBREEligibilityDetailResponse> getLatestEligibilityDetail(UUID leadId) {
        return leadBREResultReadService.getResults(leadId, ELIGIBILITY_CONFIG).stream()
                .findFirst()
                .map(this::toEligibilityDetailResponse);
    }

    private LeadEligibilityResponse toEligibilityResponse(LeadBREResultResponse result) {
        LeadEligibilityResponse.LeadEligibilityResponseBuilder builder = LeadEligibilityResponse.builder()
                .identifier(result.getIdentifier())
                .status(result.getStatus());

        if (result.getOutput() != null) {
            try {
                JsonNode root = objectMapper.readTree(result.getOutput());
                JsonNode loanCalc = root.path("loan_calculation");

                builder.eligibleEmi(toBigDecimal(loanCalc.get("eligible_emi")))
                        .incomeMaxLoan(toBigDecimal(loanCalc.get("income_max_loan")))
                        .eligibleLoanAmount(toBigDecimal(loanCalc.get("eligible_loan_amount")))
                        .minTenureMonths(toInteger(loanCalc.get("min_tenure_months")))
                        .tenureDisplayRange(toText(loanCalc.get("tenure_display_range")))
                        .emiRangeMin(toBigDecimal(loanCalc.get("emi_range_min")))
                        .emiRangeMax(toBigDecimal(loanCalc.get("emi_range_max")))
                        .softOfferEligible(toBoolean(root.get("soft_offer_eligible")))
                        .consumerVisible(toBoolean(root.get("consumer_visible")));
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse eligibility output for identifier {}", result.getIdentifier(), e);
            }
        }

        return builder.build();
    }

    private LeadBREEligibilityDetailResponse toEligibilityDetailResponse(LeadBREResultResponse result) {
        LeadBREEligibilityDetailResponse.LeadBREEligibilityDetailResponseBuilder builder =
                LeadBREEligibilityDetailResponse.builder()
                        .identifier(result.getIdentifier())
                        .status(result.getStatus());

        if (result.getOutput() != null) {
            try {
                JsonNode root = objectMapper.readTree(result.getOutput());

                JsonNode pm = root.path("profile_match");
                if (!pm.isMissingNode()) {
                    builder.profileMatch(LeadBREEligibilityDetailResponse.ProfileMatch.builder()
                            .profileName(toText(pm.get("profile_name")))
                            .roiMin(toBigDecimal(pm.get("roi_min")))
                            .roiMax(toBigDecimal(pm.get("roi_max")))
                            .profileMatchStatus(toText(pm.get("profile_match_status")))
                            .matchingProfiles(toStringList(pm.get("matching_profiles")))
                            .consumerVisible(toBoolean(pm.get("consumer_visible")))
                            .build());
                }

                JsonNode lc = root.path("loan_calculation");
                if (!lc.isMissingNode()) {
                    builder.loanCalculation(LeadBREEligibilityDetailResponse.LoanCalculation.builder()
                            .propertyValue(toBigDecimal(lc.get("property_value")))
                            .ltvMaxLoan(toBigDecimal(lc.get("ltv_max_loan")))
                            .totalApplicableIncome(toBigDecimal(lc.get("total_applicable_income")))
                            .totalObligations(toBigDecimal(lc.get("total_obligations")))
                            .eligibleEmi(toBigDecimal(lc.get("eligible_emi")))
                            .incomeMaxLoan(toBigDecimal(lc.get("income_max_loan")))
                            .eligibleLoanAmount(toBigDecimal(lc.get("eligible_loan_amount")))
                            .minTenureMonths(toInteger(lc.get("min_tenure_months")))
                            .tenureDisplayRange(toText(lc.get("tenure_display_range")))
                            .emiRangeMin(toBigDecimal(lc.get("emi_range_min")))
                            .emiRangeMax(toBigDecimal(lc.get("emi_range_max")))
                            .bindingConstraint(toText(lc.get("binding_constraint")))
                            .build());
                }

                builder.softOfferEligible(toBoolean(root.get("soft_offer_eligible")))
                        .consumerVisible(toBoolean(root.get("consumer_visible")));

            } catch (JsonProcessingException e) {
                log.warn("Failed to parse BRE eligibility output for identifier {}", result.getIdentifier(), e);
            }
        }

        return builder.build();
    }

    private BigDecimal toBigDecimal(JsonNode node) {
        return node != null && !node.isNull() ? node.decimalValue() : null;
    }

    private Integer toInteger(JsonNode node) {
        return node != null && !node.isNull() ? node.intValue() : null;
    }

    private String toText(JsonNode node) {
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private Boolean toBoolean(JsonNode node) {
        return node != null && !node.isNull() ? node.booleanValue() : null;
    }

    private List<String> toStringList(JsonNode node) {
        if (node == null || !node.isArray()) {
            return null;
        }
        List<String> list = new ArrayList<>();
        node.forEach(n -> list.add(n.asText()));
        return list;
    }
}
