package event

import java.time.LocalDateTime
import java.util.UUID

interface BaseRequest {
    val requestId: UUID
    val correlationId: String
    val timestamp: LocalDateTime
}

interface BaseResponse {
    val requestId: UUID
    val correlationId: String
    val timestamp: LocalDateTime
    val success: Boolean
    val error: String?
}
