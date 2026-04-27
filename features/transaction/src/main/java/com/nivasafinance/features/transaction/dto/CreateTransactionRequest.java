package com.nivasafinance.features.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {

    @NotNull(message = "{error.transaction.amount.required}")
    private BigDecimal amount;

    @NotBlank(message = "{error.transaction.idempotency.key.required}")
    private String idempotencyKey;

    private String remarks;
}
