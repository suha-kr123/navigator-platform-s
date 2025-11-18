package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadSearchRequest {

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    String mobileNumber;
}
