package com.nivasafinance.features.master.codemaster.dto;

import java.util.List;
import java.util.Map;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.AssertTrue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterCodeWithValuesRequest {

    private String key;
    private Map<String, String> nameMap;
    private Map<String, String> descriptionMap;
    private Long parentId;
    List<MasterCodeValueRequest> masterCodeValueRequests;

    @AssertTrue(message = "nameMap must contain default key 'default'")
    public boolean isNameMapValid() {
        return nameMap.containsKey("default");
    }

    @AssertTrue(message = "descriptionMap must contain default key 'default'")
    public boolean isDescriptionMapValid() {
        return descriptionMap.containsKey("default");
    }

    public MasterCode toEntity(String key) {
        return MasterCode.builder()
                .key(key)
                .name(MasterLanguageData.builder()
                        .defaultValue(nameMap.get("default"))
                        .build())
                .description(MasterLanguageData.builder()
                        .defaultValue(descriptionMap.get("default"))
                        .build())
                .parentId(parentId)
                .isSystemDefined(false)
                .build();
    }
}
