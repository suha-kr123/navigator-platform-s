package com.nivasafinance.features.advisor.dto.self;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfPaymentDetails {
    private String paymentMode;
    private String externalReference;
    private String paymentStatus;
    private LocalDate paymentDate;
    private Map<String, Object> paymentData;
}
