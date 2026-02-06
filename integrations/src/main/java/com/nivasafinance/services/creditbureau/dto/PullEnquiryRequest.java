package com.nivasafinance.services.creditbureau.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PullEnquiryRequest {
    private Long personId;
    private Long enquiryId;
    private String enquiryIdentifier;
    private CreditBureauPersonData personData;
}
