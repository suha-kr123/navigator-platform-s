package com.nivasafinance.features.leadotp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadSendOtpRequest {
    @NotBlank(message = "mobileNumber is required")
    private String mobileNumber;
}
