package com.nivasafinance.features.master.products.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.enums.ProductStatus;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.common.annotations.RequirePermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/products")
public class ProductController {

    private final ProductReadService productReadService;

    public ProductController(ProductReadService productReadService) {
        this.productReadService = productReadService;
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_MASTER_PRODUCTS")
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) ProductStatus status) {
        List<ProductResponse> responses = productReadService.getAllProducts(status);
        return ResponseEntity.ok(responses);
    }
}

