package com.nivasafinance.features.advisor.dto.self;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvisorSelfLeadCreateRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    private String mobileNumber;

    private String firstName;
    private String middleName;
    private String lastName;
    private String stateCode;
    private String countryCode;
    private String districtCode;

    private BigDecimal requestedAmount;

    private String product;

    private BigDecimal monthlyFamilyIncome;
}
