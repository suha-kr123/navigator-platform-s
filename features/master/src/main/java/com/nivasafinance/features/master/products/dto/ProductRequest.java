package com.nivasafinance.features.master.products.dto;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.products.enums.ProductStatus;

import lombok.Data;

@Data
public class ProductRequest {

    private String code;
    private MasterLanguageData name;
    private ProductStatus status;
}

