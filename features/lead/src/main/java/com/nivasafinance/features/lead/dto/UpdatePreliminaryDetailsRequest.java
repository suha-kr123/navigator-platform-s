package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreliminaryDetailsRequest {
    private Map<String, String> preliminaryDetails;
}

