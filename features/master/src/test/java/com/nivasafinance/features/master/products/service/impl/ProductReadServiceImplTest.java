package com.nivasafinance.features.master.products.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.entity.Product;
import com.nivasafinance.features.master.products.exception.ProductCodeNotFoundException;
import com.nivasafinance.features.master.products.exception.ProductOperationException;
import com.nivasafinance.features.master.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReadServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MessageSource messageSource;

    private ProductReadServiceImpl productReadService;

    private Product product;
    private Product secondProduct;

    @BeforeEach
    void setUp() {
        productReadService = new ProductReadServiceImpl(productRepository);
        productReadService.setMessageSource(messageSource);

        product = new Product();
        product.setId(1L);
        product.setCode("HL");
        product.setName(MasterLanguageData.builder().defaultValue("Home Loan").build());

        secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setCode("PL");
        secondProduct.setName(MasterLanguageData.builder().defaultValue("Personal Loan").build());
    }

    // ── getAllProducts ────────────────────────────────────────────────

    @Test
    void getAllProducts_productsExist_returnsProductList() {
        when(productRepository.findAll()).thenReturn(List.of(product, secondProduct));

        List<ProductResponse> result = productReadService.getAllProducts();

        assertEquals(2, result.size(), "Should return all products from repository");
    }

    @Test
    void getAllProducts_productsExist_fieldsCorrectlyMapped() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> result = productReadService.getAllProducts();

        ProductResponse response = result.get(0);
        assertEquals(1L, response.getId(), "Product ID should be mapped correctly");
        assertEquals("HL", response.getCode(), "Product code should be mapped correctly");
    }

    @Test
    void getAllProducts_noProducts_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductResponse> result = productReadService.getAllProducts();

        assertTrue(result.isEmpty(), "Should return empty list when no products exist");
    }

    @Test
    void getAllProducts_repositoryThrowsException_throwsProductOperationException() {
        when(productRepository.findAll()).thenThrow(new DataAccessResourceFailureException("DB error"));

        assertThrows(ProductOperationException.class,
                () -> productReadService.getAllProducts(),
                "Repository exception should be wrapped in ProductOperationException");
    }

    // ── getProductByCode ─────────────────────────────────────────────

    @Test
    void getProductByCode_existingCode_returnsProductResponse() {
        when(productRepository.findByCode("HL")).thenReturn(Optional.of(product));

        ProductResponse result = productReadService.getProductByCode("HL");

        assertNotNull(result, "Response should not be null for an existing product code");
        assertEquals("HL", result.getCode(), "Returned product code should match the requested code");
        assertEquals(1L, result.getId(), "Returned product ID should match the entity");
    }

    @Test
    void getProductByCode_nonExistentCode_throwsProductCodeNotFound() {
        when(productRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(ProductCodeNotFoundException.class,
                () -> productReadService.getProductByCode("INVALID"),
                "Non-existent product code should throw ProductCodeNotFoundException");
    }

    @Test
    void getProductByCode_existingCode_verifiesRepositoryInteraction() {
        when(productRepository.findByCode("PL")).thenReturn(Optional.of(secondProduct));

        productReadService.getProductByCode("PL");

        verify(productRepository).findByCode("PL");
        verify(productRepository, never()).findAll();
    }
}
