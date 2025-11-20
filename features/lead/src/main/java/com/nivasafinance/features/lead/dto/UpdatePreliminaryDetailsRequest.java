package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreliminaryDetailsRequest {
    private Map<String, String> whatsAppFormDetails;
    private Boolean isWhatsAppDIYFormCompleted;
    private BigDecimal monthlyFamilyIncome;
}

