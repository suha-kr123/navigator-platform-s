package com.nivasafinance.features.master.codemaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterCodeValueIconUploadRequest {

    @NotBlank(message = "masterCodeKey is required")
    private String masterCodeKey;

    @NotBlank(message = "masterCodeValueKey is required")
    private String masterCodeValueKey;

    @NotBlank(message = "context is required")
    @Pattern(regexp = "^(default|crm|web|app)$", message = "context must be one of: default, crm, web, app")
    private String context;

    @NotBlank(message = "size is required")
    @Pattern(regexp = "^(small|medium|large|xl|xxl)$", message = "size must be one of: small, medium, large, xl, xxl")
    private String size;
}
