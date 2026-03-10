package com.nivasafinance.features.master.codemaster.enums;

import com.nivasafinance.features.master.codemaster.dto.IconSize;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import lombok.Getter;

@Getter
public enum IconContext {
    DEFAULT("default"),
    CRM("crm"),
    WEB("web"),
    APP("app");

    private final String value;

    IconContext(String value) {
        this.value = value;
    }

    public static IconContext from(String s) {
        if (s == null || s.isBlank()) {
            return DEFAULT;
        }
        String lower = s.toLowerCase();
        for (IconContext c : values()) {
            if (c.value.equals(lower)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Invalid icon context: " + s + ". Valid: default, crm, web, app");
    }

    public IconSize getIconSizeFrom(com.nivasafinance.features.master.codemaster.dto.Icons icons) {
        if (icons == null) {
            return null;
        }
        return switch (this) {
            case DEFAULT -> icons.getDefaultIcon();
            case CRM -> icons.getCrm();
            case WEB -> icons.getWeb();
            case APP -> icons.getApp();
        };
    }

    public void setIconSizeOn(com.nivasafinance.features.master.codemaster.dto.Icons icons, IconSize iconSize) {
        if (icons == null) {
            return;
        }
        switch (this) {
            case DEFAULT -> icons.setDefaultIcon(iconSize);
            case CRM -> icons.setCrm(iconSize);
            case WEB -> icons.setWeb(iconSize);
            case APP -> icons.setApp(iconSize);
        }
    }

    public IconSize getIconSizeFrom(MasterCodeValue.IconsData data) {
        MasterCodeValue.IconSizeData sizeData = getIconSizeDataFrom(data);
        return IconSize.from(sizeData);
    }

    public MasterCodeValue.IconSizeData getIconSizeDataFrom(MasterCodeValue.IconsData data) {
        if (data == null) {
            return null;
        }
        return switch (this) {
            case DEFAULT -> data.getDefaultIcon();
            case CRM -> data.getCrm();
            case WEB -> data.getWeb();
            case APP -> data.getApp();
        };
    }

    public void setIconSizeDataOn(MasterCodeValue.IconsData data, MasterCodeValue.IconSizeData sizeData) {
        if (data == null) {
            return;
        }
        switch (this) {
            case DEFAULT -> data.setDefaultIcon(sizeData);
            case CRM -> data.setCrm(sizeData);
            case WEB -> data.setWeb(sizeData);
            case APP -> data.setApp(sizeData);
        }
    }
}
