package com.nivasafinance.features.master.codemaster.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nivasafinance.common.base.model.MasterLanguageResolver;
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
public class MasterCodeValueResponse {

    private Long id;
    private String key;
    private Map<String, String> valueMap;
    private Map<String, String> descriptionMap;
    private Boolean isSystemDefined;
    private Long parentId;

    private List<Child> children;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Child {
        private Long id;
        private String key;
        private Map<String, String> valueMap;
        private Map<String, String> descriptionMap;
        private Boolean isActive;
        private Integer displayOrder;
        private Icons icons;

        public static Child from(MasterCodeValue value) {
            return Child.builder()
                    .id(value.getId())
                    .key(value.getKey())
                    .valueMap(MasterLanguageResolver.getDisplayMap(value.getValue()))
                    .descriptionMap(MasterLanguageResolver.getDisplayMap(value.getDescription()))
                    .isActive(value.getIsActive())
                    .displayOrder(value.getDisplayOrder())
                    .icons(Icons.from(value.getIcons()))
                    .build();
        }
    }

    public static MasterCodeValueResponse from(
            MasterCode masterCode,
            List<MasterCodeValue> masterCodeValues) {
        return MasterCodeValueResponse.builder()
                .id(masterCode.getId())
                .key(masterCode.getKey())
                .valueMap(MasterLanguageResolver.getDisplayMap(masterCode.getName()))
                .descriptionMap(MasterLanguageResolver.getDisplayMap(masterCode.getDescription()))
                .isSystemDefined(masterCode.getIsSystemDefined())
                .parentId(masterCode.getParentId())
                .children(
                        masterCodeValues.stream()
                                .map(Child::from)
                                .collect(Collectors.toList()))
                .build();
    }
}
