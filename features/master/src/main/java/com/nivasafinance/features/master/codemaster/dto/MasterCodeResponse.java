package com.nivasafinance.features.master.codemaster.dto;

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
                .name(masterCode.getName().getDefaultValue())
                .description(masterCode.getDescription().getDefaultValue())
                .build();
    }
}
