package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.IdentifierType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdentifierRequest {
    @NotNull
    private IdentifierType type;
    @NotBlank
    private String identifier;
}
