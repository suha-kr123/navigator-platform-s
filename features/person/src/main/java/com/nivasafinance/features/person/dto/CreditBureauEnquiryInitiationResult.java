package com.nivasafinance.features.person.dto;

import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.CompletableFuture;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBureauEnquiryInitiationResult {

    private CreditBureauEnquiryResponse response;
    private CompletableFuture<CreditBureauEnquiryResponse> asyncPullFuture;
}
