package com.nivasafinance.common.base.model;

import java.util.Objects;

public class MasterLanguageData {
    private String defaultValue;

    public MasterLanguageData() {
        this.defaultValue = "";
    }

    public MasterLanguageData(String defaultValue) {
        this.defaultValue = defaultValue != null ? defaultValue : "";
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasterLanguageData that = (MasterLanguageData) o;
        return Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultValue);
    }

    @Override
    public String toString() {
        return "MasterLanguageData{" +
                "defaultValue='" + defaultValue + '\'' +
                '}';
    }
}

