package com.nivasafinance.features.creditbureau.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface CreditBureauReportParser {
    /**
     * Parses the credit bureau report JSON and stores the data.
     * 
     * @param enquiryId The enquiry ID
     * @param creditBureauReportJson The credit bureau report JSON
     * @return true if meaningful data was found and stored, false otherwise (NO_HIT)
     */
    boolean parse(Long enquiryId, JsonNode creditBureauReportJson);
}
