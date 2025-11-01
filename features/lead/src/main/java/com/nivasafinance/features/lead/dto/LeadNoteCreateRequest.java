package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadNoteCreateRequest {
    
    @NotBlank(message = "Note title is required")
    private String title;
    
    @NotBlank(message = "Note content is required")
    private String content;
}

