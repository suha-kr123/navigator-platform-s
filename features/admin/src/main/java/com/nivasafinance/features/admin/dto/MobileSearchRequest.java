package com.nivasafinance.features.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileSearchRequest {

    @NotBlank(message = "Mobile number is mandatory")
    private String mobileNumber;
}
