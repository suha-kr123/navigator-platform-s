package com.nivasafinance.features.master.products.service;

import com.nivasafinance.features.master.products.dto.ProductResponse;

import java.util.List;

public interface ProductReadService {
    
    List<ProductResponse> getAllProducts();
}

