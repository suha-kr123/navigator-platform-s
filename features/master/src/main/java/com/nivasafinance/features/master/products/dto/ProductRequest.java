package com.nivasafinance.features.master.products.dto;

import com.nivasafinance.common.base.model.MasterLanguageData;

import lombok.Data;

@Data
public class ProductRequest {

    private String code;
    private MasterLanguageData name;
}

