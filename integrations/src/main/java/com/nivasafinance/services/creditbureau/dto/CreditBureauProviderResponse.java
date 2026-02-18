package com.nivasafinance.services.creditbureau.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBureauProviderResponse {
    private String enquiryId;
    private CreditBureauEnquiryStatus status;
    private String reportId;
    private String stage1Request;
    private String creditBureauReportJson;
    private Map<String, Object> reportDetails;
    private String error;
}

