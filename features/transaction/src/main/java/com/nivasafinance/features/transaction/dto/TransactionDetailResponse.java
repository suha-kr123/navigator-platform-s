package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {

    private UUID identifier;
    private String status;
    private BigDecimal amount;
    private String idempotencyKey;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<Map<String, Object>> remarks;
    private List<TransactionEventResponse> events;
    private List<TransactionPaymentResponse> payments;
    private LeadTransactionContext leadContext;
}
