package com.nivasafinance.features.transaction.dto;

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
public class TransactionSearchRequest {

    @NotBlank(message = "{error.transaction.mobile.required}")
    @Pattern(regexp = "^[0-9]{10}$", message = "{error.transaction.mobile.invalid}")
    private String mobileNumber;
}
