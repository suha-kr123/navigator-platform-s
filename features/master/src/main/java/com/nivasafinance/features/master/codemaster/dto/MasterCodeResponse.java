package com.nivasafinance.features.master.codemaster.dto;

import com.nivasafinance.common.base.model.MasterLanguageResolver;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterCodeResponse {
    private String key;
    private String name;
    private String description;

    public static MasterCodeResponse from(MasterCode masterCode) {
        return MasterCodeResponse.builder()
                .key(masterCode.getKey())
                .name(masterCode.getName() != null ? MasterLanguageResolver.getDisplayValue(masterCode.getName()) : null)
                .description(masterCode.getDescription() != null ? MasterLanguageResolver.getDisplayValue(masterCode.getDescription()) : null)
                .build();
    }
}
