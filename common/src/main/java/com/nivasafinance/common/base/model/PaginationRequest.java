package com.nivasafinance.common.base.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequest {
    public static final int DEFAULT_LIMIT = 20;
    public static final long MAX_LIMIT = 100L;

    @Min(value = 0, message = "Offset must be 0 or greater")
    private int offset = 0;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = MAX_LIMIT, message = "Limit cannot exceed " + MAX_LIMIT)
    private int limit = DEFAULT_LIMIT;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}

