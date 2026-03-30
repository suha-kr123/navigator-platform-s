package com.nivasafinance.externals.masters.product.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.service.ProductReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.OPEN_API_V1 + "/products")
public class ProductExternalController {

    private final ProductReadService productReadService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> productResponses = productReadService.getAllProducts();
        return ResponseEntity.ok(productResponses);
    }
}
