package com.nivasafinance.features.transaction.dto;

import com.nivasafinance.features.transaction.enums.DomainType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadTransactionRequest {

    @NotNull(message = "{error.transaction.amount.required}")
    private BigDecimal amount;

    @NotNull(message = "{error.transaction.domain.type.required}")
    private DomainType domainType;

    private String remarks;
}
