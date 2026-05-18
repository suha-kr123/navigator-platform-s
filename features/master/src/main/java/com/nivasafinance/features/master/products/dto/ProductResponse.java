package com.nivasafinance.features.master.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String code;
    private String name;
    private String status;
}

