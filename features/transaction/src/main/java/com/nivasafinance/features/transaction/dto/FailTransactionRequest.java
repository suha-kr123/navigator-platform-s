package com.nivasafinance.features.transaction.dto;

import com.nivasafinance.features.transaction.enums.PaymentMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailTransactionRequest {

    @NotBlank(message = "{error.transaction.remarks.required}")
    private String remarks;

    private PaymentMode paymentMode;

    private Map<String, Object> paymentData;
}
