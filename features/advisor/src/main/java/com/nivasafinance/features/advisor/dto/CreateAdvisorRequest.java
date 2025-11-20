package com.nivasafinance.features.advisor.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdvisorRequest {

    @Valid
    @NotNull(message = "Mobile number details are mandatory")
    private MobileNumberDetails mobileNumberDetails;

    private PersonalDetails personalDetails;
}
