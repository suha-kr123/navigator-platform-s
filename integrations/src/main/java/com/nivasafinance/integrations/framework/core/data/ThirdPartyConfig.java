package com.nivasafinance.integrations.framework.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThirdPartyConfig {
    private UUID id;
    private String name;
    private String provider;
    private Map<String, String> configurations;
}

