package base.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class PaginationRequest(
    @field:Min(0, message = "Offset must be 0 or greater")
    val offset: Int = 0,

    @field:Min(1, message = "Limit must be at least 1")
    @field:Max(MAX_LIMIT, message = "Limit cannot exceed $MAX_LIMIT")
    val limit: Int = DEFAULT_LIMIT,

    val sortBy: String? = null,
    val sortDirection: SortDirection = SortDirection.ASC
) {
    companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 100L
    }
}

enum class SortDirection {
    ASC, DESC
}
