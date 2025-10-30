package com.nivasafinance.features.leadlender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadLenderResponse {
    
    private UUID lenderIdentifier;
}
