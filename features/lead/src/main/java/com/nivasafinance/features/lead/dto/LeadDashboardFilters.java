package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeadDashboardFilters {
    List<String> leadOwner;
    LocalDateTime lastActivityFrom;
    LocalDateTime lastActivityTo;
    LocalDateTime leadCreatedFrom;
    LocalDateTime leadCreatedTo;
    List<String> lastUpdatedBy;
    List<String> status;
    BigDecimal minAmount;
    BigDecimal maxAmount;
    List<String> branch;

    //@Builder.Default
    //Boolean leadThroughAdvisors = false;


    //TODO : advisors, stages, subStages, add next task filter
}
