package com.nivasafinance.features.master.codemaster.dto;

import java.util.Map;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MasterCodeValueRequest {

    private String key;
    Map<String, String> valueMap;
    Map<String, String> descriptionMap;

    @AssertTrue(message = "valueMap must contain default key 'default'")
    public boolean isValueMapValid() {
        return valueMap.containsKey("default");
    }

    @AssertTrue(message = "descriptionMap must contain default key 'default'")
    public boolean isDescriptionMapValid() {
        return descriptionMap.containsKey("default");
    }

    public MasterCodeValue toEntity(String key, String masterCodeKey) {
        return MasterCodeValue.builder()
                .key(key)
                .codeKey(masterCodeKey)
                .value(MasterLanguageData.builder()
                        .defaultValue(valueMap.get("default"))
                        .build())
                .description(MasterLanguageData.builder()
                        .defaultValue(descriptionMap.get("default"))
                        .build())
                .build();
    }
}
