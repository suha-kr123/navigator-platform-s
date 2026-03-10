package com.nivasafinance.features.master.codemaster.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Icons {

    @JsonProperty("default")
    private IconSize defaultIcon;

    private IconSize crm;
    private IconSize web;
    private IconSize app;

    public static Icons from(MasterCodeValue.IconsData data) {
        if (data == null) {
            return null;
        }
        return Icons.builder()
                .defaultIcon(IconSize.from(data.getDefaultIcon()))
                .crm(IconSize.from(data.getCrm()))
                .web(IconSize.from(data.getWeb()))
                .app(IconSize.from(data.getApp()))
                .build();
    }

    public MasterCodeValue.IconsData toIconsData() {
        return MasterCodeValue.IconsData.builder()
                .defaultIcon(defaultIcon != null ? defaultIcon.toIconSizeData() : null)
                .crm(crm != null ? crm.toIconSizeData() : null)
                .web(web != null ? web.toIconSizeData() : null)
                .app(app != null ? app.toIconSizeData() : null)
                .build();
    }
}
