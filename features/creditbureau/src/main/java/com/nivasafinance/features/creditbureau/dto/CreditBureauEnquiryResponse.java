package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry.ReportDetails;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBureauEnquiryResponse {
    private Long id;
    private UUID identifier;
    private String reportId;
    private CreditBureauEnquiryStatus status;
    private Long consentId;
    private UUID consentIdentifier;
    private String provider;
    private String error;
    private String requestJson;
    private String responseJson;
    private UUID reportDocumentIdentifier;
    private ReportDetails reportDetails;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public static CreditBureauEnquiryResponse toCbEnquiryResponse(CreditBureauEnquiry creditBureauEnquiry) {
        if (creditBureauEnquiry == null) {
            return null;
        }
        return CreditBureauEnquiryResponse.builder()
                .id(creditBureauEnquiry.getId())
                .identifier(creditBureauEnquiry.getIdentifier())
                .reportId(creditBureauEnquiry.getReportId())
                .status(creditBureauEnquiry.getStatus())
                .consentId(creditBureauEnquiry.getConsentId())
                .provider(creditBureauEnquiry.getProvider())
                .error(creditBureauEnquiry.getError())
                .requestJson(creditBureauEnquiry.getRequestJson())
                .responseJson(creditBureauEnquiry.getResponseJson())
                .reportDocumentIdentifier(creditBureauEnquiry.getReportDocumentIdentifier())
                .reportDetails(creditBureauEnquiry.getReportDetails())
                .createdAt(creditBureauEnquiry.getCreatedAt())
                .createdBy(creditBureauEnquiry.getCreatedBy())
                .updatedAt(creditBureauEnquiry.getUpdatedAt())
                .updatedBy(creditBureauEnquiry.getUpdatedBy())
                .build();
    }
}

