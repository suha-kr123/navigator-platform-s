package com.nivasafinance.features.master.codemaster.dto;

import com.nivasafinance.common.base.model.MasterLanguageResolver;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.enums.IconContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeValueResponse {
    private Long id;
    private String key;
    private String codeKey;
    private String value;
    private String description;
    private Boolean isActive;
    private Integer displayOrder;
    private Object icons;

    public static CodeValueResponse from(MasterCodeValue masterCodeValue) {
        return CodeValueResponse.builder()
                .id(masterCodeValue.getId())
                .key(masterCodeValue.getKey())
                .codeKey(masterCodeValue.getCodeKey())
                .value(MasterLanguageResolver.getDisplayValue(masterCodeValue.getValue()))
                .description(MasterLanguageResolver.getDisplayValue(masterCodeValue.getDescription()))
                .isActive(masterCodeValue.getIsActive())
                .displayOrder(masterCodeValue.getDisplayOrder())
                .icons(Icons.from(masterCodeValue.getIcons()))
                .build();
    }

    public static CodeValueResponse from(MasterCodeValue masterCodeValue, IconContext context) {
        IconSize iconUrls = context != null ? context.getIconSizeFrom(masterCodeValue.getIcons()) : null;
        return CodeValueResponse.builder()
                .id(masterCodeValue.getId())
                .key(masterCodeValue.getKey())
                .codeKey(masterCodeValue.getCodeKey())
                .value(MasterLanguageResolver.getDisplayValue(masterCodeValue.getValue()))
                .description(MasterLanguageResolver.getDisplayValue(masterCodeValue.getDescription()))
                .isActive(masterCodeValue.getIsActive())
                .displayOrder(masterCodeValue.getDisplayOrder())
                .icons(iconUrls)
                .build();
    }
}

