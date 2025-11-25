package com.nivasafinance.features.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvisorDashboardFilters {
    List<String> status;
    List<String> office;
    LocalDateTime createdAtFrom;
    LocalDateTime createdAtTo;
    LocalDateTime lastLeadDateFrom;
    LocalDateTime lastLeadDateTo;
    List<String> segmentation;
    List<String> sourcingChannel;
}

