package com.nivasafinance.features.creditbureau.service;

public interface CreditBureauDerivedAttributeWriteService {

    /**
     * Runs configured Redash queries and persists new derived rows for the enquiry.
     *
     * @param enquiryId credit bureau enquiry id (Redash parameter {@code enquiryId})
     * @param leadId      stored inside {@code data_ext} as {@code leadId}; may be null
     */
    void saveDerivedAttributesForEnquiry(Long enquiryId, Long leadId);
}
