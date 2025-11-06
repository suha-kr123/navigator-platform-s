package com.nivasafinance.common.base.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MasterLanguageData {
    @JsonProperty("default")
    private String defaultValue;
}

