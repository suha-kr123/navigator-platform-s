package com.nivasafinance.common.base.model;

import java.util.List;

public class PaginationUtils {
    
    public static <T> PaginatedResponse<T> toBasicPaginatedResponse(List<T> list) {
        long totalElements = list.size();
        boolean hasContent = totalElements > 0;

        PaginationInfo paginationInfo = new PaginationInfo(
                0,
                list.size(),
                totalElements,
                hasContent ? 1 : 0,
                hasContent ? 1 : 0,
                false,
                false
        );

        return new PaginatedResponse<>(list, paginationInfo);
    }
}

