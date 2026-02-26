package com.nivasafinance.features.bre.dto;

import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.enums.BREProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BREConfigResponse {
    private String uname;
    private BREProvider provider;

    public static BREConfigResponse from(BREConfigs entity) {
        if (entity == null) {
            return null;
        }
        BREConfigs.Configs configs = entity.getConfigs();
        return BREConfigResponse.builder()
                .uname(entity.getUname())
                .provider(configs != null ? configs.getProvider() : null)
                .build();
    }
}
