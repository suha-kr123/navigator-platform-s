package com.nivasafinance.features.bre.dto;

import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.enums.BREProvider;
import com.nivasafinance.features.document.dto.DocumentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BREConfigDetailedResponse {
    private String uname;
    private BREProvider provider;
    private Long dataProviderId;
    private DocumentResponse ruleFileDocument;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BREConfigDetailedResponse from(BREConfigs entity, DocumentResponse ruleFileDocument) {
        if (entity == null) {
            return null;
        }
        BREConfigs.Configs configs = entity.getConfigs();
        return BREConfigDetailedResponse.builder()
                .uname(entity.getUname())
                .provider(configs != null ? configs.getProvider() : null)
                .dataProviderId(configs != null ? configs.getDataProviderId() : null)
                .ruleFileDocument(ruleFileDocument)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
