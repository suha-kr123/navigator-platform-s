package com.nivasafinance.features.master.products.repository;

import com.nivasafinance.features.master.products.entity.Product;
import com.nivasafinance.features.master.products.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);
    List<Product> findByStatus(ProductStatus status);
}

