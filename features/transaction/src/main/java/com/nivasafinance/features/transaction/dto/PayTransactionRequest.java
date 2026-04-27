package com.nivasafinance.features.transaction.dto;

import com.nivasafinance.features.transaction.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayTransactionRequest {

    @NotNull(message = "{error.transaction.payment.mode.required}")
    private PaymentMode paymentMode;

    @NotNull(message = "{error.transaction.external.reference.required}")
    private String externalReference;

    private LocalDate paymentDate;

    private Map<String, Object> paymentData;

    private String remarks;
}
