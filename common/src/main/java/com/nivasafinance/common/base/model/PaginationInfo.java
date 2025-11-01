package com.nivasafinance.common.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationInfo {
    private int offset;
    private int limit;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private boolean hasNext;
    private boolean hasPrevious;
}

