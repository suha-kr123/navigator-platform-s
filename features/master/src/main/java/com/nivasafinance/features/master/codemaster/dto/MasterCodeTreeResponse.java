package com.nivasafinance.features.master.codemaster.dto;

import java.util.List;
import java.util.Map;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterCodeTreeResponse {

    private Long id;
    private String key;
    private Map<String, String> valueMap;
    private Map<String, String> descriptionMap;
    private Boolean isSystemDefined;
    private Long parentId;

    private List<MasterCodeTreeResponse> children;

    public static MasterCodeTreeResponse from(MasterCode masterCode, List<MasterCodeTreeResponse> children) {
        return MasterCodeTreeResponse.builder()
                .id(masterCode.getId())
                .key(masterCode.getKey())
                .valueMap(masterCode.getName().toMap())
                .descriptionMap(masterCode.getDescription().toMap())
                .isSystemDefined(masterCode.getIsSystemDefined())
                .parentId(masterCode.getParentId())
                .children(children)
                .build();
    }

    public MasterCodeValue toEntity(String key, String codeKey) {
        return MasterCodeValue.builder()
                .key(key)
                .codeKey(codeKey)
                .isActive(true)
                .value(MasterLanguageData.builder()
                        .defaultValue(valueMap.get("default"))
                        .build())
                .description(MasterLanguageData.builder()
                        .defaultValue(descriptionMap.get("default"))
                        .build())
                .build();
    }

}
