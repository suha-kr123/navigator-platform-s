package com.nivasafinance.features.master.products.service;

import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.enums.ProductStatus;

import java.util.List;

public interface ProductReadService {

    List<ProductResponse> getAllProducts(ProductStatus status);
    ProductResponse getProductByCode(String code);
}

