package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPaymentResponse {

    private UUID identifier;
    private String paymentMode;
    private String externalReference;
    private String paymentStatus;
    private LocalDate paymentDate;
    private String recordedBy;
    private Map<String, Object> paymentData;
    private LocalDateTime createdAt;
}
