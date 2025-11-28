package com.nivasafinance.features.advisor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateAdvisorCallRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}
