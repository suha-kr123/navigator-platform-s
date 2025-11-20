package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreliminaryDetailsResponse {
    private Map<String, String> whatsAppFormDetails;
    private Boolean isWhatsAppDIYFormCompleted;
    private BigDecimal monthlyFamilyIncome;
}

