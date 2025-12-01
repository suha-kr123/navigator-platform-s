package com.nivasafinance.features.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherDetails {
    
    private String occupationType;
    private String occupation;
    private Long lastCallId;
    private LocalTime preferredCallStartTime;
    private LocalTime preferredCallEndTime;
}

