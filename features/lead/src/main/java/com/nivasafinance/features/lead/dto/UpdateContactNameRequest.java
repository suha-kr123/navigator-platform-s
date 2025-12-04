package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateContactNameRequest {
    private String firstName;
    private String middleName;
    private String lastName;
}

