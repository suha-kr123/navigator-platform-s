package com.nivasafinance.common.base.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MasterLanguageData {
    @JsonProperty("default")
    private String defaultValue;

    public Map<String, String> toMap() {
        return Map.of("default", defaultValue);
    }
}

