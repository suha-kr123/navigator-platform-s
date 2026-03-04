package com.nivasafinance.features.advisor.dto.self;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelfSendOtpRequest {
    @NotBlank(message = "Mobile number is required")
    private String mobileNo;
}
