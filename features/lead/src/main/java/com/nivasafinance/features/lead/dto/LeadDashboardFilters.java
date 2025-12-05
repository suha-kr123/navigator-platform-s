package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    List<String> substatus;
    BigDecimal minAmount;
    BigDecimal maxAmount;
    List<String> branch;
    String lastCallDirection;
    String lastCallStatus;
    List<String> stageKey;
    List<String> subStageKey;
    List<String> stageAssignedTo;
    LocalDateTime stageAssignedAtFrom;
    LocalDateTime stageAssignedAtTo;
    LocalDateTime stageEnteredAtFrom;
    LocalDateTime stageEnteredAtTo;
    LocalDateTime onHoldDateFrom;
    LocalDateTime onHoldDateTo;
    List<String> onHoldReason;
    LocalDate onHoldFollowUpDateFrom;
    LocalDate onHoldFollowUpDateTo;


    //TODO : advisors, add next task filter
}
