package com.nivasafinance.externals.masters.product.controller;

import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.service.ProductReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductExternalControllerTest {

    @Mock
    private ProductReadService productReadService;

    @InjectMocks
    private ProductExternalController controller;

    @Test
    void getAllProducts_success_returnsOk() {
        List<ProductResponse> products = List.of(new ProductResponse());
        when(productReadService.getAllProducts()).thenReturn(products);

        ResponseEntity<List<ProductResponse>> result = controller.getAllProducts();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(productReadService).getAllProducts();
    }

    @Test
    void getAllProducts_emptyList_returnsOkWithEmptyBody() {
        when(productReadService.getAllProducts()).thenReturn(List.of());

        ResponseEntity<List<ProductResponse>> result = controller.getAllProducts();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }
}
