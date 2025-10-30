package com.nivasafinance.features.leadlender.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadLenderRequest {
    
    @NotBlank(message = "Lender key is required")
    private String lenderKey;
}
