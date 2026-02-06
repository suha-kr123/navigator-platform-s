package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnquiryStatusResponse {
    private UUID identifier;
    private CreditBureauEnquiryStatus status;

    public static EnquiryStatusResponse toStatusResponse(CreditBureauEnquiry enquiry) {
        if(enquiry == null) {
            return null;
        }
        return EnquiryStatusResponse.builder()
                .identifier(enquiry.getIdentifier())
                .status(enquiry.getStatus())
                .build();
    }
}