package com.nivasafinance.services.creditbureau.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBureauEnquiryRequest {
    private Long personId;
    private Long contactId;
    private String phoneNumber;
}

