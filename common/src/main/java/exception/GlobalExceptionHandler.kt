package exception

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        exception: ResourceNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = exception.localizedMessage,
            statusCode = HttpStatus.NOT_FOUND,
            errorCode = "RESOURCE_NOT_FOUND",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "BAD_REQUEST",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(ex: ValidationException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "VALIDATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val fieldErrors = ex.bindingResult.fieldErrors
        val errorMessages = fieldErrors.joinToString(", ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        val apiError = ApiError(
            error = "Validation failed: $errorMessages",
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "VALIDATION_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ApiError> {
        val errorMessage = when (val cause = ex.cause) {
            is InvalidFormatException -> {
                val fieldName = cause.pathReference.toString().split("->").lastOrNull()?.trim() ?: "unknown"
                val invalidValue = cause.value?.toString() ?: "null"

                when {
                    fieldName.contains("stage", ignoreCase = true) -> {
                        val validValues = listOf(
                            "INQUIRY",
                            "DOCUMENTATION",
                            "PROCESSING",
                            "SANCTION",
                            "DISBURSEMENT",
                            "CLOSED"
                        )
                        "Invalid value '$invalidValue' for field '$fieldName'. " +
                            "Valid values are: ${validValues.joinToString(", ")}"
                    }
                    fieldName.contains("status", ignoreCase = true) -> {
                        val validValues = listOf("ACTIVE", "ON_HOLD", "REJECTED", "CANCELLED", "COMPLETED")
                        "Invalid value '$invalidValue' for field '$fieldName'. " +
                            "Valid values are: ${validValues.joinToString(", ")}"
                    }
                    else -> "Invalid value '$invalidValue' for field '$fieldName'"
                }
            }
            else -> "Invalid request body: ${ex.message}"
        }

        val apiError = ApiError(
            error = errorMessage,
            statusCode = HttpStatus.BAD_REQUEST,
            errorCode = "INVALID_REQUEST_BODY",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(ex: ConflictException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.CONFLICT,
            errorCode = "CONFLICT",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.UNAUTHORIZED,
            errorCode = "UNAUTHORIZED",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: WebRequest): ResponseEntity<ApiError> {
        val apiError = ApiError(
            error = ex.localizedMessage,
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR,
            errorCode = "INTERNAL_ERROR",
            requestId = generateRequestId(),
            path = request.getDescription(false)
        )
        return ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun generateRequestId(): String = UUID.randomUUID().toString()
}
