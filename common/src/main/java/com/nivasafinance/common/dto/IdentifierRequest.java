package com.nivasafinance.common.dto;

import com.nivasafinance.common.enums.IdentifierType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdentifierRequest {
    private IdentifierType type;
    private String identifier;
}
