package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[^\\\\/]*\\.[^\\\\/]*$", message = "Invalid file name. Example: file.jpg")
    private String name;
    
    private List<String> tags;
}

