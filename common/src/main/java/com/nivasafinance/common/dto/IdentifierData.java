package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.IdentifierType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IdentifierData {
    private UUID id;
    private IdentifierType type;
    private String identifier;
}
