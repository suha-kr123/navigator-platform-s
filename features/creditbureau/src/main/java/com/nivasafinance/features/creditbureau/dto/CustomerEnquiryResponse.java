package com.nivasafinance.features.creditbureau.dto;

import com.nivasafinance.features.creditbureau.entity.CreditBureauCustomerEnquiry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEnquiryResponse {
    private UUID identifier;
    private String lenderName;
    private LocalDate inquiryDate;
    private String ownershipType;
    private String creditInquiryPurposeType;
    private BigDecimal inquiryAmount;

    public static CustomerEnquiryResponse toCustomerEnquiryResponse(CreditBureauCustomerEnquiry customerEnquiry) {
        if (customerEnquiry == null) {
            return null;
        }
        return CustomerEnquiryResponse.builder()
                .identifier(customerEnquiry.getIdentifier())
                .lenderName(customerEnquiry.getLenderName())
                .inquiryDate(customerEnquiry.getInquiryDate())
                .ownershipType(customerEnquiry.getOwnershipType())
                .creditInquiryPurposeType(customerEnquiry.getCreditInquiryPurposeType())
                .inquiryAmount(customerEnquiry.getInquiryAmount())
                .build();
    }
}