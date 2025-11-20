package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateLeadCallRequest {

    @NotNull(message = "Contact identifier is required")
    private UUID contactIdentifier;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}
