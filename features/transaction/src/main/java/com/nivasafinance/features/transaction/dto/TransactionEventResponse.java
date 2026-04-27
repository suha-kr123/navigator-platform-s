package com.nivasafinance.features.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEventResponse {

    private UUID identifier;
    private String eventType;
    private String actorUsername;
    private String remarks;
    private Map<String, Object> paymentDetails;
    private LocalDateTime eventTimestamp;
}
