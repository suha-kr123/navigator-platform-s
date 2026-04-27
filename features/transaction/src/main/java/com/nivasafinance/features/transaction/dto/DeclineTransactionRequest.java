package com.nivasafinance.features.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclineTransactionRequest {

    @NotBlank(message = "{error.transaction.remarks.required}")
    private String remarks;
}
