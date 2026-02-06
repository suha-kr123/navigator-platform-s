package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.features.creditbureau.entity.CreditBureauDemographicVariation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemographicVariationResponse {
    private UUID identifier;
    private String variationType;
    private String variationValue;
    private LocalDate reportedDate;
    private LocalDate firstReportedDate;
    private String loanTypeAssociated;
    private String sourceIndicator;

    public static DemographicVariationResponse toDemographicVariationResponse(CreditBureauDemographicVariation demographicVariation) {
        if (demographicVariation == null) {
            return null;
        }
        return DemographicVariationResponse.builder()
                .identifier(demographicVariation.getIdentifier())
                .variationType(demographicVariation.getVariationType())
                .variationValue(demographicVariation.getVariationValue())
                .reportedDate(demographicVariation.getReportedDate())
                .firstReportedDate(demographicVariation.getFirstReportedDate())
                .loanTypeAssociated(demographicVariation.getLoanTypeAssociated())
                .sourceIndicator(demographicVariation.getSourceIndicator())
                .build();
    }
}
