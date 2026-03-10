package com.nivasafinance.common.base.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MasterLanguageData {

    @JsonProperty("default")
    private String defaultValue;

    private String kn;

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        if (defaultValue != null) {
            map.put("default", defaultValue);
        }
        if (kn != null) {
            map.put("kn", kn);
        }
        return Map.copyOf(map);
    }

    public static MasterLanguageData fromMap(Map<String, String> map) {
        if (map == null) {
            return new MasterLanguageData(null, null);
        }
        return MasterLanguageData.builder()
                .defaultValue(map.get("default"))
                .kn(map.get("kn"))
                .build();
    }
}
