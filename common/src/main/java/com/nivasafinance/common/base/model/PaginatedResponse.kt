package com.nivasafinance.common.base.model

data class PaginatedResponse<T>(
    val content: List<T>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val offset: Int,
    val limit: Int,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

fun <T> List<T>.toBasicPaginatedResponse(): PaginatedResponse<T> {
    val totalElements = size.toLong()
    val hasContent = totalElements > 0

    val paginationInfo = PaginationInfo(
        offset = 0,
        limit = size,
        totalElements = totalElements,
        totalPages = if (hasContent) 1 else 0,
        currentPage = if (hasContent) 1 else 0,
        hasNext = false,
        hasPrevious = false
    )

    return PaginatedResponse(
        content = this,
        pagination = paginationInfo
    )
}
