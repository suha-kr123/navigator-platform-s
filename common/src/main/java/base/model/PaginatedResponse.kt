package base.model

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
