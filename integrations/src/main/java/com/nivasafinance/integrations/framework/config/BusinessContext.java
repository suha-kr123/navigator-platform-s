package com.nivasafinance.integrations.framework.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessContext {
    private String entityName;
    private Long entityId;
    private String businessPurpose;
}

