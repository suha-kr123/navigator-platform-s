package com.nivasafinance.features.master.codemaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IconSize {

    private String small;
    private String medium;
    private String large;
    private String xl;
    private String xxl;
    private String svg;

    public static IconSize from(MasterCodeValue.IconSizeData data) {
        if (data == null) {
            return null;
        }
        return IconSize.builder()
                .small(urlFrom(data.getSmall()))
                .medium(urlFrom(data.getMedium()))
                .large(urlFrom(data.getLarge()))
                .xl(urlFrom(data.getXl()))
                .xxl(urlFrom(data.getXxl()))
                .svg(urlFrom(data.getSvg()))
                .build();
    }

    private static String urlFrom(MasterCodeValue.IconAsset asset) {
        return asset != null ? asset.getUrl() : null;
    }

    public MasterCodeValue.IconSizeData toIconSizeData() {
        return MasterCodeValue.IconSizeData.builder()
                .small(asset(small))
                .medium(asset(medium))
                .large(asset(large))
                .xl(asset(xl))
                .xxl(asset(xxl))
                .svg(asset(svg))
                .build();
    }

    private static MasterCodeValue.IconAsset asset(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return MasterCodeValue.IconAsset.builder().url(url).documentId(null).build();
    }
}
