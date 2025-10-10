package com.nivasafinance.common.base.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PaginationRequest(
    @field:Min(0, message = "Offset must be 0 or greater")
    val offset: Int = 0,

    @field:Min(1, message = "Limit must be at least 1")
    @field:Max(MAX_LIMIT, message = "Limit cannot exceed $MAX_LIMIT")
    val limit: Int = DEFAULT_LIMIT,

    val sortBy: String? = null,
    val sortDirection: String = "ASC",
    
    /**
     * Whether to include total count in pagination info.
     * When false, totalElements will be -1 and totalPages will be calculated differently.
     * This can improve performance for large datasets when total count is not needed.
     */
    val includeTotalCount: Boolean = true
) {
    companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 100L
    }
}
