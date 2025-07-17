package exception

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ApiError(
    val timeStamp: LocalDateTime = LocalDateTime.now(),
    val error: String? = null,
    val statusCode: HttpStatus? = null
)
