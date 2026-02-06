package com.nivasafinance.features.creditbureau.enums;

public enum CreditBureauEnquiryStatus {
    INITIATED,
    PROCESSING,
    SUCCESS,
    NO_HIT,
    FAILED,
    DATA_MISMATCH;

    public static CreditBureauEnquiryStatus fromCreditBureauEnquiryStatus(com.nivasafinance.services.creditbureau.dto.CreditBureauEnquiryStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INITIATED -> CreditBureauEnquiryStatus.INITIATED;
            case PROCESSING -> CreditBureauEnquiryStatus.PROCESSING;
            case SUCCESS -> CreditBureauEnquiryStatus.SUCCESS;
            case NO_HIT -> CreditBureauEnquiryStatus.NO_HIT;
            case FAILED -> CreditBureauEnquiryStatus.FAILED;
            case DATA_MISMATCH -> CreditBureauEnquiryStatus.DATA_MISMATCH;
        };
    }
}
