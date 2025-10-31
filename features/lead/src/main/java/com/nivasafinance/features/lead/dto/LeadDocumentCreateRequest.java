package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.lead.enums.LeadDocumentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDocumentCreateRequest {
    
    @NotBlank(message = "Document name is required")
    private String name;
    
    private List<String> tags;
    
    private LeadDocumentStatus status;
}

