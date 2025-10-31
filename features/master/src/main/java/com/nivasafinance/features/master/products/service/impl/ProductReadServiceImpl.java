package com.nivasafinance.features.master.products.service.impl;

import com.nivasafinance.common.base.BaseNavigatorService;
import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.entity.Product;
import com.nivasafinance.features.master.products.exception.ProductExceptionFactory;
import com.nivasafinance.features.master.products.repository.ProductRepository;
import com.nivasafinance.features.master.products.service.ProductReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductReadServiceImpl extends BaseNavigatorService implements ProductReadService {

    private final ProductRepository productRepository;

    public ProductReadServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return products.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw ProductExceptionFactory.retrieveEntityFailed(getMessageSource());
        }
    }

    private ProductResponse mapEntityToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName()
        );
    }
}

