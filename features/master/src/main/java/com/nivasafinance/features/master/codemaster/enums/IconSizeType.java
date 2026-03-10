package com.nivasafinance.features.master.codemaster.enums;

import com.nivasafinance.features.master.codemaster.dto.IconSize;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import lombok.Getter;

@Getter
public enum IconSizeType {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    XL("xl"),
    XXL("xxl"),
    SVG("svg");

    private final String value;

    IconSizeType(String value) {
        this.value = value;
    }

    public static IconSizeType from(String s) {
        if (s == null || s.isBlank()) {
            return MEDIUM;
        }
        String lower = s.toLowerCase();
        for (IconSizeType v : values()) {
            if (v.value.equals(lower)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Invalid icon size: " + s + ". Valid: small, medium, large, xl, xxl, svg");
    }

    public String getUrlFrom(IconSize iconSize) {
        if (iconSize == null) {
            return null;
        }
        return switch (this) {
            case SMALL -> iconSize.getSmall();
            case MEDIUM -> iconSize.getMedium();
            case LARGE -> iconSize.getLarge();
            case XL -> iconSize.getXl();
            case XXL -> iconSize.getXxl();
            case SVG -> iconSize.getSvg();
        };
    }

    public void setUrlOn(IconSize iconSize, String url) {
        if (iconSize == null) {
            return;
        }
        switch (this) {
            case SMALL -> iconSize.setSmall(url);
            case MEDIUM -> iconSize.setMedium(url);
            case LARGE -> iconSize.setLarge(url);
            case XL -> iconSize.setXl(url);
            case XXL -> iconSize.setXxl(url);
            case SVG -> iconSize.setSvg(url);
        }
    }

    public String getUrlFrom(MasterCodeValue.IconSizeData data) {
        if (data == null) {
            return null;
        }
        MasterCodeValue.IconAsset asset = getIconAssetFrom(data);
        return asset != null ? asset.getUrl() : null;
    }

    public Long getDocumentIdFrom(MasterCodeValue.IconSizeData data) {
        if (data == null) {
            return null;
        }
        MasterCodeValue.IconAsset asset = getIconAssetFrom(data);
        return asset != null ? asset.getDocumentId() : null;
    }

    private MasterCodeValue.IconAsset getIconAssetFrom(MasterCodeValue.IconSizeData data) {
        return switch (this) {
            case SMALL -> data.getSmall();
            case MEDIUM -> data.getMedium();
            case LARGE -> data.getLarge();
            case XL -> data.getXl();
            case XXL -> data.getXxl();
            case SVG -> data.getSvg();
        };
    }

    public void setUrlAndDocumentIdOn(MasterCodeValue.IconSizeData data, String url, Long documentId) {
        if (data == null) {
            return;
        }
        MasterCodeValue.IconAsset asset = MasterCodeValue.IconAsset.builder().url(url).documentId(documentId).build();
        switch (this) {
            case SMALL -> data.setSmall(asset);
            case MEDIUM -> data.setMedium(asset);
            case LARGE -> data.setLarge(asset);
            case XL -> data.setXl(asset);
            case XXL -> data.setXxl(asset);
            case SVG -> data.setSvg(asset);
        }
    }
}
