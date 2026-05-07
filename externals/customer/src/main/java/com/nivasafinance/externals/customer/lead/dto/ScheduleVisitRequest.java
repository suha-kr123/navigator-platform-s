package com.nivasafinance.externals.customer.lead.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleVisitRequest {

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    @NotNull(message = "Visit time is required")
    private LocalTime visitTime;
}
